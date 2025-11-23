package com.universallogger.config;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * LoggerConfigBuilder - LoggerConfig를 프로그래매틱하게 생성하기 위한 빌더
 *
 * YAML/JSON 설정 파일 대신 코드에서 직접 설정을 구성할 때 사용.
 * Fluent API를 제공하여 가독성 높은 설정 코드 작성 가능.
 *
 * 사용법:
 * <pre>
 * LoggerConfig config = LoggerConfigBuilder.create()
 *     .appName("my-app")
 *     .rootLevel("DEBUG")
 *     .console()
 *         .enabled(true)
 *         .level("INFO")
 *         .format("[%d{HH:mm:ss}] %level %msg%n")
 *         .and()
 *     .file()
 *         .enabled(true)
 *         .path("logs/app.log")
 *         .maxSize("10MB")
 *         .maxFiles(30)
 *         .and()
 *     .build();
 * </pre>
 */
public class LoggerConfigBuilder {

    private String version = "1.0";
    private String appName = "application";
    private String timezone = "Asia/Seoul";
    private String encoding = "UTF-8";
    private final Map<String, String> levels = new HashMap<>();
    private final Map<String, LoggerConfig.OutputConfig> outputs = new HashMap<>();
    private boolean asyncEnabled = false;
    private int bufferSize = 8192;
    private int flushInterval = 1000;
    private int maxQueueSize = 10000;

    // 필터 설정
    private boolean sensitiveDataEnabled = false;
    private LoggerConfig.FiltersConfig.SensitiveDataConfig.PatternConfig[] sensitivePatterns;

    private LoggerConfigBuilder() {
        levels.put("root", "INFO");
    }

    /**
     * 빌더 생성
     */
    public static LoggerConfigBuilder create() {
        return new LoggerConfigBuilder();
    }

    // ========== Global Settings ==========

    /**
     * 설정 버전
     */
    public LoggerConfigBuilder version(String version) {
        this.version = version;
        return this;
    }

    /**
     * 애플리케이션 이름
     */
    public LoggerConfigBuilder appName(String appName) {
        this.appName = appName;
        return this;
    }

    /**
     * 타임존
     */
    public LoggerConfigBuilder timezone(String timezone) {
        this.timezone = timezone;
        return this;
    }

    /**
     * 인코딩
     */
    public LoggerConfigBuilder encoding(String encoding) {
        this.encoding = encoding;
        return this;
    }

    // ========== Level Settings ==========

    /**
     * 루트 로그 레벨
     */
    public LoggerConfigBuilder rootLevel(String level) {
        this.levels.put("root", level);
        return this;
    }

    /**
     * 모듈별 로그 레벨
     */
    public LoggerConfigBuilder level(String module, String level) {
        this.levels.put(module, level);
        return this;
    }

    // ========== Output Builders ==========

    /**
     * 콘솔 출력 설정 빌더
     */
    public ConsoleOutputBuilder console() {
        return new ConsoleOutputBuilder(this);
    }

    /**
     * 파일 출력 설정 빌더
     */
    public FileOutputBuilder file() {
        return new FileOutputBuilder(this, "file");
    }

    /**
     * 에러 파일 출력 설정 빌더
     */
    public FileOutputBuilder errorFile() {
        return new FileOutputBuilder(this, "error_file");
    }

    /**
     * 웹훅 출력 설정 빌더
     */
    public WebhookOutputBuilder webhook() {
        return new WebhookOutputBuilder(this);
    }

    /**
     * 이메일 출력 설정 빌더
     */
    public EmailOutputBuilder email() {
        return new EmailOutputBuilder(this);
    }

    /**
     * 데이터베이스 출력 설정 빌더
     */
    public DatabaseOutputBuilder database() {
        return new DatabaseOutputBuilder(this);
    }

    // ========== Performance Settings ==========

    /**
     * 비동기 로깅 활성화
     */
    public LoggerConfigBuilder async(boolean enabled) {
        this.asyncEnabled = enabled;
        return this;
    }

    /**
     * 버퍼 크기
     */
    public LoggerConfigBuilder bufferSize(int size) {
        this.bufferSize = size;
        return this;
    }

    /**
     * 플러시 간격 (ms)
     */
    public LoggerConfigBuilder flushInterval(int interval) {
        this.flushInterval = interval;
        return this;
    }

    /**
     * 최대 큐 크기
     */
    public LoggerConfigBuilder maxQueueSize(int size) {
        this.maxQueueSize = size;
        return this;
    }

    // ========== Filter Settings ==========

    /**
     * 민감 데이터 필터링 활성화
     */
    public LoggerConfigBuilder sensitiveDataFilter(boolean enabled) {
        this.sensitiveDataEnabled = enabled;
        return this;
    }

    /**
     * 민감 데이터 패턴 설정
     */
    public LoggerConfigBuilder sensitivePatterns(
            LoggerConfig.FiltersConfig.SensitiveDataConfig.PatternConfig[] patterns) {
        this.sensitivePatterns = patterns;
        return this;
    }

    // ========== Internal Methods ==========

