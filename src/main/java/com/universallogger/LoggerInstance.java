package com.universallogger;

import com.universallogger.appenders.AppenderFactory;
import com.universallogger.appenders.DefaultAppenderFactory;
import com.universallogger.config.LoggerConfig;
import com.universallogger.constants.LoggerConstants;
import com.universallogger.filters.SensitiveDataFilter;
import com.universallogger.loaders.UniversalConfigLoader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.Level;
import ch.qos.logback.core.Appender;
import ch.qos.logback.classic.spi.ILoggingEvent;

import java.io.File;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * LoggerInstance - 인스턴스 기반 로거 구현
 *
 * 사용법:
 * <pre>
 * // Builder를 통한 생성
 * LoggerInstance logger = LoggerInstance.builder()
 *     .configPath("logging.yml")
 *     .build();
 *
 * // 또는 직접 설정 객체로 생성
 * LoggerInstance logger = LoggerInstance.builder()
 *     .config(loggerConfig)
 *     .build();
 *
 * // 커스텀 AppenderFactory 주입 (테스트/확장용)
 * LoggerInstance logger = LoggerInstance.builder()
 *     .configPath("logging.yml")
 *     .appenderFactory(customFactory)
 *     .build();
 *
 * // 로깅
 * logger.info("Hello World!");
 * </pre>
 */
public class LoggerInstance {

    private static final Logger logger = LoggerFactory.getLogger(LoggerInstance.class);
    private static final Object INIT_LOCK = new Object();

    private final LoggerConfig config;
    private final LoggerContext loggerContext;
    private final Map<String, Logger> loggers;
    private final com.universallogger.filters.MessageFilter messageFilter;
    private final AppenderFactory appenderFactory;
    private final com.universallogger.loaders.ConfigLoader configLoader;
    private final boolean initialized;

    /**
     * Private 생성자 - Builder를 통해서만 인스턴스 생성
     */
    private LoggerInstance(Builder builder) {
        this.loggers = new ConcurrentHashMap<>();

        // AppenderFactory 설정 (주입 또는 기본값)
        this.appenderFactory = builder.appenderFactory != null
            ? builder.appenderFactory
            : new DefaultAppenderFactory();

        // ConfigLoader 설정 (주입 또는 기본값)
        this.configLoader = builder.configLoader != null
            ? builder.configLoader
            : new UniversalConfigLoader();

        // Config 설정 (방어적 복사 적용 - 외부 변경으로부터 보호)
        if (builder.config != null) {
            this.config = builder.config.copy();
        } else if (builder.configPath != null) {
            this.config = loadConfig(builder.configPath);
        } else {
            this.config = loadDefaultConfig();
        }

        // MessageFilter 설정 (주입 또는 기본값)
        this.messageFilter = builder.messageFilter != null
            ? builder.messageFilter
            : new SensitiveDataFilter(this.config.getFilters());

        this.loggerContext = initLoggerContext();
        this.initialized = setupLogging();
    }

    /**
     * 설정 파일 로드
     */
    private LoggerConfig loadConfig(String configPath) {
        try {
            return configLoader.load(configPath);
        } catch (Exception e) {
            logger.error("Failed to load config: {}, falling back to defaults.yml", configPath, e);
            return loadDefaultConfig();
        }
    }

    /**
     * 기본 설정 로드 (defaults.yml)
     */
    private LoggerConfig loadDefaultConfig() {
        try {
            return configLoader.loadDefaultConfig();
        } catch (Exception e) {
            logger.error("CRITICAL: Cannot load defaults/universal-logger-defaults.yml", e);
            throw new IllegalStateException("Failed to load default configuration from JAR", e);
        }
    }

    /**
     * LoggerContext 초기화
     *
     * Thread-safe: 여러 LoggerInstance가 동시에 생성되어도 초기화는 순차적으로 진행됨
     * 성능 영향: 초기화 시에만 동기화, 로깅 성능에는 영향 없음
     */
    private LoggerContext initLoggerContext() {
        synchronized (INIT_LOCK) {
            LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
            context.reset();
            return context;
        }
    }

