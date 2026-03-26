package com.universallogger.config;

import java.util.Map;
import java.util.HashMap;

/**
 * Universal Logger 설정 클래스
 *
 * YAML 설정 파일(universal-logger.yml)을 Java 객체로 매핑하는 메인 설정 클래스입니다.
 * 로깅 시스템의 모든 설정을 담당하며, 다음과 같은 설정을 관리합니다:
 *
 * <ul>
 *   <li>{@link GlobalConfig} - 애플리케이션 이름, 타임존, 인코딩 등 전역 설정</li>
 *   <li>{@link OutputConfig} - 콘솔, 파일, 웹훅, 이메일 등 출력 대상별 설정</li>
 *   <li>{@link FiltersConfig} - 민감 데이터 마스킹 등 필터 설정</li>
 *   <li>{@link PerformanceConfig} - 비동기 처리, 버퍼 크기 등 성능 관련 설정</li>
 *   <li>{@link SqlConfig} - SQL 로깅 관련 설정</li>
 * </ul>
 *
 * 프로파일 기반 설정을 지원하며, {@link #merge(LoggerConfig)} 메서드를 통해
 * 기본 설정과 프로파일별 설정을 병합할 수 있습니다.
 *
 * @see com.universallogger.loaders.UniversalConfigLoader
 */
public class LoggerConfig {
    private String version;
    private GlobalConfig global = new GlobalConfig();
    private Map<String, String> levels = new HashMap<>();
    private Map<String, OutputConfig> outputs = new HashMap<>();
    private FiltersConfig filters = new FiltersConfig();
    private PerformanceConfig performance = new PerformanceConfig();
    private SqlConfig sql = new SqlConfig();

    // Getter 및 Setter
    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public GlobalConfig getGlobal() {
        return global;
    }

    public void setGlobal(GlobalConfig global) {
        this.global = global;
    }

    public Map<String, String> getLevels() {
        return levels;
    }

    public void setLevels(Map<String, String> levels) {
        this.levels = levels;
    }

    public Map<String, OutputConfig> getOutputs() {
        return outputs;
    }

    public void setOutputs(Map<String, OutputConfig> outputs) {
        this.outputs = outputs;
    }

    public FiltersConfig getFilters() {
        return filters;
    }

    public void setFilters(FiltersConfig filters) {
        this.filters = filters;
    }

    public PerformanceConfig getPerformance() {
        return performance;
    }

    public void setPerformance(PerformanceConfig performance) {
        this.performance = performance;
    }

    public SqlConfig getSql() {
        return sql;
    }

    public void setSql(SqlConfig sql) {
        this.sql = sql;
    }

    /**
     * 다른 설정을 현재 설정에 병합
     * override 설정이 우선 적용됨
     *
     * @param override 병합할 설정 (우선 적용됨)
     */
    public void merge(LoggerConfig override) {
        if (override == null) {
            return;
        }

        // 글로벌 설정 병합
        if (override.getGlobal() != null) {
            if (override.getGlobal().getAppName() != null && !override.getGlobal().getAppName().isEmpty()) {
                this.global.setAppName(override.getGlobal().getAppName());
            }
            if (override.getGlobal().getTimezone() != null && !override.getGlobal().getTimezone().isEmpty()) {
                this.global.setTimezone(override.getGlobal().getTimezone());
            }
            if (override.getGlobal().getEncoding() != null && !override.getGlobal().getEncoding().isEmpty()) {
                this.global.setEncoding(override.getGlobal().getEncoding());
            }
        }

        // 로그 레벨 병합
        if (override.getLevels() != null && !override.getLevels().isEmpty()) {
            this.levels.putAll(override.getLevels());
        }

        // 출력 설정 병합
        if (override.getOutputs() != null && !override.getOutputs().isEmpty()) {
            for (Map.Entry<String, OutputConfig> entry : override.getOutputs().entrySet()) {
                String key = entry.getKey();
                OutputConfig overrideOutput = entry.getValue();

                if (this.outputs.containsKey(key)) {
                    // 기존 출력 설정 병합
                    OutputConfig baseOutput = this.outputs.get(key);
                    mergeOutputConfig(baseOutput, overrideOutput);
                } else {
                    // 새 출력 설정 추가
                    this.outputs.put(key, overrideOutput);
                }
            }
        }

        // 필터 설정 병합
        if (override.getFilters() != null && override.getFilters().getSensitiveData() != null) {
            FiltersConfig.SensitiveDataConfig overrideSensitive = override.getFilters().getSensitiveData();
            if (overrideSensitive.getPatterns() != null && overrideSensitive.getPatterns().length > 0) {
                this.filters.getSensitiveData().setEnabled(overrideSensitive.isEnabled());
                this.filters.getSensitiveData().setPatterns(overrideSensitive.getPatterns());
            }
        }

        // 성능 설정 병합
        if (override.getPerformance() != null) {
            // 명시적으로 설정된 값만 덮어씀 (기본값 제외)
            PerformanceConfig overridePerf = override.getPerformance();
            this.performance.setAsync(overridePerf.isAsync());
            this.performance.setBufferSize(overridePerf.getBufferSize());
            this.performance.setFlushInterval(overridePerf.getFlushInterval());
            this.performance.setMaxQueueSize(overridePerf.getMaxQueueSize());
        }

        // SQL 설정 병합
        if (override.getSql() != null && override.getSql().getParameterSubstitution() != null) {
            this.sql.setParameterSubstitution(override.getSql().getParameterSubstitution());
        }
    }

