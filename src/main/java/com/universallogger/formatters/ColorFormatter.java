package com.universallogger.formatters;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.spi.ILoggingEvent;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.HashMap;

/**
 * 컬러 포맷터 - 콘솔 출력용
 */
public class ColorFormatter extends PatternLayoutEncoder {

    private static final DateTimeFormatter DATE_FORMATTER =
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private static final Map<String, String> DEFAULT_COLORS = new HashMap<>();
    static {
        DEFAULT_COLORS.put("DEBUG", "\u001B[36m");   // cyan
        DEFAULT_COLORS.put("INFO", "\u001B[32m");    // green
        DEFAULT_COLORS.put("WARN", "\u001B[33m");    // yellow
        DEFAULT_COLORS.put("ERROR", "\u001B[31m");   // red
        DEFAULT_COLORS.put("CRITICAL", "\u001B[35m"); // magenta
    }

    private static final String RESET = "\u001B[0m";

    private Map<String, String> colors = DEFAULT_COLORS;
    private String pattern = "[{timestamp}] {level} - {message}";

    public void setPattern(String pattern) {
        if (pattern != null && !pattern.trim().isEmpty()) {
            this.pattern = pattern;
        }
    }

    public void setColors(Map<String, String> colors) {
        if (colors != null && !colors.isEmpty()) {
            this.colors = new HashMap<>(DEFAULT_COLORS);
            // 사용자 색상으로 업데이트
            for (Map.Entry<String, String> entry : colors.entrySet()) {
                String ansiColor = convertToAnsiColor(entry.getValue());
                this.colors.put(entry.getKey(), ansiColor);
            }
        }
    }

    @Override
    public byte[] encode(ILoggingEvent event) {
        String formattedMessage = formatMessage(event);
        return (formattedMessage + "\n").getBytes(StandardCharsets.UTF_8);
    }

    /**
     * 메시지 포맷팅 (컬러 포함)
     */
    private String formatMessage(ILoggingEvent event) {
        String formatPattern = this.pattern;

        String levelName = event.getLevel().toString();
        String colorCode = colors.getOrDefault(levelName, "");
        String coloredLevel = colorCode + levelName + RESET;

        // 클래스명과 메서드명을 함께 표시
        String classAndMethod = getClassAndMethodName(event);

        return formatPattern
            .replace("{timestamp}", DATE_FORMATTER.format(LocalDateTime.now()))
            .replace("{level}", String.format("%-12s", coloredLevel)) // 컬러 코드 때문에 더 긴 포맷
            .replace("{logger}", String.format("%-15s", classAndMethod))
            .replace("{class}", getClassName(event))
            .replace("{method}", getMethodName(event))
            .replace("{line}", String.valueOf(getLineNumber(event)))
            .replace("{thread}", event.getThreadName())
            .replace("{message}", event.getFormattedMessage());
    }

    /**
     * 색상명을 ANSI 코드로 변환
     */
    private String convertToAnsiColor(String colorName) {
        if (colorName == null) return "";

        switch (colorName.toLowerCase()) {
            case "black": return "\u001B[30m";
            case "red": return "\u001B[31m";
            case "green": return "\u001B[32m";
            case "yellow": return "\u001B[33m";
            case "blue": return "\u001B[34m";
            case "magenta": return "\u001B[35m";
            case "cyan": return "\u001B[36m";
            case "white": return "\u001B[37m";
            case "gray":
            case "grey": return "\u001B[90m";
            default: return colorName; // 이미 ANSI 코드인 경우
        }
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