package com.universallogger.formatters;

import ch.qos.logback.classic.encoder.PatternLayoutEncoder;

import java.nio.charset.StandardCharsets;

/**
 * 파일 포맷터 - 파일 출력용
 * Logback의 PatternLayoutEncoder를 사용하여 표준 패턴 지원
 */
public class FileFormatter extends PatternLayoutEncoder {

    private String defaultPattern = "%d{yyyy-MM-dd HH:mm:ss} %-5level [%thread] %logger{50} %msg%n";

    public FileFormatter() {
        // UTF-8 인코딩 설정
        setCharset(StandardCharsets.UTF_8);
    }

    @Override
    public void setPattern(String pattern) {
        // 사용자 패턴이 제공되면 그대로 사용, 없으면 기본 패턴 사용
        if (pattern != null && !pattern.trim().isEmpty()) {
            super.setPattern(pattern);
        } else {
            super.setPattern(defaultPattern);
        }
    }

    @Override
    public void start() {
        // 패턴이 설정되지 않았으면 기본 패턴 사용
        if (getPattern() == null || getPattern().trim().isEmpty()) {
            super.setPattern(defaultPattern);
        }
        super.start();
    }

    /**
     * 기본 패턴 반환 (문서화/참고용)
     */
    public String getDefaultPattern() {
        return defaultPattern;
    }
}