    void addOutput(String key, LoggerConfig.OutputConfig config) {
        this.outputs.put(key, config);
    }

    // ========== Build ==========

    /**
     * LoggerConfig 빌드
     */
    public LoggerConfig build() {
        LoggerConfig config = new LoggerConfig();

        // Version
        config.setVersion(version);

        // Global
        LoggerConfig.GlobalConfig global = new LoggerConfig.GlobalConfig();
        global.setAppName(appName);
        global.setTimezone(timezone);
        global.setEncoding(encoding);
        config.setGlobal(global);

        // Levels
        config.setLevels(new HashMap<>(levels));

        // Outputs
        config.setOutputs(new HashMap<>(outputs));

        // Performance
        LoggerConfig.PerformanceConfig performance = new LoggerConfig.PerformanceConfig();
        performance.setAsync(asyncEnabled);
        performance.setBufferSize(bufferSize);
        performance.setFlushInterval(flushInterval);
        performance.setMaxQueueSize(maxQueueSize);
        config.setPerformance(performance);

        // Filters
        LoggerConfig.FiltersConfig filters = new LoggerConfig.FiltersConfig();
        LoggerConfig.FiltersConfig.SensitiveDataConfig sensitiveData =
            new LoggerConfig.FiltersConfig.SensitiveDataConfig();
        sensitiveData.setEnabled(sensitiveDataEnabled);
        if (sensitivePatterns != null) {
            sensitiveData.setPatterns(sensitivePatterns);
        }
        filters.setSensitiveData(sensitiveData);
        config.setFilters(filters);

        return config;
    }

    // ========== Nested Builders ==========

    /**
     * 콘솔 출력 설정 빌더
     */
    public static class ConsoleOutputBuilder {
        private final LoggerConfigBuilder parent;
        private boolean enabled = true;
        private String level = "INFO";
        private String format = "[%d{yyyy-MM-dd HH:mm:ss}] %-5level %logger{36} - %msg%n";
        private final Map<String, String> colors = new HashMap<>();

        ConsoleOutputBuilder(LoggerConfigBuilder parent) {
            this.parent = parent;
            // 기본 색상 설정
            colors.put("DEBUG", "cyan");
            colors.put("INFO", "green");
            colors.put("WARN", "yellow");
            colors.put("ERROR", "red");
        }

        public ConsoleOutputBuilder enabled(boolean enabled) {
            this.enabled = enabled;
            return this;
        }

        public ConsoleOutputBuilder level(String level) {
            this.level = level;
            return this;
        }

        public ConsoleOutputBuilder format(String format) {
            this.format = format;
            return this;
        }

        public ConsoleOutputBuilder color(String level, String color) {
            this.colors.put(level, color);
            return this;
        }

        public LoggerConfigBuilder and() {
            LoggerConfig.OutputConfig config = new LoggerConfig.OutputConfig();
            config.setEnabled(enabled);
            config.setLevel(level);
            config.setFormat(format);
            config.setColors(new HashMap<>(colors));
            parent.addOutput("console", config);
            return parent;
        }
    }

    /**
     * 파일 출력 설정 빌더
     */
    public static class FileOutputBuilder {
        private final LoggerConfigBuilder parent;
        private final String outputKey;
        private boolean enabled = true;
        private String level = "INFO";
        private String format = "[%d{yyyy-MM-dd HH:mm:ss}] %-5level %logger{36} - %msg%n";
        private String path = "logs/{appName}.log";
        private String maxSize = "10MB";
        private int maxFiles = 30;
        private boolean compress = true;

        FileOutputBuilder(LoggerConfigBuilder parent, String outputKey) {
            this.parent = parent;
            this.outputKey = outputKey;
            if ("error_file".equals(outputKey)) {
                this.level = "ERROR";
                this.path = "logs/{appName}-error.log";
            }
        }

        public FileOutputBuilder enabled(boolean enabled) {
            this.enabled = enabled;
            return this;
        }

        public FileOutputBuilder level(String level) {
            this.level = level;
            return this;
        }

        public FileOutputBuilder format(String format) {
            this.format = format;
            return this;
        }

        public FileOutputBuilder path(String path) {
            this.path = path;
            return this;
        }

        public FileOutputBuilder maxSize(String maxSize) {
            this.maxSize = maxSize;
            return this;
        }

        public FileOutputBuilder maxFiles(int maxFiles) {
            this.maxFiles = maxFiles;
            return this;
        }

        public FileOutputBuilder compress(boolean compress) {
            this.compress = compress;
            return this;
        }

        public LoggerConfigBuilder and() {
            LoggerConfig.OutputConfig config = new LoggerConfig.OutputConfig();
            config.setEnabled(enabled);
            config.setLevel(level);
            config.setFormat(format);
            config.setPath(path);

            LoggerConfig.RotationConfig rotation = new LoggerConfig.RotationConfig();
            rotation.setMaxSize(maxSize);
            rotation.setMaxFiles(maxFiles);
            rotation.setCompress(compress);
            config.setRotation(rotation);

            parent.addOutput(outputKey, config);
            return parent;
        }
    }

