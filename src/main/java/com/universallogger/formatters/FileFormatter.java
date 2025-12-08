package com.universallogger.formatters;

import ch.qos.logback.classic.encoder.PatternLayoutEncoder;

import java.nio.charset.StandardCharsets;

/**
 * 파일 포맷터 - 파일 출력용
 * Logback의 PatternLayoutEncoder를 사용하여 표준 패턴 지원
 *
 * 기본 패턴은 universal-logger-defaults.yml에서 로드됨
 */
public class FileFormatter extends PatternLayoutEncoder {

    public FileFormatter() {
        // UTF-8 인코딩 설정
        setCharset(StandardCharsets.UTF_8);
    }
}
