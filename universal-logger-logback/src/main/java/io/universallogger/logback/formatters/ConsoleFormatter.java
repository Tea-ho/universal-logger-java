package io.universallogger.logback.formatters;

import ch.qos.logback.classic.encoder.PatternLayoutEncoder;

import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * 콘솔 출력용 포맷터 (컬러 지원)
 *
 * Logback의 PatternLayoutEncoder를 확장하여 표준 패턴 및 컬러 기능을 지원합니다.
 * 기본 패턴은 universal-logger-defaults.yml에서 로드됩니다.
 *
 * <p>주요 기능:</p>
 * <ul>
 *   <li>UTF-8 인코딩 기본 설정</li>
 *   <li>Logback 표준 패턴 문법 지원</li>
 *   <li>ANSI 컬러 코드 지원 (%red, %green 등)</li>
 * </ul>
 *
 * @see FileFormatter
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