    /**
     * 출력 설정 병합
     */
    private void mergeOutputConfig(OutputConfig base, OutputConfig override) {
        // enabled 플래그는 명시적으로 설정된 경우에만 덮어씀
        if (override.getEnabled() != null) {
            base.setEnabled(override.getEnabled());
        }

        if (override.getLevel() != null && !override.getLevel().isEmpty()) {
            base.setLevel(override.getLevel());
        }
        if (override.getFormat() != null && !override.getFormat().isEmpty()) {
            base.setFormat(override.getFormat());
        }
        if (override.getPath() != null && !override.getPath().isEmpty()) {
            base.setPath(override.getPath());
        }
        if (override.getColors() != null && !override.getColors().isEmpty()) {
            base.getColors().putAll(override.getColors());
        }

        // 로테이션 설정 병합
        if (override.getRotation() != null) {
            RotationConfig overrideRotation = override.getRotation();
            if (overrideRotation.getMaxSize() != null && !overrideRotation.getMaxSize().isEmpty()) {
                base.getRotation().setMaxSize(overrideRotation.getMaxSize());
            }
            if (overrideRotation.getMaxFiles() > 0) {
                base.getRotation().setMaxFiles(overrideRotation.getMaxFiles());
            }
            if (overrideRotation.getCompress() != null) {
                base.getRotation().setCompress(overrideRotation.getCompress());
            }
        }

        // 웹훅 설정 병합
        if (override.getWebhook() != null && override.getWebhook().getUrl() != null) {
            base.setWebhook(override.getWebhook());
        }

        // 이메일 설정 병합
        if (override.getEmail() != null && override.getEmail().getSmtp() != null) {
            base.setEmail(override.getEmail());
        }
    }

