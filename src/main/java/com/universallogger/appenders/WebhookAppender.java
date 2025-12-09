package com.universallogger.appenders;

import ch.qos.logback.core.AppenderBase;
import ch.qos.logback.classic.spi.ILoggingEvent;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * 웹훅 어펜더 (Slack, Discord 등)
 * 비동기 전송 + 타임아웃 적용
 */
public class WebhookAppender extends AppenderBase<ILoggingEvent> {

    private String url;
    private String format = "{message}";
    private CloseableHttpClient httpClient;
    private ExecutorService executorService;
    private static final int TIMEOUT_MS = 3000; // 3초 타임아웃

    @Override
    public void start() {
        super.start();

        // 타임아웃 설정이 적용된 HttpClient 생성
        RequestConfig requestConfig = RequestConfig.custom()
            .setConnectTimeout(TIMEOUT_MS)
            .setSocketTimeout(TIMEOUT_MS)
            .setConnectionRequestTimeout(TIMEOUT_MS)
            .build();

        httpClient = HttpClients.custom()
            .setDefaultRequestConfig(requestConfig)
            .build();

        // 비동기 전송을 위한 스레드 풀 (단일 스레드)
        executorService = Executors.newSingleThreadExecutor(r -> {
            Thread thread = new Thread(r, "WebhookAppender-Thread");
            thread.setDaemon(true); // 데몬 스레드로 설정
            return thread;
        });
    }

    @Override
    public void stop() {
        super.stop();

        // ExecutorService 종료
        if (executorService != null) {
            executorService.shutdown();
            try {
                if (!executorService.awaitTermination(5, TimeUnit.SECONDS)) {
                    executorService.shutdownNow();
                }
            } catch (InterruptedException e) {
                executorService.shutdownNow();
            }
        }

        // HttpClient 종료
        if (httpClient != null) {
            try {
                httpClient.close();
            } catch (Exception e) {
                // ignore
            }
        }
    }

    @Override
    protected void append(ILoggingEvent event) {
        // 비동기로 웹훅 전송 (메인 스레드 블로킹 방지)
        executorService.submit(() -> {
            try {
                String message = formatMessage(event);
                String payload = "{\"text\": \"" + escapeJson(message) + "\"}";

                HttpPost post = new HttpPost(url);
                post.setHeader("Content-Type", "application/json");
                post.setEntity(new StringEntity(payload, "UTF-8"));

                httpClient.execute(post).close(); // 응답 즉시 닫기
            } catch (Exception e) {
                // 에러 발생 시에도 블로킹하지 않고 무시 (로그만 남김)
                addError("Failed to send webhook: " + e.getMessage());
            }
        });
    }

    private String formatMessage(ILoggingEvent event) {
        String exceptionTrace = getStackTrace(event);
        return format
            .replace("{timestamp}", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
            .replace("{level}", event.getLevel().toString())
            .replace("{logger}", event.getLoggerName())
            .replace("{message}", event.getFormattedMessage())
            .replace("{exception}", exceptionTrace)
            .replace("{app_name}", "JavaApp");
    }

    private String getStackTrace(ILoggingEvent event) {
        if (event.getThrowableProxy() == null) {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        ch.qos.logback.classic.spi.IThrowableProxy throwableProxy = event.getThrowableProxy();

        // Exception class and message
        sb.append(throwableProxy.getClassName());
        if (throwableProxy.getMessage() != null) {
            sb.append(": ").append(throwableProxy.getMessage());
        }
        sb.append("\n");

        // Stack trace elements
        ch.qos.logback.classic.spi.StackTraceElementProxy[] stackTrace = throwableProxy.getStackTraceElementProxyArray();
        if (stackTrace != null) {
            for (ch.qos.logback.classic.spi.StackTraceElementProxy element : stackTrace) {
                sb.append("    at ").append(element.getStackTraceElement().toString()).append("\n");
            }
        }

        // Caused by (if any)
        ch.qos.logback.classic.spi.IThrowableProxy cause = throwableProxy.getCause();
        while (cause != null) {
            sb.append("Caused by: ").append(cause.getClassName());
            if (cause.getMessage() != null) {
                sb.append(": ").append(cause.getMessage());
            }
            sb.append("\n");

            ch.qos.logback.classic.spi.StackTraceElementProxy[] causeStackTrace = cause.getStackTraceElementProxyArray();
            if (causeStackTrace != null) {
                for (ch.qos.logback.classic.spi.StackTraceElementProxy element : causeStackTrace) {
                    sb.append("    at ").append(element.getStackTraceElement().toString()).append("\n");
                }
            }
            cause = cause.getCause();
        }

        return sb.toString().trim();
    }

    private String escapeJson(String text) {
        return text.replace("\"", "\\\"").replace("\n", "\\n");
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setFormat(String format) {
        this.format = format;
    }
}