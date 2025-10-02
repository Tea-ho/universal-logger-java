package com.universallogger.formatters;

import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.spi.ILoggingEvent;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 커스텀 포맷터
 */
public class CustomFormatter extends PatternLayoutEncoder {

    private static final DateTimeFormatter DATE_FORMATTER =
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private String pattern = "{timestamp} | {level} | {logger} | {message}";

    public void setPattern(String pattern) {
        if (pattern != null && !pattern.trim().isEmpty()) {
            this.pattern = pattern;
        }
    }

    @Override
    public byte[] encode(ILoggingEvent event) {
        String formattedMessage = formatMessage(event);
        return (formattedMessage + "\n").getBytes();
    }

    /**
     * 메시지 포맷팅
     */
    private String formatMessage(ILoggingEvent event) {
        return this.pattern
            .replace("{timestamp}", DATE_FORMATTER.format(LocalDateTime.now()))
            .replace("{level}", String.format("%-5s", event.getLevel().toString()))
            .replace("{logger}", String.format("%-15s", event.getLoggerName()))
            .replace("{class}", event.getLoggerName())
            .replace("{method}", getMethodName(event))
            .replace("{line}", String.valueOf(getLineNumber(event)))
            .replace("{thread}", event.getThreadName())
            .replace("{message}", event.getFormattedMessage());
    }

    /**
     * 메서드명 추출
     */
    private String getMethodName(ILoggingEvent event) {
        StackTraceElement[] stack = event.getCallerData();
        if (stack != null && stack.length > 0) {
            return stack[0].getMethodName();
        }
        return "unknown";
    }

    /**
     * 라인 번호 추출
     */
    private int getLineNumber(ILoggingEvent event) {
        StackTraceElement[] stack = event.getCallerData();
        if (stack != null && stack.length > 0) {
            return stack[0].getLineNumber();
        }
        return 0;
    }
}