package com.universallogger.formatters;

import ch.qos.logback.classic.encoder.PatternLayoutEncoder;

/**
 * 커스텀 포맷터 - 파일 출력용
 * Logback의 PatternLayoutEncoder를 사용하여 표준 패턴 지원
 */
public class CustomFormatter extends PatternLayoutEncoder {

    private String customPattern = "%d{yyyy-MM-dd HH:mm:ss} %-5level [%thread] [%logger{50}] %msg%n";

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
            .replace("{level}", "%-5level")
            .replace("{thread}", "[%thread]")
            .replace("{logger}", "[%logger{50}]")
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
}
