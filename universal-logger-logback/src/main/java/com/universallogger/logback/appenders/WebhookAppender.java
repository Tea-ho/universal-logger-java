package com.universallogger.logback.appenders;

import ch.qos.logback.core.AppenderBase;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.IThrowableProxy;
import com.universallogger.config.LoggerConfig;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 웹훅 알림을 위한 Logback 커스텀 앱펜더
 *
 * 로그 이벤트 발생 시 지정된 웹훅 URL로 HTTP POST 요청을 전송합니다.
 * Slack, Discord, Microsoft Teams 등의 웹훅 서비스와 연동할 수 있습니다.
 *
 * <p>주요 기능:</p>
 * <ul>
 *   <li>비동기 전송 - 메인 스레드 블로킹 방지</li>
 *   <li>타임아웃 설정 (3초)</li>
 *   <li>Slack/Discord 호환 JSON 페이로드 형식</li>
 *   <li>메시지 포맷 템플릿 커스터마이징</li>
 *   <li>예외 스택 트레이스 포함</li>
 *   <li>Rate Limiting - 분당 최대 전송 수 제한</li>
 *   <li>Deduplication - 동일 에러 중복 알림 방지</li>
 * </ul>
 *
 * @see AppenderFactory#createWebhookAppender
 */
public class WebhookAppender extends AppenderBase<ILoggingEvent> {

    private String url;
    private String format = "{message}";
    private String appName = "JavaApp";
    private CloseableHttpClient httpClient;
    private ExecutorService executorService;
    private static final int TIMEOUT_MS = 3000; // 3초 타임아웃

    // Rate Limiting 필드
    private int maxPerMinute = 10;
    private static final long RATE_LIMIT_WINDOW_MS = 60_000L;
    private final AtomicInteger sendCount = new AtomicInteger(0);
    private final AtomicLong windowStart = new AtomicLong(System.currentTimeMillis());

    // Deduplication 필드
    private int deduplicationSeconds = 300;
    private static final int MAX_DEDUP_ENTRIES = 1000;
    private final ConcurrentHashMap<String, Long> recentSentMap = new ConcurrentHashMap<>();

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
        // Rate Limiting 체크
        if (!isRateLimitAllowed()) {
            return;
        }

        // Deduplication 체크
        if (!isDeduplicationAllowed(event)) {
            return;
        }

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

    /**
     * Rate Limiting 체크
     * 분당 최대 전송 수를 초과하면 전송을 스킵한다.
     *
     * @return 전송 가능하면 true
     */
    private boolean isRateLimitAllowed() {
        long now = System.currentTimeMillis();
        long windowStartValue = windowStart.get();

        // 윈도우 초과 시 리셋
        if (now - windowStartValue > RATE_LIMIT_WINDOW_MS) {
            windowStart.set(now);
            sendCount.set(0);
        }

        return sendCount.incrementAndGet() <= maxPerMinute;
    }

    /**
     * Deduplication 체크
     * 동일 에러가 설정된 시간 내에 이미 전송되었으면 스킵한다.
     * 키 생성 전략: 예외 있으면 loggerName + exceptionClass, 없으면 loggerName + message 앞 100자
     *
     * @return 전송 가능하면 true
     */
    private boolean isDeduplicationAllowed(ILoggingEvent event) {
        // deduplicationSeconds가 0이면 비활성화
        if (deduplicationSeconds <= 0) {
            return true;
        }

        String dedupKey = buildDeduplicationKey(event);
        long now = System.currentTimeMillis();
        long deduplicationMs = deduplicationSeconds * 1000L;

        Long lastSent = recentSentMap.get(dedupKey);
        if (lastSent != null && (now - lastSent) < deduplicationMs) {
            // 동일 에러가 최근에 전송됨 → 스킵
            return false;
        }

        // 전송 기록 갱신
        recentSentMap.put(dedupKey, now);

        // 맵 크기 제한: MAX_DEDUP_ENTRIES 초과 시 오래된 항목 정리
        if (recentSentMap.size() > MAX_DEDUP_ENTRIES) {
            evictOldEntries(now, deduplicationMs);
        }

        return true;
    }

    /**
     * Deduplication 키 생성
     * Sentry 방식: 예외 클래스 기준으로 그룹핑하여 변수값이 다른 동일 에러도 하나로 묶음
     */
    private String buildDeduplicationKey(ILoggingEvent event) {
        String loggerName = event.getLoggerName();
        IThrowableProxy throwableProxy = event.getThrowableProxy();

        if (throwableProxy != null) {
            return loggerName + ":" + throwableProxy.getClassName();
        }

        // 예외 없으면 메시지 앞 100자 사용
        String message = event.getFormattedMessage();
        if (message != null && message.length() > 100) {
            message = message.substring(0, 100);
        }
        return loggerName + ":" + message;
    }

    /**
     * 만료된 Deduplication 항목 제거
     */
    private void evictOldEntries(long now, long deduplicationMs) {
        Iterator<Map.Entry<String, Long>> it = recentSentMap.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, Long> entry = it.next();
            if (now - entry.getValue() > deduplicationMs) {
                it.remove();
            }
        }
    }

    private String formatMessage(ILoggingEvent event) {
        String exceptionTrace = getStackTrace(event);
        return format
                .replace("{timestamp}", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
                .replace("{level}", event.getLevel().toString())
                .replace("{logger}", event.getLoggerName())
                .replace("{message}", event.getFormattedMessage())
                .replace("{exception}", exceptionTrace)
                .replace("{app_name}", appName);
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
        if (text == null) {
            return "";
        }

        // JSON 특수 문자 이스케이프 처리
        // 주의: 백슬래시를 가장 먼저 처리해야 함
        return text
            .replace("\\", "\\\\")     // 백슬래시 (가장 먼저 처리)
            .replace("\"", "\\\"")     // 따옴표
            .replace("\n", "\\n")      // 개행
            .replace("\r", "\\r")      // 캐리지 리턴
            .replace("\t", "\\t")      // 탭
            .replace("\b", "\\b")      // 백스페이스
            .replace("\f", "\\f");     // 폼 피드
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    /**
     * AlertPolicy 설정 적용
     */
    public void setAlertPolicy(LoggerConfig.WebhookConfig.AlertPolicyConfig policy) {
        if (policy != null) {
            this.maxPerMinute = policy.getRateLimit();
            this.deduplicationSeconds = policy.getDeduplicationSeconds();
        }
    }
}
