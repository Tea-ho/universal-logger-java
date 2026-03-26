package com.universallogger.log4j2.appenders;

import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.Core;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.Property;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginElement;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;

import java.io.PrintWriter;
import java.io.StringWriter;
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
 * 웹훅 알림을 위한 Log4j2 커스텀 Appender
 *
 * 로그 이벤트 발생 시 지정된 웹훅 URL로 HTTP POST 요청을 전송합니다.
 * Slack, Discord, Microsoft Teams 등의 웹훅 서비스와 연동할 수 있습니다.
 *
 * <p>주요 기능:</p>
 * <ul>
 *   <li>비동기 전송 - 메인 스레드 블로킹 방지</li>
 *   <li>연결 타임아웃 설정 (3초)</li>
 *   <li>Slack/Discord 호환 JSON 페이로드 형식</li>
 *   <li>메시지 포맷 템플릿 커스터마이징</li>
 *   <li>예외 스택 트레이스 포함</li>
 *   <li>Rate Limiting - 분당 최대 전송 수 제한</li>
 *   <li>Deduplication - 동일 에러 중복 알림 방지</li>
 * </ul>
 *
 * @see org.apache.logging.log4j.core.appender.AbstractAppender
 */
@Plugin(name = "UniversalWebhook", category = Core.CATEGORY_NAME, elementType = Appender.ELEMENT_TYPE)
public class WebhookAppender extends AbstractAppender {

    private static final int TIMEOUT_MS = 3000; // 3초 타임아웃
    private static final long RATE_LIMIT_WINDOW_MS = 60_000L;
    private static final int MAX_DEDUP_ENTRIES = 1000;

    private final String webhookUrl;
    private final String messageFormat;
    private final CloseableHttpClient httpClient;
    private final ExecutorService executorService;

    // Rate Limiting 필드
    private final int maxPerMinute;
    private final AtomicInteger sendCount = new AtomicInteger(0);
    private final AtomicLong windowStart = new AtomicLong(System.currentTimeMillis());

    // Deduplication 필드
    private final int deduplicationSeconds;
    private final ConcurrentHashMap<String, Long> recentSentMap = new ConcurrentHashMap<>();

    protected WebhookAppender(String name, Filter filter,
                              String webhookUrl,
                              String messageFormat,
                              int rateLimit,
                              int deduplicationSeconds) {
        super(name, filter, null, true, Property.EMPTY_ARRAY);
        this.webhookUrl = webhookUrl;
        this.messageFormat = messageFormat;
        this.maxPerMinute = rateLimit > 0 ? rateLimit : 10;
        this.deduplicationSeconds = deduplicationSeconds;

        // 타임아웃 설정이 적용된 HttpClient 생성
        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectTimeout(TIMEOUT_MS)
                .setSocketTimeout(TIMEOUT_MS)
                .setConnectionRequestTimeout(TIMEOUT_MS)
                .build();

        this.httpClient = HttpClients.custom()
                .setDefaultRequestConfig(requestConfig)
                .build();

        // 비동기 전송을 위한 스레드 풀
        this.executorService = Executors.newSingleThreadExecutor(r -> {
            Thread thread = new Thread(r, "Log4j2-WebhookAppender-Thread");
            thread.setDaemon(true);
            return thread;
        });
    }

    @Override
    public void append(LogEvent event) {
        // Rate Limiting 체크
        if (!isRateLimitAllowed()) {
            return;
        }

        // Deduplication 체크
        final LogEvent immutableEvent = event.toImmutable();
        if (!isDeduplicationAllowed(immutableEvent)) {
            return;
        }

        // 비동기로 웹훅 전송 (메인 스레드 블로킹 방지)
        executorService.submit(() -> {
            try {
                String formattedMessage = formatMessage(immutableEvent);
                sendWebhook(formattedMessage);
            } catch (Exception e) {
                error("Failed to send webhook notification", e);
            }
        });
    }

