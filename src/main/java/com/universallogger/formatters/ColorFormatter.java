package com.universallogger.formatters;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.PatternLayout;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.pattern.color.*;

import java.util.Map;
import java.util.HashMap;

/**
 * 컬러 포맷터 - 콘솔 출력용
 * Logback의 PatternLayoutEncoder를 사용하여 표준 패턴 및 컬러 기능 지원
 */
public class ColorFormatter extends PatternLayoutEncoder {

    private static final Map<String, String> DEFAULT_COLORS = new HashMap<>();
    static {
        DEFAULT_COLORS.put("DEBUG", "cyan");
        DEFAULT_COLORS.put("INFO", "green");
        DEFAULT_COLORS.put("WARN", "yellow");
        DEFAULT_COLORS.put("ERROR", "red");
        DEFAULT_COLORS.put("CRITICAL", "magenta");
    }

    private String customPattern = "%d{yyyy-MM-dd HH:mm:ss} %highlight(%-5level) %cyan([%thread]) %boldMagenta([%logger{50}]) %msg%n";

    @Override
    public void setPattern(String pattern) {
        if (pattern != null && !pattern.trim().isEmpty()) {
            this.customPattern = convertPatternToLogback(pattern);
        }
        super.setPattern(this.customPattern);
    }

    @Override
    public void start() {
        // 패턴이 설정되지 않았으면 기본 패턴 사용
        if (getPattern() == null || getPattern().trim().isEmpty()) {
            super.setPattern(this.customPattern);
        }
        super.start();
    }

    /**
     * Universal Logger 패턴을 Logback 패턴으로 변환
     */
    private String convertPatternToLogback(String pattern) {
        String converted = pattern
            .replace("{timestamp}", "%d{yyyy-MM-dd HH:mm:ss}")
            .replace("{level}", "%highlight(%-5level)")
            .replace("{thread}", "%cyan([%thread])")
            .replace("{logger}", "%boldMagenta([%logger{50}])")
            .replace("{class}", "%class")
            .replace("{method}", "%method")
            .replace("{line}", "%line")
            .replace("{message}", "%msg")
            .replace("{exception}", "%ex");

        // 줄바꿈 추가 (없으면)
        if (!converted.endsWith("%n") && !converted.endsWith("\n")) {
            converted += "%n";
        }

        return converted;
    }

    public void setColors(Map<String, String> colors) {
        // Logback의 내장 컬러 기능을 사용하므로,
        // 사용자 정의 색상은 패턴에서 직접 지정
        // 이 메서드는 호환성을 위해 유지
    }
}