    /**
     * 방어적 복사 - 이 설정의 독립적인 복사본 생성
     *
     * 원본 설정이 변경되어도 복사본에 영향을 주지 않습니다.
     * LoggerInstance 등에서 외부로부터 받은 설정을 안전하게 보관할 때 사용.
     *
     * @return 이 설정의 깊은 복사본
     */
    public LoggerConfig copy() {
        LoggerConfig copy = new LoggerConfig();

        copy.version = this.version;

        // 글로벌 설정 복사
        if (this.global != null) {
            GlobalConfig globalCopy = new GlobalConfig();
            globalCopy.setAppName(this.global.getAppName());
            globalCopy.setTimezone(this.global.getTimezone());
            globalCopy.setEncoding(this.global.getEncoding());
            copy.global = globalCopy;
        }

        // 로그 레벨 복사
        copy.levels = new HashMap<>(this.levels);

        // 출력 설정 깊은 복사
        copy.outputs = new HashMap<>();
        for (Map.Entry<String, OutputConfig> entry : this.outputs.entrySet()) {
            copy.outputs.put(entry.getKey(), copyOutputConfig(entry.getValue()));
        }

        // 필터 설정 복사
        if (this.filters != null) {
            FiltersConfig filtersCopy = new FiltersConfig();
            if (this.filters.getSensitiveData() != null) {
                FiltersConfig.SensitiveDataConfig sdCopy =
                    new FiltersConfig.SensitiveDataConfig();
                sdCopy.setEnabled(this.filters.getSensitiveData().isEnabled());
                if (this.filters.getSensitiveData().getPatterns() != null) {
                    FiltersConfig.SensitiveDataConfig.PatternConfig[] patterns =
                        this.filters.getSensitiveData().getPatterns();
                    FiltersConfig.SensitiveDataConfig.PatternConfig[] patternsCopy =
                        new FiltersConfig.SensitiveDataConfig.PatternConfig[patterns.length];
                    for (int i = 0; i < patterns.length; i++) {
                        patternsCopy[i] = new FiltersConfig.SensitiveDataConfig.PatternConfig();
                        patternsCopy[i].setRegex(patterns[i].getRegex());
                        patternsCopy[i].setReplace(patterns[i].getReplace());
                    }
                    sdCopy.setPatterns(patternsCopy);
                }
                filtersCopy.setSensitiveData(sdCopy);
            }
            copy.filters = filtersCopy;
        }

        // 성능 설정 복사
        if (this.performance != null) {
            PerformanceConfig perfCopy = new PerformanceConfig();
            perfCopy.setAsync(this.performance.isAsync());
            perfCopy.setBufferSize(this.performance.getBufferSize());
            perfCopy.setFlushInterval(this.performance.getFlushInterval());
            perfCopy.setMaxQueueSize(this.performance.getMaxQueueSize());
            copy.performance = perfCopy;
        }

        // SQL 설정 복사
        if (this.sql != null) {
            SqlConfig sqlCopy = new SqlConfig();
            sqlCopy.setParameterSubstitution(this.sql.isParameterSubstitution());
            copy.sql = sqlCopy;
        }

        return copy;
    }

    /**
     * OutputConfig 깊은 복사
     */
    private OutputConfig copyOutputConfig(OutputConfig original) {
        if (original == null) return null;

        OutputConfig copy = new OutputConfig();
        copy.setEnabled(original.isEnabled());
        copy.setLevel(original.getLevel());
        copy.setFormat(original.getFormat());
        copy.setPath(original.getPath());

        // 색상 설정 복사
        if (original.getColors() != null) {
            copy.setColors(new HashMap<>(original.getColors()));
        }

        // 로테이션 설정 복사
        if (original.getRotation() != null) {
            RotationConfig rotCopy = new RotationConfig();
            rotCopy.setMaxSize(original.getRotation().getMaxSize());
            rotCopy.setMaxFiles(original.getRotation().getMaxFiles());
            rotCopy.setCompress(original.getRotation().getCompress());
            copy.setRotation(rotCopy);
        }

        // 웹훅 설정 복사
        if (original.getWebhook() != null) {
            WebhookConfig whCopy = new WebhookConfig();
            whCopy.setUrl(original.getWebhook().getUrl());
            whCopy.setFormat(original.getWebhook().getFormat());
            copy.setWebhook(whCopy);
        }

        // 이메일 설정 복사
        if (original.getEmail() != null) {
            EmailConfig emailCopy = new EmailConfig();
            if (original.getEmail().getSmtp() != null) {
                EmailConfig.SmtpConfig smtpCopy = new EmailConfig.SmtpConfig();
                smtpCopy.setHost(original.getEmail().getSmtp().getHost());
                smtpCopy.setPort(original.getEmail().getSmtp().getPort());
                smtpCopy.setUsername(original.getEmail().getSmtp().getUsername());
                smtpCopy.setPassword(original.getEmail().getSmtp().getPassword());
                emailCopy.setSmtp(smtpCopy);
            }
            if (original.getEmail().getTo() != null) {
                emailCopy.setTo(original.getEmail().getTo().clone());
            }
            emailCopy.setSubject(original.getEmail().getSubject());
            copy.setEmail(emailCopy);
        }

        return copy;
    }