    /**
     * 웹훅 출력 설정 빌더
     */
    public static class WebhookOutputBuilder {
        private final LoggerConfigBuilder parent;
        private boolean enabled = false;
        private String level = "ERROR";
        private String url;
        private String format = "json";

        WebhookOutputBuilder(LoggerConfigBuilder parent) {
            this.parent = parent;
        }

        public WebhookOutputBuilder enabled(boolean enabled) {
            this.enabled = enabled;
            return this;
        }

        public WebhookOutputBuilder level(String level) {
            this.level = level;
            return this;
        }

        public WebhookOutputBuilder url(String url) {
            this.url = url;
            return this;
        }

        public WebhookOutputBuilder format(String format) {
            this.format = format;
            return this;
        }

        public LoggerConfigBuilder and() {
            LoggerConfig.OutputConfig config = new LoggerConfig.OutputConfig();
            config.setEnabled(enabled);
            config.setLevel(level);

            LoggerConfig.WebhookConfig webhook = new LoggerConfig.WebhookConfig();
            webhook.setUrl(url);
            webhook.setFormat(format);
            config.setWebhook(webhook);

            parent.addOutput("webhook", config);
            return parent;
        }
    }

    /**
     * 이메일 출력 설정 빌더
     */
    public static class EmailOutputBuilder {
        private final LoggerConfigBuilder parent;
        private boolean enabled = false;
        private String level = "ERROR";
        private String smtpHost;
        private int smtpPort = 587;
        private String smtpUsername;
        private String smtpPassword;
        private String[] to;
        private String subject = "[{appName}] Log Alert";

        EmailOutputBuilder(LoggerConfigBuilder parent) {
            this.parent = parent;
        }

        public EmailOutputBuilder enabled(boolean enabled) {
            this.enabled = enabled;
            return this;
        }

        public EmailOutputBuilder level(String level) {
            this.level = level;
            return this;
        }

        public EmailOutputBuilder smtpHost(String host) {
            this.smtpHost = host;
            return this;
        }

        public EmailOutputBuilder smtpPort(int port) {
            this.smtpPort = port;
            return this;
        }

        public EmailOutputBuilder smtpCredentials(String username, String password) {
            this.smtpUsername = username;
            this.smtpPassword = password;
            return this;
        }

        public EmailOutputBuilder to(String... recipients) {
            this.to = recipients;
            return this;
        }

        public EmailOutputBuilder subject(String subject) {
            this.subject = subject;
            return this;
        }

        public LoggerConfigBuilder and() {
            LoggerConfig.OutputConfig config = new LoggerConfig.OutputConfig();
            config.setEnabled(enabled);
            config.setLevel(level);

            LoggerConfig.EmailConfig email = new LoggerConfig.EmailConfig();
            LoggerConfig.EmailConfig.SmtpConfig smtp = new LoggerConfig.EmailConfig.SmtpConfig();
            smtp.setHost(smtpHost);
            smtp.setPort(smtpPort);
            smtp.setUsername(smtpUsername);
            smtp.setPassword(smtpPassword);
            email.setSmtp(smtp);
            email.setTo(to);
            email.setSubject(subject);
            config.setEmail(email);

            parent.addOutput("email", config);
            return parent;
        }
    }

    /**
     * 데이터베이스 출력 설정 빌더
     */
    public static class DatabaseOutputBuilder {
        private final LoggerConfigBuilder parent;
        private boolean enabled = false;
        private String level = "INFO";
        private String type = "mysql";
        private String host = "localhost";
        private int port = 3306;
        private String database;
        private String table = "logs";
        private String username;
        private String password;

        DatabaseOutputBuilder(LoggerConfigBuilder parent) {
            this.parent = parent;
        }

        public DatabaseOutputBuilder enabled(boolean enabled) {
            this.enabled = enabled;
            return this;
        }

        public DatabaseOutputBuilder level(String level) {
            this.level = level;
            return this;
        }

        public DatabaseOutputBuilder type(String type) {
            this.type = type;
            return this;
        }

        public DatabaseOutputBuilder host(String host) {
            this.host = host;
            return this;
        }

        public DatabaseOutputBuilder port(int port) {
            this.port = port;
            return this;
        }

        public DatabaseOutputBuilder database(String database) {
            this.database = database;
            return this;
        }

        public DatabaseOutputBuilder table(String table) {
            this.table = table;
            return this;
        }

        public DatabaseOutputBuilder credentials(String username, String password) {
            this.username = username;
            this.password = password;
            return this;
        }

        public LoggerConfigBuilder and() {
            LoggerConfig.OutputConfig config = new LoggerConfig.OutputConfig();
            config.setEnabled(enabled);
            config.setLevel(level);

            LoggerConfig.DatabaseConfig dbConfig = new LoggerConfig.DatabaseConfig();
            dbConfig.setType(type);
            dbConfig.setHost(host);
            dbConfig.setPort(port);
            dbConfig.setDatabase(database);
            dbConfig.setTable(table);
            dbConfig.setUsername(username);
            dbConfig.setPassword(password);
            config.setDatabase(dbConfig);

            parent.addOutput("database", config);
            return parent;
        }
    }
}
