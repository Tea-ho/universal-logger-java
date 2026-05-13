package io.universallogger.filters;

import io.universallogger.config.LoggerConfig;

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import java.util.regex.Matcher;

/**
 * 민감한 데이터 필터링 구현체
 *
 * 정규식 패턴을 사용하여 민감한 정보를 마스킹합니다.
 * (비밀번호, 토큰, 신용카드 번호, API 키 등)
 *
 * @deprecated 더 이상 사용되지 않습니다. {@link SensitiveDataLogbackFilter}를 사용하세요.
 * @see SensitiveDataLogbackFilter
 */
@Deprecated
public class SensitiveDataFilter implements MessageFilter {

    private final Pattern[] patterns;
    private final String[] replacements;

    /**
     * 필터 설정으로 생성
     *
     * @param filtersConfig 필터 설정
     */
    public SensitiveDataFilter(LoggerConfig.FiltersConfig filtersConfig) {
        if (filtersConfig == null ||
            filtersConfig.getSensitiveData() == null ||
            !filtersConfig.getSensitiveData().isEnabled()) {
            patterns = new Pattern[0];
            replacements = new String[0];
            return;
        }

        LoggerConfig.FiltersConfig.SensitiveDataConfig.PatternConfig[] patternConfigs =
            filtersConfig.getSensitiveData().getPatterns();

        if (patternConfigs == null || patternConfigs.length == 0) {
            patterns = new Pattern[0];
            replacements = new String[0];
            return;
        }

        // 유효한 패턴만 수집
        java.util.List<Pattern> validPatterns = new java.util.ArrayList<>();
        java.util.List<String> validReplacements = new java.util.ArrayList<>();

        for (int i = 0; i < patternConfigs.length; i++) {
            String regex = patternConfigs[i].getRegex();
            if (regex == null || regex.isEmpty()) {
                continue;
            }
            // 패턴 복잡도 검증: 길이 제한 및 중첩 반복 감지 (ReDoS 방지)
            if (regex.length() > 200) {
                continue; // 지나치게 긴 패턴 거부
            }
            try {
                validPatterns.add(Pattern.compile(regex));
                validReplacements.add(patternConfigs[i].getReplace() != null ? patternConfigs[i].getReplace() : "***");
            } catch (PatternSyntaxException e) {
                // 잘못된 정규식 패턴은 무시
            }
        }

        patterns = validPatterns.toArray(new Pattern[0]);
        replacements = validReplacements.toArray(new String[0]);
    }

    @Override
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
     *
     * @return 기본 패턴이 설정된 필터
     */
    public static SensitiveDataFilter createDefaultFilter() {
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

        return new SensitiveDataFilter(filtersConfig);
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
