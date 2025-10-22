package com.universallogger;

import com.universallogger.config.LoggerConfig;
import com.universallogger.appenders.*;
import com.universallogger.constants.LoggerConstants;
import com.universallogger.formatters.ColorFormatter;
import com.universallogger.formatters.CustomFormatter;
import com.universallogger.utils.ConfigLoader;
import com.universallogger.utils.MessageFilter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.Level;
import ch.qos.logback.core.ConsoleAppender;
import ch.qos.logback.core.rolling.RollingFileAppender;
import ch.qos.logback.core.rolling.TimeBasedRollingPolicy;
import ch.qos.logback.core.rolling.SizeAndTimeBasedRollingPolicy;
import ch.qos.logback.core.util.FileSize;
import ch.qos.logback.core.spi.FilterReply;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

/**
 * Universal Logger - Java용 범용 로깅 라이브러리
 *
 * 사용법:
 * UniversalLogger.init("logging.yml");
 * UniversalLogger.info("Hello World!");
 */
public class UniversalLogger {

    private static LoggerConfig config;
    private static LoggerContext loggerContext;
    private static Map<String, Logger> loggers = new ConcurrentHashMap<>();
    private static MessageFilter messageFilter;
    private static boolean initialized = false;
    private static boolean autoInitAttempted = false;

    /**
     * 자동 초기화 - 설정 파일 자동 감지
     * 우선순위: universal-logger.yml > universal-logger.xml > logback.xml > minimal.yml
     */
    private static synchronized void autoInit() {
        if (initialized || autoInitAttempted) {
            return;
        }

        autoInitAttempted = true;

        String[] configPaths = LoggerConstants.ConfigPath.SEARCH_PATHS;

        for (String path : configPaths) {
            try {
                if (path.startsWith("classpath:")) {
                    // 클래스패스 리소스 확인
                    String resourcePath = path.substring("classpath:".length());
                    if (UniversalLogger.class.getClassLoader().getResource(resourcePath) != null) {
                        init(path);
                        return;
                    }
                } else {
                    // 파일 시스템 확인
                    File file = new File(path);
                    if (file.exists()) {
                        init(path);
                        return;
                    }
                }
            } catch (Exception e) {
                // 다음 경로 시도
                continue;
            }
        }

        // 설정 파일을 찾지 못한 경우 기본 설정으로 초기화
        System.out.println("[Universal Logger] No config file found. Using default settings.");
        initWithDefaults();
    }

    /**
     * 설정 파일로 로거 초기화
     *
     * @param configPath 설정 파일 경로 (YAML, XML, JSON 지원)
     */
    public static synchronized void init(String configPath) {
        // 명시적 init() 호출은 재초기화 허용 (autoInit과 달리)
        if (initialized) {
            System.out.println("[Universal Logger] Re-initializing with config: " + configPath);
        }

        try {
            config = ConfigLoader.load(configPath);
            setupLogging();
            initialized = true;
            autoInitAttempted = true;
            System.out.println("[Universal Logger] Initialized with config: " + configPath);
        } catch (Exception e) {
            System.err.println("[Universal Logger] Failed to initialize with config: " + configPath);
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
            // 실패 시 기본 설정으로 폴백
            initWithDefaults();
        }
    }

    /**
     * 설정 객체로 직접 초기화
     */
    public static void init(LoggerConfig loggerConfig) {
        config = loggerConfig;
        setupLogging();
        initialized = true;
    }

    /**
     * 로깅 시스템 설정
     */
    private static void setupLogging() {
        loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
        loggerContext.reset();

        // 메시지 필터 설정
        messageFilter = new MessageFilter(config.getFilters());

        // 로그 디렉토리 생성
        new File("logs").mkdirs();

        // 루트 로거 설정
        ch.qos.logback.classic.Logger rootLogger = loggerContext.getLogger(Logger.ROOT_LOGGER_NAME);
        rootLogger.setLevel(Level.valueOf(config.getLevels().getOrDefault("root", "INFO")));

        // 출력 설정
        setupOutputs(rootLogger);

        // 모듈별 로거 레벨 설정
        for (Map.Entry<String, String> entry : config.getLevels().entrySet()) {
            if (!"root".equals(entry.getKey())) {
                ch.qos.logback.classic.Logger moduleLogger = loggerContext.getLogger(entry.getKey());
                moduleLogger.setLevel(Level.valueOf(entry.getValue()));
            }
        }
    }

