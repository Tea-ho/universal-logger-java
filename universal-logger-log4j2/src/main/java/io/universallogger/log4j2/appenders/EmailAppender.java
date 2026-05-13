package io.universallogger.log4j2.appenders;

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

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 이메일 알림을 위한 Log4j2 커스텀 Appender
 *
 * ERROR 레벨 이상의 로그 이벤트 발생 시 지정된 수신자에게 이메일 알림을 전송합니다.
 * SMTP 서버를 통해 이메일을 발송하며, HTML 형식의 본문을 지원합니다.
 *
 * <p>주요 기능:</p>
 * <ul>
 *   <li>SMTP Session 재사용 (매번 생성하지 않음)</li>
 *   <li>SMTP 타임아웃 설정</li>
 *   <li>비동기 전송 - 메인 스레드 블로킹 방지</li>
 *   <li>Rate limiting - 이메일 폭탄 방지 (분당 최대 10건)</li>
 *   <li>SMTP 인증 및 TLS 암호화 지원</li>
 *   <li>다중 수신자 지원 (쉼표로 구분)</li>
 *   <li>제목/본문 템플릿 커스터마이징</li>
 *   <li>예외 스택 트레이스 포함</li>
 * </ul>
 *
 * @see org.apache.logging.log4j.core.appender.AbstractAppender
 */
@Plugin(name = "UniversalEmail", category = Core.CATEGORY_NAME, elementType = Appender.ELEMENT_TYPE)
public class EmailAppender extends AbstractAppender {

    // Rate limiting: 분당 최대 10건
    private static final int MAX_EMAILS_PER_MINUTE = 10;
    private static final long RATE_LIMIT_WINDOW_MS = 60_000L;

    private final String smtpHost;
    private final int smtpPort;
    private final String username;
    private final String password;
    private final String from;
    private final String to;
    private final String subjectTemplate;
    private final String bodyTemplate;

    // SMTP Session 재사용
    private final Session mailSession;

    // 비동기 전송
    private final ExecutorService executorService;

    // Rate limiting
    private final AtomicInteger emailCount = new AtomicInteger(0);
    private final AtomicLong windowStart = new AtomicLong(System.currentTimeMillis());

    protected EmailAppender(String name, Filter filter,
                            String smtpHost, int smtpPort,
                            String username, String password,
                            String from, String to,
                            String subjectTemplate, String bodyTemplate) {
        super(name, filter, null, true, Property.EMPTY_ARRAY);
        this.smtpHost = smtpHost;
        this.smtpPort = smtpPort;
        this.username = username;
        this.password = password;
        this.from = from;
        this.to = to;
        this.subjectTemplate = subjectTemplate;
        this.bodyTemplate = bodyTemplate;

        // SMTP Session 1회 생성 후 재사용
        this.mailSession = createMailSession();

        // 비동기 전송을 위한 스레드 풀
        this.executorService = Executors.newSingleThreadExecutor(r -> {
            Thread thread = new Thread(r, "Log4j2-EmailAppender-Thread");
            thread.setDaemon(true);
            return thread;
        });
    }

