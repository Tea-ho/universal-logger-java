package com.universallogger.logback.appenders;

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
 * 이메일 알림을 위한 Logback 커스텀 앱펜더
 *
 * ERROR 레벨 이상의 로그 이벤트 발생 시 지정된 수신자에게 이메일 알림을 전송합니다.
 * SMTP 서버를 통해 이메일을 발송하며, SSL/TLS 암호화를 지원합니다.
 *
 * <p>주요 기능:</p>
 * <ul>
 *   <li>SMTP 인증 및 SSL/TLS 암호화 지원</li>
 *   <li>다중 수신자 지원</li>
 *   <li>포트에 따른 자동 암호화 설정 (465: SSL, 587: TLS)</li>
 *   <li>연결 타임아웃 설정 (5초)</li>
 * </ul>
 *
 * @see AppenderFactory#createEmailAppender
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
            addWarn("Email appender: No recipients configured. Email notifications disabled.");
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
            addInfo("Email appender configured successfully");
        } catch (Exception e) {
            addError("Email appender setup failed: " + e.getMessage(), e);
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
