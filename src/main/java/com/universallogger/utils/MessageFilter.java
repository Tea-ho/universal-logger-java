package com.universallogger.utils;

import com.universallogger.config.LoggerConfig;

import java.util.regex.Pattern;
import java.util.regex.Matcher;

/**
 * 메시지 필터 - 민감한 정보 마스킹 등
 */
public class MessageFilter {

    private Pattern[] patterns;
    private String[] replacements;

    public MessageFilter(LoggerConfig.FiltersConfig filtersConfig) {
        setupFilters(filtersConfig);
    }

    /**
     * 필터 설정
     */
    private void setupFilters(LoggerConfig.FiltersConfig filtersConfig) {
        if (filtersConfig == null ||
            filtersConfig.getSensitiveData() == null ||
            !filtersConfig.getSensitiveData().isEnabled()) {
            patterns = new Pattern[0];
            replacements = new String[0];
            return;
        }

        LoggerConfig.FiltersConfig.SensitiveDataConfig.PatternConfig[] patternConfigs =
            filtersConfig.getSensitiveData().getPatterns();

        patterns = new Pattern[patternConfigs.length];
        replacements = new String[patternConfigs.length];

        for (int i = 0; i < patternConfigs.length; i++) {
            patterns[i] = Pattern.compile(patternConfigs[i].getRegex());
            replacements[i] = patternConfigs[i].getReplace();
        }
    }

    /**
     * 메시지 필터링
     */
    public String filter(String message) {
        if (message == null || patterns.length == 0) {
            return message;
        }

        String filteredMessage = message;

        for (int i = 0; i < patterns.length; i++) {
            Matcher matcher = patterns[i].matcher(filteredMessage);
            filteredMessage = matcher.replaceAll(replacements[i]);
        }

        return filteredMessage;
    }

    /**
     * 기본 민감한 정보 필터 생성
     */
    public static MessageFilter createDefaultFilter() {
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

        return new MessageFilter(filtersConfig);
    }

    /**
     * 패턴 설정 생성 헬퍼
     */
    private static LoggerConfig.FiltersConfig.SensitiveDataConfig.PatternConfig createPattern(
            String regex, String replace) {
        LoggerConfig.FiltersConfig.SensitiveDataConfig.PatternConfig pattern =
            new LoggerConfig.FiltersConfig.SensitiveDataConfig.PatternConfig();
        pattern.setRegex(regex);
        pattern.setReplace(replace);
        return pattern;
    }
}