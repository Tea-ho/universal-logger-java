package com.universallogger.logback;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.filter.Filter;
import ch.qos.logback.core.spi.FilterReply;
import com.universallogger.config.LoggerConfig;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * 민감 정보 마스킹을 위한 Logback 필터
 *
 * SLF4J를 통한 모든 로그에 자동으로 민감 정보 마스킹을 적용합니다.
 * 정규표현식 패턴을 사용하여 비밀번호, 신용카드 번호 등의 민감 정보를 마스킹합니다.
 *
 * <p>설정 예시 (universal-logger.yml):</p>
 * <pre>{@code
 * filters:
 *   sensitiveData:
 *     enabled: true
 *     patterns:
 *       - regex: "password=\\w+"
 *         replace: "password=***"
 *       - regex: "\\d{4}-\\d{4}-\\d{4}-\\d{4}"
 *         replace: "****-****-****-****"
 * }</pre>
 *
 * @see LoggerConfig.FiltersConfig.SensitiveDataConfig
 */
public class SensitiveDataLogbackFilter extends Filter<ILoggingEvent> {

    private Pattern[] patterns;
    private String[] replacements;
    private boolean enabled = false;

    /**
     * 필터 설정으로 초기화
     */
    public void configure(LoggerConfig.FiltersConfig filtersConfig) {
        if (filtersConfig == null ||
            filtersConfig.getSensitiveData() == null ||
            !filtersConfig.getSensitiveData().isEnabled()) {
            this.enabled = false;
            return;
        }

        this.enabled = true;

        LoggerConfig.FiltersConfig.SensitiveDataConfig.PatternConfig[] patternConfigs =
            filtersConfig.getSensitiveData().getPatterns();

        if (patternConfigs == null || patternConfigs.length == 0) {
            this.enabled = false;
            return;
        }

        // 유효한 패턴만 수집 (ReDoS 방지)
        java.util.List<Pattern> validPatterns = new java.util.ArrayList<>();
        java.util.List<String> validReplacements = new java.util.ArrayList<>();

        for (int i = 0; i < patternConfigs.length; i++) {
            String regex = patternConfigs[i].getRegex();
            if (regex == null || regex.isEmpty() || regex.length() > 200) {
                continue;
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

        if (patterns.length == 0) {
            this.enabled = false;
        }
    }

    @Override
    public FilterReply decide(ILoggingEvent event) {
        if (!enabled || patterns == null || patterns.length == 0) {
            return FilterReply.NEUTRAL;
        }

        // 메시지 필터링은 Appender에서 처리하도록 변경
        // 이 Filter는 활성화 여부만 확인
        return FilterReply.NEUTRAL;
    }

    /**
     * 메시지 필터링 적용
     */
    public String filterMessage(String message) {
        if (!enabled || message == null || patterns == null) {
            return message;
        }

        String filteredMessage = message;

        for (int i = 0; i < patterns.length; i++) {
            Matcher matcher = patterns[i].matcher(filteredMessage);
            filteredMessage = matcher.replaceAll(replacements[i]);
        }

        return filteredMessage;
    }

    public boolean isEnabled() {
        return enabled;
    }
}
