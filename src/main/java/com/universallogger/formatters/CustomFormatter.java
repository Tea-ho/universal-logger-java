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
        // 클래스명과 메서드명을 함께 표시
        String classAndMethod = getClassAndMethodName(event);

        return this.pattern
            .replace("{timestamp}", DATE_FORMATTER.format(LocalDateTime.now()))
            .replace("{level}", String.format("%-5s", event.getLevel().toString()))
            .replace("{logger}", String.format("%-15s", classAndMethod))
            .replace("{class}", getClassName(event))
            .replace("{method}", getMethodName(event))
            .replace("{line}", String.valueOf(getLineNumber(event)))
            .replace("{thread}", event.getThreadName())
            .replace("{message}", event.getFormattedMessage());
    }

    /**
     * 클래스명 추출 (전체 패키지 경로)
     */
    private String getClassName(ILoggingEvent event) {
        StackTraceElement[] stack = event.getCallerData();
        if (stack != null && stack.length > 0) {
            return stack[0].getClassName();
        }
        return event.getLoggerName();
    }

    /**
     * 클래스명과 메서드명을 함께 반환 (간단한 클래스명.메서드명)
     * SQL 로깅의 경우 로거 이름에 실제 호출자 정보가 포함되어 있음
     */
    private String getClassAndMethodName(ILoggingEvent event) {
        // 로거 이름을 우선 사용 (SqlCaptureProxy가 실제 호출자를 로거 이름으로 전달)
        String loggerName = event.getLoggerName();
        if (loggerName != null && !loggerName.startsWith("com.universallogger")) {
            // 로거 이름이 실제 클래스 정보인 경우 (SqlCaptureProxy에서 전달)
            return loggerName;
        }

        // 일반적인 로깅의 경우 스택에서 추출
        StackTraceElement[] stack = event.getCallerData();
        if (stack != null && stack.length > 0) {
            String fullClassName = stack[0].getClassName();
            String simpleClassName = fullClassName.substring(fullClassName.lastIndexOf('.') + 1);
            String methodName = stack[0].getMethodName();
            int lineNumber = stack[0].getLineNumber();
            return simpleClassName + "." + methodName + ":" + lineNumber;
        }
        return loggerName;
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