    /**
     * 로깅 시스템 설정
     */
    private boolean setupLogging() {
        try {
            // 로그 디렉토리 생성
            new File("logs").mkdirs();

            // 루트 로거 설정
            ch.qos.logback.classic.Logger rootLogger =
                loggerContext.getLogger(Logger.ROOT_LOGGER_NAME);
            rootLogger.setLevel(Level.valueOf(
                config.getLevels().getOrDefault("root", "INFO")));

            // 출력 설정 (Factory 사용)
            setupOutputs(rootLogger);

            // 모듈별 로거 레벨 설정
            for (Map.Entry<String, String> entry : config.getLevels().entrySet()) {
                if (!"root".equals(entry.getKey())) {
                    ch.qos.logback.classic.Logger moduleLogger =
                        loggerContext.getLogger(entry.getKey());
                    moduleLogger.setLevel(Level.valueOf(entry.getValue()));
                }
            }

            return true;
        } catch (Exception e) {
            logger.error("Failed to setup logging", e);
            return false;
        }
    }

    /**
     * 출력 설정 - AppenderFactory를 사용하여 앱펜더 생성
     */
    private void setupOutputs(ch.qos.logback.classic.Logger rootLogger) {
        Map<String, LoggerConfig.OutputConfig> outputs = config.getOutputs();
        String appName = config.getGlobal().getAppName();

        // 콘솔 앱펜더
        if (isOutputEnabled(outputs, "console")) {
            Appender<ILoggingEvent> appender = appenderFactory.createConsoleAppender(
                loggerContext, outputs.get("console"), appName);
            rootLogger.addAppender(appender);
        }

        // 파일 앱펜더
        if (isOutputEnabled(outputs, "file")) {
            Appender<ILoggingEvent> appender = appenderFactory.createFileAppender(
                loggerContext, outputs.get("file"), appName);
            rootLogger.addAppender(appender);
        }

        // 에러 파일 앱펜더
        if (isOutputEnabled(outputs, "errorFile")) {
            Appender<ILoggingEvent> appender = appenderFactory.createErrorFileAppender(
                loggerContext, outputs.get("errorFile"), appName);
            rootLogger.addAppender(appender);
        }

        // 웹훅 앱펜더
        if (isOutputEnabled(outputs, "webhook")) {
            Appender<ILoggingEvent> appender = appenderFactory.createWebhookAppender(
                loggerContext, outputs.get("webhook"));
            rootLogger.addAppender(appender);
        }

        // 이메일 앱펜더
        if (isOutputEnabled(outputs, "email")) {
            Appender<ILoggingEvent> appender = appenderFactory.createEmailAppender(
                loggerContext, outputs.get("email"));
            rootLogger.addAppender(appender);
        }

        // 데이터베이스 앱펜더
        if (isOutputEnabled(outputs, "database")) {
            Appender<ILoggingEvent> appender = appenderFactory.createDatabaseAppender(
                loggerContext, outputs.get("database"));
            rootLogger.addAppender(appender);
        }
    }

    /**
     * 출력 활성화 여부 확인
     */
    private boolean isOutputEnabled(Map<String, LoggerConfig.OutputConfig> outputs, String key) {
        return outputs.containsKey(key) && outputs.get(key).isEnabled();
    }

    // ========== Public API ==========

    /**
     * 로거 인스턴스 반환
     */
    public Logger getLogger(String name) {
        return loggers.computeIfAbsent(name, n -> LoggerFactory.getLogger(n));
    }

    /**
     * 로거 인스턴스 반환 (클래스 기반)
     */
    public Logger getLogger(Class<?> clazz) {
        return getLogger(clazz.getSimpleName());
    }

    /**
     * 기본 로거 반환
     */
    private Logger getDefaultLogger() {
        return getLogger("universal");
    }