    /**
     * SMTP Session 생성 (1회)
     */
    private Session createMailSession() {
        Properties props = new Properties();
        props.put("mail.smtp.host", smtpHost);
        props.put("mail.smtp.port", String.valueOf(smtpPort));
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");

        // SMTP 타임아웃 설정 (5초)
        props.put("mail.smtp.connectiontimeout", "5000");
        props.put("mail.smtp.timeout", "5000");
        props.put("mail.smtp.writetimeout", "5000");

        return Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });
    }

    /**
     * Rate limiting 체크
     */
    private boolean isRateLimitAllowed() {
        long now = System.currentTimeMillis();
        long windowStartValue = windowStart.get();

        if (now - windowStartValue > RATE_LIMIT_WINDOW_MS) {
            windowStart.set(now);
            emailCount.set(0);
        }

        return emailCount.incrementAndGet() <= MAX_EMAILS_PER_MINUTE;
    }

    @Override
    public void append(LogEvent event) {
        // Rate limiting 체크
        if (!isRateLimitAllowed()) {
            error("Email rate limit exceeded. Skipping email notification.");
            return;
        }

        // 비동기 전송
        final LogEvent immutableEvent = event.toImmutable();
        executorService.submit(() -> {
            try {
                String subject = formatSubject(immutableEvent);
                String body = formatBody(immutableEvent);
                sendEmail(subject, body);
            } catch (Exception e) {
                error("Failed to send email notification", e);
            }
        });
    }

    private void sendEmail(String subject, String body) {
        try {
            Message message = new MimeMessage(mailSession);
            message.setFrom(new InternetAddress(from));

            // 쉼표로 구분된 수신자 파싱
            String[] recipients = to.split(",");
            InternetAddress[] addresses = new InternetAddress[recipients.length];
            for (int i = 0; i < recipients.length; i++) {
                addresses[i] = new InternetAddress(recipients[i].trim());
            }
            message.setRecipients(Message.RecipientType.TO, addresses);

            message.setSubject(subject);
            message.setContent(body, "text/html; charset=UTF-8");

            Transport.send(message);
        } catch (Exception e) {
            error("Failed to send email", e);
        }
    }

    @Override
    public void stop() {
        // ExecutorService 종료
        if (executorService != null) {
            executorService.shutdown();
            try {
                if (!executorService.awaitTermination(10, TimeUnit.SECONDS)) {
                    executorService.shutdownNow();
                }
            } catch (InterruptedException e) {
                executorService.shutdownNow();
            }
        }

        super.stop();
    }

    private String formatSubject(LogEvent event) {
        String template = subjectTemplate != null ? subjectTemplate : "[{level}] Log Event from {logger}";
        return template
            .replace("{level}", event.getLevel().toString())
            .replace("{logger}", event.getLoggerName())
            .replace("{message}", truncate(event.getMessage().getFormattedMessage(), 50));
    }

    private String formatBody(LogEvent event) {
        String template = bodyTemplate != null ? bodyTemplate : getDefaultBodyTemplate();

        String exceptionTrace = "";
        if (event.getThrown() != null) {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            event.getThrown().printStackTrace(pw);
            exceptionTrace = sw.toString();
        }

        return template
            .replace("{timestamp}", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
            .replace("{level}", event.getLevel().toString())
            .replace("{logger}", event.getLoggerName())
            .replace("{message}", escapeHtml(event.getMessage().getFormattedMessage()))
            .replace("{exception}", escapeHtml(exceptionTrace))
            .replace("{thread}", event.getThreadName());
    }

    private String getDefaultBodyTemplate() {
        return "<html><body>" +
               "<h2>Log Event Notification</h2>" +
               "<table border='1' cellpadding='5'>" +
               "<tr><th>Time</th><td>{timestamp}</td></tr>" +
               "<tr><th>Level</th><td>{level}</td></tr>" +
               "<tr><th>Logger</th><td>{logger}</td></tr>" +
               "<tr><th>Thread</th><td>{thread}</td></tr>" +
               "<tr><th>Message</th><td><pre>{message}</pre></td></tr>" +
               "<tr><th>Exception</th><td><pre>{exception}</pre></td></tr>" +
               "</table>" +
               "</body></html>";
    }

    private String truncate(String text, int maxLength) {
        if (text == null) return "";
        return text.length() > maxLength ? text.substring(0, maxLength) + "..." : text;
    }

    private String escapeHtml(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;")
                   .replace("<", "&lt;")
                   .replace(">", "&gt;")
                   .replace("\"", "&quot;")
                   .replace("'", "&#39;");
    }

    @PluginFactory
    public static EmailAppender createAppender(
            @PluginAttribute("name") String name,
            @PluginElement("Filter") Filter filter,
            @PluginAttribute("smtpHost") String smtpHost,
            @PluginAttribute("smtpPort") int smtpPort,
            @PluginAttribute("username") String username,
            @PluginAttribute("password") String password,
            @PluginAttribute("from") String from,
            @PluginAttribute("to") String to,
            @PluginAttribute("subject") String subject,
            @PluginAttribute("bodyTemplate") String bodyTemplate) {

        if (name == null) {
            LOGGER.error("No name provided for EmailAppender");
            return null;
        }

        return new EmailAppender(name, filter,
            smtpHost != null ? smtpHost : "smtp.gmail.com",
            smtpPort > 0 ? smtpPort : 587,
            username, password, from, to, subject, bodyTemplate);
    }
}