    /**
     * 글로벌 설정
     * 기본값은 universal-logger-defaults.yml에서 로드됨
     */
    public static class GlobalConfig {
        private String appName;
        private String timezone;
        private String encoding;

        public String getAppName() {
            return appName;
        }

        public void setAppName(String appName) {
            this.appName = appName;
        }

        public String getTimezone() {
            return timezone;
        }

        public void setTimezone(String timezone) {
            this.timezone = timezone;
        }

        public String getEncoding() {
            return encoding;
        }

        public void setEncoding(String encoding) {
            this.encoding = encoding;
        }
    }

    /**
     * 출력 설정
     * 기본값은 universal-logger-defaults.yml에서 로드됨
     */
    public static class OutputConfig {
        private Boolean enabled;
        private String level;
        private String format;
        private String path;
        private Map<String, String> colors = new HashMap<>();
        private RotationConfig rotation = new RotationConfig();
        private WebhookConfig webhook = new WebhookConfig();
        private EmailConfig email = new EmailConfig();

        // Getter 및 Setter
        public boolean isEnabled() {
            return enabled != null && enabled;
        }

        public Boolean getEnabled() {
            return enabled;
        }

        public void setEnabled(Boolean enabled) {
            this.enabled = enabled;
        }

        public String getLevel() {
            return level;
        }

        public void setLevel(String level) {
            this.level = level;
        }

        public String getFormat() {
            return format;
        }

        public void setFormat(String format) {
            this.format = format;
        }

        public String getPath() {
            return path;
        }

        public void setPath(String path) {
            this.path = path;
        }

        public Map<String, String> getColors() {
            return colors;
        }

        public void setColors(Map<String, String> colors) {
            this.colors = colors;
        }

        public RotationConfig getRotation() {
            return rotation;
        }

        public void setRotation(RotationConfig rotation) {
            this.rotation = rotation;
        }

        public WebhookConfig getWebhook() {
            return webhook;
        }

        public void setWebhook(WebhookConfig webhook) {
            this.webhook = webhook;
        }

        public EmailConfig getEmail() {
            return email;
        }

        public void setEmail(EmailConfig email) {
            this.email = email;
        }
    }

    /**
     * 로테이션 설정
     * 기본값은 universal-logger-defaults.yml에서 로드됨
     */
    public static class RotationConfig {
        private String maxSize;
        private int maxFiles;
        private Boolean compress;

        public String getMaxSize() {
            return maxSize;
        }

        public void setMaxSize(String maxSize) {
            this.maxSize = maxSize;
        }

        public int getMaxFiles() {
            return maxFiles;
        }

        public void setMaxFiles(int maxFiles) {
            this.maxFiles = maxFiles;
        }

        public boolean isCompress() {
            return compress != null ? compress : false;
        }

        public Boolean getCompress() {
            return compress;
        }

        public void setCompress(Boolean compress) {
            this.compress = compress;
        }
    }

    /**
     * 웹훅 설정
     * 기본값은 universal-logger-defaults.yml에서 로드됨
     */
    public static class WebhookConfig {
        private String url;
        private String format;
        private AlertPolicyConfig alertPolicy;

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public String getFormat() {
            return format;
        }

        public void setFormat(String format) {
            this.format = format;
        }

        public AlertPolicyConfig getAlertPolicy() {
            return alertPolicy;
        }

        public void setAlertPolicy(AlertPolicyConfig alertPolicy) {
            this.alertPolicy = alertPolicy;
        }

        /**
         * 웹훅 알림 정책 설정
         * Rate Limiting과 Deduplication을 통해 알림 피로도를 줄인다.
         */
        public static class AlertPolicyConfig {
            private int rateLimit = 10;              // 분당 최대 전송 수
            private int deduplicationSeconds = 300;  // 동일 에러 무시 시간(초), 0이면 비활성화

            public int getRateLimit() {
                return rateLimit;
            }

            public void setRateLimit(int rateLimit) {
                this.rateLimit = rateLimit;
            }