    /**
     * Rate Limiting 체크
     * 분당 최대 전송 수를 초과하면 전송을 스킵한다.
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
     */
    private boolean isDeduplicationAllowed(LogEvent event) {
        if (deduplicationSeconds <= 0) {
            return true;
        }

        String dedupKey = buildDeduplicationKey(event);
        long now = System.currentTimeMillis();
        long deduplicationMs = deduplicationSeconds * 1000L;

        Long lastSent = recentSentMap.get(dedupKey);
        if (lastSent != null && (now - lastSent) < deduplicationMs) {
            return false;
        }

        recentSentMap.put(dedupKey, now);

        // 맵 크기 제한
        if (recentSentMap.size() > MAX_DEDUP_ENTRIES) {
            evictOldEntries(now, deduplicationMs);
        }

        return true;
    }

    /**
     * Deduplication 키 생성
     * Sentry 방식: 예외 클래스 기준으로 그룹핑
     */
    private String buildDeduplicationKey(LogEvent event) {
        String loggerName = event.getLoggerName();

        if (event.getThrown() != null) {
            return loggerName + ":" + event.getThrown().getClass().getName();
        }

        // 예외 없으면 메시지 앞 100자 사용
        String message = event.getMessage().getFormattedMessage();
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

    private void sendWebhook(String message) {
        CloseableHttpResponse response = null;
        try {
            HttpPost post = new HttpPost(webhookUrl);
            String payload = "{\"text\":\"" + escapeJson(message) + "\"}";
            post.setEntity(new StringEntity(payload, ContentType.APPLICATION_JSON));

            response = httpClient.execute(post);
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode < 200 || statusCode >= 300) {
                error("Webhook returned error status: " + statusCode);
            }
        } catch (Exception e) {
            error("Failed to send webhook", e);
        } finally {
            if (response != null) {
                try {
                    response.close();
                } catch (Exception e) {
                    // 무시
                }
            }
        }
    }

    private String formatMessage(LogEvent event) {
        String format = messageFormat != null ? messageFormat : getDefaultFormat();

        // 예외가 있으면 스택 트레이스 추출
        String exceptionTrace = "";
        if (event.getThrown() != null) {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            event.getThrown().printStackTrace(pw);
            exceptionTrace = sw.toString();
        }

        return format
            .replace("{timestamp}", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
            .replace("{level}", event.getLevel().toString())
            .replace("{logger}", event.getLoggerName())
            .replace("{message}", event.getMessage().getFormattedMessage())
            .replace("{exception}", exceptionTrace)
            .replace("{thread}", event.getThreadName());
    }

    private String getDefaultFormat() {
        return "🚨 *{level}* Log Event\\n\\n" +
               "*Time:* {timestamp}\\n" +
               "*Logger:* `{logger}`\\n" +
               "*Thread:* {thread}\\n" +
               "*Message:*\\n```\\n{message}\\n```\\n" +
               "*Exception:*\\n```\\n{exception}\\n```";
    }

    private String escapeJson(String text) {
        if (text == null) return "";
        return text.replace("\\", "\\\\")
                   .replace("\"", "\\\"")
                   .replace("\n", "\\n")
                   .replace("\r", "\\r")
                   .replace("\t", "\\t");
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
        try {
            if (httpClient != null) {
                httpClient.close();
            }
        } catch (Exception e) {
            error("Failed to close HTTP client", e);
        }
    }

    @PluginFactory
    public static WebhookAppender createAppender(
            @PluginAttribute("name") String name,
            @PluginElement("Filter") Filter filter,
            @PluginAttribute("webhookUrl") String webhookUrl,
            @PluginAttribute("messageFormat") String messageFormat,
            @PluginAttribute(value = "rateLimit", defaultInt = 10) int rateLimit,
            @PluginAttribute(value = "deduplicationSeconds", defaultInt = 300) int deduplicationSeconds) {

        if (name == null) {
            LOGGER.error("No name provided for WebhookAppender");
            return null;
        }

        if (webhookUrl == null) {
            LOGGER.error("No webhook URL provided for WebhookAppender");
            return null;
        }

        return new WebhookAppender(name, filter, webhookUrl, messageFormat, rateLimit, deduplicationSeconds);
    }
}
