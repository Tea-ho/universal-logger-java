package com.universallogger.filters;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.filter.Filter;
import ch.qos.logback.core.spi.FilterReply;
import com.universallogger.config.LoggerConfig;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Logback Filter for sensitive data masking
 *
 * SLF4J를 통한 모든 로그에 자동으로 민감 정보 마스킹을 적용합니다.
 *
 * 설정 예시:
 * <pre>
 * filters:
 *   sensitiveData:
 *     enabled: true
 *     patterns:
 *       - regex: "password=\\w+"
 *         replace: "password=***"
 * </pre>
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

        patterns = new Pattern[patternConfigs.length];
        replacements = new String[patternConfigs.length];

        for (int i = 0; i < patternConfigs.length; i++) {
            patterns[i] = Pattern.compile(patternConfigs[i].getRegex());
            replacements[i] = patternConfigs[i].getReplace();
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