    /**
     * 메시지 필터링
     */
    private String filterMessage(String message) {
        if (messageFilter != null) {
            return messageFilter.filter(message);
        }
        return message;
    }

    // ========== Logging Methods ==========

    public void trace(String message) {
        getDefaultLogger().trace(filterMessage(message));
    }

    public void trace(String format, Object... args) {
        getDefaultLogger().trace(filterMessage(format), args);
    }

    public void debug(String message) {
        getDefaultLogger().debug(filterMessage(message));
    }

    public void debug(String format, Object... args) {
        getDefaultLogger().debug(filterMessage(format), args);
    }

    public void info(String message) {
        getDefaultLogger().info(filterMessage(message));
    }

    public void info(String format, Object... args) {
        getDefaultLogger().info(filterMessage(format), args);
    }

    public void warn(String message) {
        getDefaultLogger().warn(filterMessage(message));
    }

    public void warn(String format, Object... args) {
        getDefaultLogger().warn(filterMessage(format), args);
    }

    public void error(String message) {
        getDefaultLogger().error(filterMessage(message));
    }

    public void error(String format, Object... args) {
        getDefaultLogger().error(filterMessage(format), args);
    }

    public void error(String message, Throwable throwable) {
        getDefaultLogger().error(filterMessage(message), throwable);
    }

    // ========== Getters ==========

    /**
     * 현재 설정 반환
     */
    public LoggerConfig getConfig() {
        return config;
    }

    /**
     * 초기화 상태 확인
     */
    public boolean isInitialized() {
        return initialized;
    }

    /**
     * LoggerContext 반환 (고급 사용자용)
     */
    public LoggerContext getLoggerContext() {
        return loggerContext;
    }

    /**
     * AppenderFactory 반환
     */
    public AppenderFactory getAppenderFactory() {
        return appenderFactory;
    }

    // ========== Builder ==========

    /**
     * Builder 생성
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Builder 클래스 - LoggerInstance 생성을 위한 빌더 패턴
     *
     * DI(의존성 주입) 지원:
     * - AppenderFactory 주입 가능 (테스트 시 Mock 주입)
     * - ConfigLoader 주입 가능 (테스트 시 Mock 주입)
     * - MessageFilter 주입 가능 (테스트 시 Mock 주입)
     * - Config 또는 configPath 지정 가능
     */
    public static class Builder {
        private String configPath;
        private LoggerConfig config;
        private AppenderFactory appenderFactory;
        private com.universallogger.loaders.ConfigLoader configLoader;
        private com.universallogger.filters.MessageFilter messageFilter;

        private Builder() {}

        /**
         * 설정 파일 경로 지정
         */
        public Builder configPath(String configPath) {
            this.configPath = configPath;
            return this;
        }

        /**
         * 설정 객체 직접 지정
         */
        public Builder config(LoggerConfig config) {
            this.config = config;
            return this;
        }

        /**
         * 커스텀 AppenderFactory 지정 (DI/테스트용)
         *
         * @param appenderFactory 사용할 AppenderFactory 구현체
         */
        public Builder appenderFactory(AppenderFactory appenderFactory) {
            this.appenderFactory = appenderFactory;
            return this;
        }

        /**
         * 커스텀 ConfigLoader 지정 (DI/테스트용)
         *
         * @param configLoader 사용할 ConfigLoader 구현체
         */
        public Builder configLoader(com.universallogger.loaders.ConfigLoader configLoader) {
            this.configLoader = configLoader;
            return this;
        }

        /**
         * 커스텀 MessageFilter 지정 (DI/테스트용)
         *
         * @param messageFilter 사용할 MessageFilter 구현체
         */
        public Builder messageFilter(com.universallogger.filters.MessageFilter messageFilter) {
            this.messageFilter = messageFilter;
            return this;
        }

        /**
         * LoggerInstance 생성
         */
        public LoggerInstance build() {
            return new LoggerInstance(this);
        }
    }
}