    /**
     * 출력 설정
     */
    private static void setupOutputs(ch.qos.logback.classic.Logger rootLogger) {
        Map<String, LoggerConfig.OutputConfig> outputs = config.getOutputs();

        // 콘솔 출력
        if (outputs.containsKey("console") && outputs.get("console").isEnabled()) {
            setupConsoleAppender(rootLogger, outputs.get("console"));
        }

        // 파일 출력
        if (outputs.containsKey("file") && outputs.get("file").isEnabled()) {
            setupFileAppender(rootLogger, outputs.get("file"));
        }

        // 에러 파일
        if (outputs.containsKey("error_file") && outputs.get("error_file").isEnabled()) {
            setupErrorFileAppender(rootLogger, outputs.get("error_file"));
        }

        // 웹훅
        if (outputs.containsKey("webhook") && outputs.get("webhook").isEnabled()) {
            setupWebhookAppender(rootLogger, outputs.get("webhook"));
        }

        // 이메일
        if (outputs.containsKey("email") && outputs.get("email").isEnabled()) {
            setupEmailAppender(rootLogger, outputs.get("email"));
        }

        // 데이터베이스
        if (outputs.containsKey("database") && outputs.get("database").isEnabled()) {
            setupDatabaseAppender(rootLogger, outputs.get("database"));
        }
    }

    /**
     * 콘솔 어펜더 설정
     */
    private static void setupConsoleAppender(ch.qos.logback.classic.Logger logger, LoggerConfig.OutputConfig config) {
        ConsoleAppender<ch.qos.logback.classic.spi.ILoggingEvent> appender = new ConsoleAppender<>();
        appender.setContext(loggerContext);
        appender.setName("CONSOLE");

        // 컬러 포맷터 설정
        ColorFormatter formatter = new ColorFormatter();
        formatter.setContext(loggerContext);
        formatter.setPattern(config.getFormat());
        formatter.setColors(config.getColors());
        formatter.start();

        appender.setEncoder(formatter);
        appender.start();

        // 임계값 필터 추가 (해당 레벨 이상의 로그만 출력)
        ch.qos.logback.classic.filter.ThresholdFilter thresholdFilter = new ch.qos.logback.classic.filter.ThresholdFilter();
        thresholdFilter.setLevel(config.getLevel());
        thresholdFilter.start();
        appender.addFilter(thresholdFilter);
        
        appender.start();

        logger.addAppender(appender);
    }

    /**
     * 파일 어펜더 설정
     */
    private static void setupFileAppender(ch.qos.logback.classic.Logger logger, LoggerConfig.OutputConfig config) {
        RollingFileAppender<ch.qos.logback.classic.spi.ILoggingEvent> appender = new RollingFileAppender<>();
        appender.setContext(loggerContext);
        appender.setName("FILE");

        // 파일 경로 설정
        String filePath = replacePlaceholders(config.getPath());
        appender.setFile(filePath);

        // 롤링 정책 설정
        SizeAndTimeBasedRollingPolicy<ch.qos.logback.classic.spi.ILoggingEvent> rollingPolicy =
            new SizeAndTimeBasedRollingPolicy<>();
        rollingPolicy.setContext(loggerContext);
        rollingPolicy.setParent(appender);
        rollingPolicy.setFileNamePattern(filePath + ".%d{yyyy-MM-dd}.%i.gz");
        rollingPolicy.setMaxFileSize(FileSize.valueOf(config.getRotation().getMaxSize()));
        rollingPolicy.setMaxHistory(config.getRotation().getMaxFiles());
        rollingPolicy.start();

        appender.setRollingPolicy(rollingPolicy);

        // 포맷터 설정
        CustomFormatter formatter = new CustomFormatter();
        formatter.setContext(loggerContext);
        formatter.setPattern(config.getFormat());
        formatter.start();

        appender.setEncoder(formatter);
        appender.start();

        logger.addAppender(appender);
    }

    /**
     * 에러 파일 어펜더 설정
     */
    private static void setupErrorFileAppender(ch.qos.logback.classic.Logger logger, LoggerConfig.OutputConfig config) {
        RollingFileAppender<ch.qos.logback.classic.spi.ILoggingEvent> appender = new RollingFileAppender<>();
        appender.setContext(loggerContext);
        appender.setName("ERROR_FILE");

        String filePath = replacePlaceholders(config.getPath());
        appender.setFile(filePath);

        // 에러 레벨 필터
        ch.qos.logback.classic.filter.ThresholdFilter thresholdFilter =
            new ch.qos.logback.classic.filter.ThresholdFilter();
        thresholdFilter.setLevel(config.getLevel());
        thresholdFilter.start();
        appender.addFilter(thresholdFilter);
        
        appender.start();

        // 포맷터 설정
        CustomFormatter formatter = new CustomFormatter();
        formatter.setContext(loggerContext);
        formatter.setPattern(config.getFormat());
        formatter.start();

        appender.setEncoder(formatter);
        appender.start();

        logger.addAppender(appender);
    }

    /**
     * 웹훅 어펜더 설정
     */
    private static void setupWebhookAppender(ch.qos.logback.classic.Logger logger, LoggerConfig.OutputConfig config) {
        WebhookAppender appender = new WebhookAppender();
        appender.setContext(loggerContext);
        appender.setName("WEBHOOK");
        appender.setUrl(config.getWebhook().getUrl());
        appender.setFormat(config.getWebhook().getFormat());
        appender.start();

        logger.addAppender(appender);
    }