            public int getDeduplicationSeconds() {
                return deduplicationSeconds;
            }

            public void setDeduplicationSeconds(int deduplicationSeconds) {
                this.deduplicationSeconds = deduplicationSeconds;
            }
        }
    }

    /**
     * 이메일 설정
     * 기본값은 universal-logger-defaults.yml에서 로드됨
     */
    public static class EmailConfig {
        private SmtpConfig smtp = new SmtpConfig();
        private String from;
        private String[] to;
        private String subject;

        public SmtpConfig getSmtp() {
            return smtp;
        }

        public void setSmtp(SmtpConfig smtp) {
            this.smtp = smtp;
        }

        public String getFrom() {
            return from;
        }

        public void setFrom(String from) {
            this.from = from;
        }

        public String[] getTo() {
            return to;
        }

        public void setTo(String[] to) {
            this.to = to;
        }

        public String getSubject() {
            return subject;
        }

        public void setSubject(String subject) {
            this.subject = subject;
        }

        public static class SmtpConfig {
            private String host;
            private int port;
            private String username;
            private String password;

            public String getHost() {
                return host;
            }

            public void setHost(String host) {
                this.host = host;
            }

            public int getPort() {
                return port;
            }

            public void setPort(int port) {
                this.port = port;
            }

            public String getUsername() {
                return username;
            }

            public void setUsername(String username) {
                this.username = username;
            }

            public String getPassword() {
                return password;
            }

            public void setPassword(String password) {
                this.password = password;
            }
        }
    }

    /**
     * 필터 설정
     * 기본값은 universal-logger-defaults.yml에서 로드됨
     */
    public static class FiltersConfig {
        private SensitiveDataConfig sensitiveData = new SensitiveDataConfig();

        public SensitiveDataConfig getSensitiveData() {
            return sensitiveData;
        }

        public void setSensitiveData(SensitiveDataConfig sensitiveData) {
            this.sensitiveData = sensitiveData;
        }

        public static class SensitiveDataConfig {
            private boolean enabled;
            private PatternConfig[] patterns;

            public boolean isEnabled() {
                return enabled;
            }

            public void setEnabled(boolean enabled) {
                this.enabled = enabled;
            }

            public PatternConfig[] getPatterns() {
                return patterns;
            }

            public void setPatterns(PatternConfig[] patterns) {
                this.patterns = patterns;
            }

            public static class PatternConfig {
                private String regex;
                private String replace;

                public String getRegex() {
                    return regex;
                }

                public void setRegex(String regex) {
                    this.regex = regex;
                }

                public String getReplace() {
                    return replace;
                }

                public void setReplace(String replace) {
                    this.replace = replace;
                }
            }
        }
    }

    /**
     * 성능 설정
     * 기본값은 universal-logger-defaults.yml에서 로드됨
     */
    public static class PerformanceConfig {
        private boolean async;
        private int bufferSize;
        private int flushInterval;
        private int maxQueueSize;

        public boolean isAsync() {
            return async;
        }

        public void setAsync(boolean async) {
            this.async = async;
        }

        public int getBufferSize() {
            return bufferSize;
        }

        public void setBufferSize(int bufferSize) {
            this.bufferSize = bufferSize;
        }

        public int getFlushInterval() {
            return flushInterval;
        }

        public void setFlushInterval(int flushInterval) {
            this.flushInterval = flushInterval;
        }

        public int getMaxQueueSize() {
            return maxQueueSize;
        }

        public void setMaxQueueSize(int maxQueueSize) {
            this.maxQueueSize = maxQueueSize;
        }
    }

    /**
     * SQL 로깅 설정
     * 기본값은 universal-logger-defaults.yml에서 로드됨
     */
    public static class SqlConfig {
        private Boolean parameterSubstitution;

        public boolean isParameterSubstitution() {
            return parameterSubstitution != null ? parameterSubstitution : false;
        }

        public Boolean getParameterSubstitution() {
            return parameterSubstitution;
        }

        public void setParameterSubstitution(Boolean parameterSubstitution) {
            this.parameterSubstitution = parameterSubstitution;
        }
    }
}