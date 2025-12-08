package com.universallogger.formatters;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.PatternLayout;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.pattern.color.*;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.HashMap;

/**
 * 콘솔 포맷터 - 콘솔 출력용 (컬러 지원)
 * Logback의 PatternLayoutEncoder를 사용하여 표준 패턴 및 컬러 기능 지원
 */
public class ConsoleFormatter extends PatternLayoutEncoder {

    private static final Map<String, String> DEFAULT_COLORS = new HashMap<>();
    static {
        DEFAULT_COLORS.put("DEBUG", "cyan");
        DEFAULT_COLORS.put("INFO", "green");
        DEFAULT_COLORS.put("WARN", "yellow");
        DEFAULT_COLORS.put("ERROR", "red");
        DEFAULT_COLORS.put("CRITICAL", "magenta");
    }

    private String defaultPattern = "%d{yyyy-MM-dd HH:mm:ss} %highlight(%-5level) %cyan([%thread]) %boldMagenta(%logger{50}) %msg%n";

    public ConsoleFormatter() {
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

    public void setColors(Map<String, String> colors) {
        // 호환성을 위해 유지
        // 색상 변경은 패턴 문자열에서 직접 지정
        // 예: %red(%level), %green(%msg) 등
    }
}
