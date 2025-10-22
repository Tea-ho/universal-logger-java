package com.universallogger.config;

import java.util.Map;
import java.util.HashMap;

/**
 * 로거 설정 클래스
 */
public class LoggerConfig {
    private String version;
    private GlobalConfig global = new GlobalConfig();
    private Map<String, String> levels = new HashMap<>();
    private Map<String, OutputConfig> outputs = new HashMap<>();
    private FiltersConfig filters = new FiltersConfig();
    private PerformanceConfig performance = new PerformanceConfig();

    // Getters and Setters
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

    /**
     * Merge another configuration into this one
     * The override configuration takes precedence
     *
     * @param override Configuration to merge (takes precedence)
     */
    public void merge(LoggerConfig override) {
        if (override == null) {
            return;
        }

        // Merge global config
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

        // Merge levels
        if (override.getLevels() != null && !override.getLevels().isEmpty()) {
            this.levels.putAll(override.getLevels());
        }

        // Merge outputs
        if (override.getOutputs() != null && !override.getOutputs().isEmpty()) {
            for (Map.Entry<String, OutputConfig> entry : override.getOutputs().entrySet()) {
                String key = entry.getKey();
                OutputConfig overrideOutput = entry.getValue();

                if (this.outputs.containsKey(key)) {
                    // Merge existing output config
                    OutputConfig baseOutput = this.outputs.get(key);
                    mergeOutputConfig(baseOutput, overrideOutput);
                } else {
                    // Add new output config
                    this.outputs.put(key, overrideOutput);
                }
            }
        }

        // Merge filters
        if (override.getFilters() != null && override.getFilters().getSensitiveData() != null) {
            FiltersConfig.SensitiveDataConfig overrideSensitive = override.getFilters().getSensitiveData();
            if (overrideSensitive.getPatterns() != null && overrideSensitive.getPatterns().length > 0) {
                this.filters.getSensitiveData().setEnabled(overrideSensitive.isEnabled());
                this.filters.getSensitiveData().setPatterns(overrideSensitive.getPatterns());
            }
        }

        // Merge performance
        if (override.getPerformance() != null) {
            // Only override if values are explicitly set (not defaults)
            PerformanceConfig overridePerf = override.getPerformance();
            this.performance.setAsync(overridePerf.isAsync());
            this.performance.setBufferSize(overridePerf.getBufferSize());
            this.performance.setFlushInterval(overridePerf.getFlushInterval());
            this.performance.setMaxQueueSize(overridePerf.getMaxQueueSize());
        }
    }

    /**
     * Merge output configurations
     */
    private void mergeOutputConfig(OutputConfig base, OutputConfig override) {
        // Always override enabled flag
        base.setEnabled(override.isEnabled());

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

        // Merge rotation
        if (override.getRotation() != null) {
            RotationConfig overrideRotation = override.getRotation();
            if (overrideRotation.getMaxSize() != null && !overrideRotation.getMaxSize().isEmpty()) {
                base.getRotation().setMaxSize(overrideRotation.getMaxSize());
            }
            if (overrideRotation.getMaxFiles() > 0) {
                base.getRotation().setMaxFiles(overrideRotation.getMaxFiles());
            }
            base.getRotation().setCompress(overrideRotation.isCompress());
        }

        // Merge database config
        if (override.getDatabase() != null && override.getDatabase().getType() != null) {
            base.setDatabase(override.getDatabase());
        }

        // Merge webhook config
        if (override.getWebhook() != null && override.getWebhook().getUrl() != null) {
            base.setWebhook(override.getWebhook());
        }

        // Merge email config
        if (override.getEmail() != null && override.getEmail().getSmtp() != null) {
            base.setEmail(override.getEmail());
        }
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
        private boolean enabled;
        private String level;
        private String format;
        private String path;
        private Map<String, String> colors = new HashMap<>();
        private RotationConfig rotation = new RotationConfig();
        private DatabaseConfig database = new DatabaseConfig();
        private WebhookConfig webhook = new WebhookConfig();
        private EmailConfig email = new EmailConfig();

        // Getters and Setters
        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
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

        public DatabaseConfig getDatabase() {
            return database;
        }

        public void setDatabase(DatabaseConfig database) {
            this.database = database;
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
        private boolean compress;

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
            return compress;
        }

        public void setCompress(boolean compress) {
            this.compress = compress;
        }
    }

    /**
     * 데이터베이스 설정
     */
    public static class DatabaseConfig {
        private String type;
        private String host;
        private int port;
        private String database;
        private String table;
        private String username;
        private String password;

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

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

        public String getDatabase() {
            return database;
        }

        public void setDatabase(String database) {
            this.database = database;
        }

        public String getTable() {
            return table;
        }

        public void setTable(String table) {
            this.table = table;
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

    /**
     * 웹훅 설정
     * 기본값은 universal-logger-defaults.yml에서 로드됨
     */
    public static class WebhookConfig {
        private String url;
        private String format;

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
    }

    /**
     * 이메일 설정
     * 기본값은 universal-logger-defaults.yml에서 로드됨
     */
    public static class EmailConfig {
        private SmtpConfig smtp = new SmtpConfig();
        private String[] to;
        private String subject;

        public SmtpConfig getSmtp() {
            return smtp;
        }

        public void setSmtp(SmtpConfig smtp) {
            this.smtp = smtp;
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
}