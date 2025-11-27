package com.universallogger.utils;

import com.universallogger.config.LoggerConfig;
import com.universallogger.filters.SensitiveDataFilter;

/**
 * 메시지 필터 유틸리티 클래스 (하위 호환성 유지)
 *
 * 기존 코드와의 호환성을 위해 제공됩니다.
 * 내부적으로 SensitiveDataFilter를 사용합니다.
 *
 * @deprecated 새 코드에서는 MessageFilter 인터페이스와 SensitiveDataFilter 사용을 권장합니다.
 *             DI를 통한 테스트 용이성과 확장성이 향상됩니다.
 */
public class MessageFilter {

    private final com.universallogger.filters.MessageFilter delegate;

    public MessageFilter(LoggerConfig.FiltersConfig filtersConfig) {
        this.delegate = new SensitiveDataFilter(filtersConfig);
    }

    /**
     * 메시지 필터링
     *
     * @param message 원본 메시지
     * @return 필터링된 메시지
     */
    public String filter(String message) {
        return delegate.filter(message);
    }

    /**
     * 기본 민감한 정보 필터 생성
     *
     * @return 기본 패턴이 설정된 필터
     */
    public static MessageFilter createDefaultFilter() {
        return new MessageFilter(createDefaultConfig());
    }

    private static LoggerConfig.FiltersConfig createDefaultConfig() {
        LoggerConfig.FiltersConfig filtersConfig = new LoggerConfig.FiltersConfig();
        LoggerConfig.FiltersConfig.SensitiveDataConfig sensitiveData =
            new LoggerConfig.FiltersConfig.SensitiveDataConfig();

        sensitiveData.setEnabled(true);

        // 기본 패턴들
        LoggerConfig.FiltersConfig.SensitiveDataConfig.PatternConfig[] patterns = {
            createPattern("password=\\w+", "password=***"),
            createPattern("token=\\w+", "token=***"),
            createPattern("\\d{4}-\\d{4}-\\d{4}-\\d{4}", "****-****-****-****"),
            createPattern("api[_-]?key=\\w+", "api_key=***")
        };

        sensitiveData.setPatterns(patterns);
        filtersConfig.setSensitiveData(sensitiveData);

        return filtersConfig;
    }

    private static LoggerConfig.FiltersConfig.SensitiveDataConfig.PatternConfig createPattern(
            String regex, String replace) {
        LoggerConfig.FiltersConfig.SensitiveDataConfig.PatternConfig pattern =
            new LoggerConfig.FiltersConfig.SensitiveDataConfig.PatternConfig();
        pattern.setRegex(regex);
        pattern.setReplace(replace);
        return pattern;
    }
}
