package com.universallogger.formatters;

import ch.qos.logback.classic.encoder.PatternLayoutEncoder;

import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * 콘솔 포맷터 - 콘솔 출력용 (컬러 지원)
 * Logback의 PatternLayoutEncoder를 사용하여 표준 패턴 및 컬러 기능 지원
 *
 * 기본 패턴은 universal-logger-defaults.yml에서 로드됨
 */
public class ConsoleFormatter extends PatternLayoutEncoder {

    public ConsoleFormatter() {
        // UTF-8 인코딩 설정
        setCharset(StandardCharsets.UTF_8);
    }

    public void setColors(Map<String, String> colors) {
        // 호환성을 위해 유지
        // 색상 변경은 패턴 문자열에서 직접 지정
        // 예: %red(%level), %green(%msg) 등
    }
}