    /**
     * 이메일 어펜더 설정
     */
    private static void setupEmailAppender(ch.qos.logback.classic.Logger logger, LoggerConfig.OutputConfig config) {
        EmailAppender appender = new EmailAppender();
        appender.setContext(loggerContext);
        appender.setName("EMAIL");
        appender.setEmailConfig(config.getEmail());

        // 레벨 필터 추가 (설정된 레벨 이상만 이메일 발송)
        ch.qos.logback.classic.filter.ThresholdFilter thresholdFilter = new ch.qos.logback.classic.filter.ThresholdFilter();
        thresholdFilter.setLevel(config.getLevel());
        thresholdFilter.start();
        appender.addFilter(thresholdFilter);
        
        appender.start();

        logger.addAppender(appender);
    }

    /**
     * 데이터베이스 어펜더 설정
     */
    private static void setupDatabaseAppender(ch.qos.logback.classic.Logger logger, LoggerConfig.OutputConfig config) {
        DatabaseAppender appender = new DatabaseAppender();
        appender.setContext(loggerContext);
        appender.setName("DATABASE");
        appender.setDatabaseConfig(config.getDatabase());
        appender.start();

        logger.addAppender(appender);
    }

    /**
     * 플레이스홀더 치환
     * Note: {date} is not replaced here - it's used in Logback's rolling pattern
     */
    private static String replacePlaceholders(String path) {
        if (path == null) return "logs/app.log";

        // Only replace {appName} - do NOT replace {date} as it's used by Logback
        return path.replace("{appName}", config.getGlobal().getAppName());
    }

    /**
     * 로거 인스턴스 반환
     */
    public static Logger getLogger(String name) {
        if (!initialized) {
            // 자동 초기화 시도
            autoInit();
        }

        return loggers.computeIfAbsent(name, n -> LoggerFactory.getLogger(n));
    }

    /**
     * 로거 인스턴스 반환 (클래스 기반)
     */
    public static Logger getLogger(Class<?> clazz) {
        return getLogger(clazz.getSimpleName());
    }

    /**
     * 기본 설정으로 초기화
     */
    private static synchronized void initWithDefaults() {
        if (initialized) {
            return;
        }

        config = new LoggerConfig();

        // 기본 앱 이름 설정
        config.getGlobal().setAppName(LoggerConstants.Defaults.APP_NAME);

        // 루트 레벨 설정
        config.getLevels().put("root", LoggerConstants.Defaults.LOG_LEVEL);

        // 콘솔 출력 설정
        LoggerConfig.OutputConfig consoleConfig = new LoggerConfig.OutputConfig();
        consoleConfig.setEnabled(true);
        consoleConfig.setLevel("INFO");
        consoleConfig.setFormat("[%d{yyyy-MM-dd HH:mm:ss}] %-5level %logger{36} - %msg%n");
        config.getOutputs().put("console", consoleConfig);

        // 파일 출력 설정 (기본값은 비활성화)
        LoggerConfig.OutputConfig fileConfig = new LoggerConfig.OutputConfig();
        fileConfig.setEnabled(false);
        config.getOutputs().put("file", fileConfig);

        setupLogging();
        initialized = true;
        autoInitAttempted = true;
        System.out.println("[Universal Logger] Initialized with default settings.");
    }

    // 편의 메서드들
    private static Logger getDefaultLogger() {
        return getLogger("universal");
    }

    public static void debug(String message) {
        getDefaultLogger().debug(filterMessage(message));
    }

    public static void debug(String format, Object... args) {
        getDefaultLogger().debug(filterMessage(format), args);
    }

    public static void info(String message) {
        getDefaultLogger().info(filterMessage(message));
    }

    public static void info(String format, Object... args) {
        getDefaultLogger().info(filterMessage(format), args);
    }

    public static void warn(String message) {
        getDefaultLogger().warn(filterMessage(message));
    }

    public static void warn(String format, Object... args) {
        getDefaultLogger().warn(filterMessage(format), args);
    }

    public static void error(String message) {
        getDefaultLogger().error(filterMessage(message));
    }

    public static void error(String format, Object... args) {
        getDefaultLogger().error(filterMessage(format), args);
    }

    public static void error(String message, Throwable throwable) {
        getDefaultLogger().error(filterMessage(message), throwable);
    }

    /**
     * 메시지 필터링
     */
    private static String filterMessage(String message) {
        if (messageFilter != null) {
            return messageFilter.filter(message);
        }
        return message;
    }

    /**
     * 현재 설정 반환
     */
    public static LoggerConfig getConfig() {
        return config;
    }

    /**
     * 초기화 상태 확인
     */
    public static boolean isInitialized() {
        return initialized;
    }
}
