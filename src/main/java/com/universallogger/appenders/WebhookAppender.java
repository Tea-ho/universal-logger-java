package com.universallogger.appenders;

import ch.qos.logback.core.AppenderBase;
import ch.qos.logback.classic.spi.ILoggingEvent;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 웹훅 어펜더 (Slack, Discord 등)
 */
public class WebhookAppender extends AppenderBase<ILoggingEvent> {

    private String url;
    private String format = "{message}";
    private CloseableHttpClient httpClient;

    @Override
    public void start() {
        super.start();
        httpClient = HttpClients.createDefault();
    }

    @Override
    public void stop() {
        super.stop();
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
        try {
            String message = formatMessage(event);
            String payload = "{\"text\": \"" + escapeJson(message) + "\"}";

            HttpPost post = new HttpPost(url);
            post.setHeader("Content-Type", "application/json");
            post.setEntity(new StringEntity(payload));

            httpClient.execute(post);
        } catch (Exception e) {
            addError("Failed to send webhook: " + e.getMessage());
        }
    }

    private String formatMessage(ILoggingEvent event) {
        return format
            .replace("{timestamp}", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
            .replace("{level}", event.getLevel().toString())
            .replace("{logger}", event.getLoggerName())
            .replace("{message}", event.getFormattedMessage())
            .replace("{app_name}", "JavaApp");
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