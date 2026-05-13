package io.universallogger.logback.formatters;

import ch.qos.logback.classic.encoder.PatternLayoutEncoder;

import java.nio.charset.StandardCharsets;

/**
 * 파일 출력용 포맷터
 *
 * Logback의 PatternLayoutEncoder를 확장하여 파일 출력을 위한 패턴을 지원합니다.
 * 기본 패턴은 universal-logger-defaults.yml에서 로드됩니다.
 *
 * <p>주요 기능:</p>
 * <ul>
 *   <li>UTF-8 인코딩 기본 설정</li>
 *   <li>Logback 표준 패턴 문법 지원</li>
 *   <li>파일 롤링 정책과 호환</li>
 * </ul>
 *
 * @see ConsoleFormatter
 */
public class FileFormatter extends PatternLayoutEncoder {

    public FileFormatter() {
        // UTF-8 인코딩 설정
        setCharset(StandardCharsets.UTF_8);
    }
}
