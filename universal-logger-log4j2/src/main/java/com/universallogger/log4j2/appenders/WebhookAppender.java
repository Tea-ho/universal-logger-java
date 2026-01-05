package com.universallogger.log4j2.appenders;

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

/**
 * Log4j2 Custom Appender for Webhook notifications
 */
@Plugin(name = "UniversalWebhook", category = Core.CATEGORY_NAME, elementType = Appender.ELEMENT_TYPE)
public class WebhookAppender extends AbstractAppender {

    private final String webhookUrl;
    private final String messageFormat;
    private final CloseableHttpClient httpClient;

    protected WebhookAppender(String name, Filter filter,
                              String webhookUrl,
                              String messageFormat) {
        super(name, filter, null, true, Property.EMPTY_ARRAY);
        this.webhookUrl = webhookUrl;
        this.messageFormat = messageFormat;
        this.httpClient = HttpClients.createDefault();
    }

    @Override
    public void append(LogEvent event) {
        try {
            String formattedMessage = formatMessage(event);
            sendWebhook(formattedMessage);
        } catch (Exception e) {
            error("Failed to send webhook notification", e);
        }
    }

    private void sendWebhook(String message) {
        try {
            HttpPost post = new HttpPost(webhookUrl);

            // Slack/Discord 형식의 JSON 페이로드
            String payload = "{\"text\":\"" + escapeJson(message) + "\"}";

            post.setEntity(new StringEntity(payload, ContentType.APPLICATION_JSON));

            CloseableHttpResponse response = httpClient.execute(post);
            try {
                int statusCode = response.getStatusLine().getStatusCode();
                if (statusCode < 200 || statusCode >= 300) {
                    error("Webhook returned error status: " + statusCode);
                }
            } finally {
                response.close();
            }
        } catch (Exception e) {
            error("Failed to send webhook", e);
        }
    }

    private String formatMessage(LogEvent event) {
        String format = messageFormat != null ? messageFormat : getDefaultFormat();

        // Extract exception stack trace if present
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
            @PluginAttribute("messageFormat") String messageFormat) {

        if (name == null) {
            LOGGER.error("No name provided for WebhookAppender");
            return null;
        }

        if (webhookUrl == null) {
            LOGGER.error("No webhook URL provided for WebhookAppender");
            return null;
        }

        return new WebhookAppender(name, filter, webhookUrl, messageFormat);
    }
}
