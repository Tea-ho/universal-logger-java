package com.universallogger.appenders;

import ch.qos.logback.core.AppenderBase;
import ch.qos.logback.classic.spi.ILoggingEvent;
import com.universallogger.config.LoggerConfig;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Properties;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 이메일 어펜더
 */
public class EmailAppender extends AppenderBase<ILoggingEvent> {

    private LoggerConfig.EmailConfig emailConfig;
    private Session mailSession;

    @Override
    public void start() {
        super.start();
        setupMailSession();
    }

    private void setupMailSession() {
        if (emailConfig == null || emailConfig.getSmtp() == null) {
            return;
        }

        // 수신자가 없으면 설정하지 않음
        if (emailConfig.getTo() == null || emailConfig.getTo().length == 0) {
            System.err.println("⚠️  Email appender: No recipients configured. Email notifications disabled.");
            return;
        }

        Properties props = new Properties();
        props.put("mail.smtp.host", emailConfig.getSmtp().getHost());
        props.put("mail.smtp.port", emailConfig.getSmtp().getPort());
        props.put("mail.smtp.auth", "true");

        // 포트 465는 SSL, 587은 TLS(STARTTLS)
        int port = emailConfig.getSmtp().getPort();
        if (port == 465) {
            // SSL 설정
            props.put("mail.smtp.ssl.enable", "true");
            props.put("mail.smtp.socketFactory.port", String.valueOf(port));
            props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
            props.put("mail.smtp.socketFactory.fallback", "false");
        } else {
            // TLS(STARTTLS) 설정
            props.put("mail.smtp.starttls.enable", "true");
        }

        // 타임아웃 설정 (5초)
        props.put("mail.smtp.connectiontimeout", "5000");
        props.put("mail.smtp.timeout", "5000");
        props.put("mail.smtp.writetimeout", "5000");

        try {
            mailSession = Session.getInstance(props, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(
                        emailConfig.getSmtp().getUsername(),
                        emailConfig.getSmtp().getPassword()
                    );
                }
            });
            System.out.println("✅ Email appender configured successfully");
        } catch (Exception e) {
            System.err.println("⚠️  Email appender setup failed: " + e.getMessage());
        }
    }

    @Override
    protected void append(ILoggingEvent event) {
        try {
            if (mailSession == null || emailConfig.getTo() == null) {
                return;
            }

            MimeMessage message = new MimeMessage(mailSession);
            message.setFrom(new InternetAddress(emailConfig.getSmtp().getUsername()));

            for (String recipient : emailConfig.getTo()) {
                message.addRecipient(Message.RecipientType.TO, new InternetAddress(recipient));
            }

            String subject = emailConfig.getSubject()
                .replace("{appName}", "JavaApp")
                .replace("{level}", event.getLevel().toString());

            message.setSubject(subject);

            String body = String.format(
                "시간: %s\n레벨: %s\n로거: %s\n메시지: %s\n",
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
                event.getLevel().toString(),
                event.getLoggerName(),
                event.getFormattedMessage()
            );

            message.setText(body);

            Transport.send(message);

        } catch (Exception e) {
            addError("Failed to send email: " + e.getMessage());
        }
    }

    public void setEmailConfig(LoggerConfig.EmailConfig emailConfig) {
        this.emailConfig = emailConfig;
    }
}
