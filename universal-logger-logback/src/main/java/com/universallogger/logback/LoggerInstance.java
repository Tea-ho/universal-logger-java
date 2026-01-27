package com.universallogger.logback;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import com.universallogger.config.LoggerConfig;
import com.universallogger.loaders.UniversalConfigLoader;
import com.universallogger.logback.appenders.AppenderFactory;
import com.universallogger.logback.appenders.DefaultAppenderFactory;
import com.universallogger.utils.ProfileResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Universal Logger의 Logback 구현체 핵심 클래스
 *
 * 설정 파일을 기반으로 Logback 로깅 시스템을 초기화하고 관리합니다.
 * Builder 패턴을 사용하여 유연한 인스턴스 생성을 지원합니다.
 *
 * <p>주요 기능:</p>
 * <ul>
 *   <li>YAML 설정 파일 기반 로깅 설정</li>
 *   <li>프로파일 기반 설정 지원 (dev, prd 등)</li>
 *   <li>다양한 앱펜더 지원 (콘솔, 파일, 웹훅, 이메일)</li>
 *   <li>의존성 주입(DI) 지원 (AppenderFactory, ConfigLoader)</li>
 * </ul>
 *
 * <p>사용 예시:</p>
 * <pre>{@code
 * LoggerInstance logger = LoggerInstance.builder()
 *     .configPath("META-INF/universal-logger/universal-logger.yml")
 *     .profile("dev")
 *     .build();
 * }</pre>
 *
 * @see AppenderFactory
 * @see LoggerConfig
 */
public class LoggerInstance {

    private static final Logger logger = LoggerFactory.getLogger(LoggerInstance.class);
    private static final Object INIT_LOCK = new Object();

    private final LoggerConfig config;
    private final LoggerContext loggerContext;
    private final Map<String, Logger> loggers;
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
            this.config = loadConfig(builder.configPath, builder.profile);
        } else {
            this.config = loadDefaultConfig();
        }

        // applicationName 오버라이드 (Builder에서 지정된 경우)
        if (builder.applicationName != null && !builder.applicationName.trim().isEmpty()) {
            this.config.getGlobal().setAppName(builder.applicationName.trim());
            logger.debug("Application name overridden by builder: {}", builder.applicationName);
        }

        this.loggerContext = initLoggerContext();
        this.initialized = setupLogging();
    }

    /**
     * 설정 파일 로드 (프로파일 지원)
     *
     * Spring/Spring Boot 프로젝트의 경우 spring.profiles.active를 자동 감지하여
     * 프로파일별 설정 파일을 로드합니다.
     *
     * 로딩 순서:
     * 1. defaults/universal-logger-defaults.yml (기본값)
     * 2. {configPath} (사용자 기본 설정)
     * 3. {configPath with profile} (사용자 프로파일 설정, 있는 경우)
     *
     * @param configPath 설정 파일 경로
     * @param profile 활성 프로파일 (null이면 자동 감지)
     */
    private LoggerConfig loadConfig(String configPath, String profile) {
        try {
            // 명시적으로 제공된 프로파일 사용, 없으면 자동 감지
            String activeProfile = profile != null ? profile : ProfileResolver.resolveProfile();

            if (activeProfile != null) {
                logger.info("Loading configuration with profile: {}", activeProfile);
            }

            // 프로파일 지원으로 설정 로드
            return configLoader.loadWithProfile(configPath, activeProfile, true);
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
    }

    /**
     * 출력 활성화 여부 확인
     */
    private boolean isOutputEnabled(Map<String, LoggerConfig.OutputConfig> outputs, String key) {
        return outputs.containsKey(key) && outputs.get(key).isEnabled();
    }

    // ========== 공개 API ==========

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

    // ========== Getter 메서드 ==========

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

    // ========== 빌더 ==========

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
     * - Config 또는 configPath 지정 가능
     * - Profile 명시적 지정 가능 (Spring 통합 시 권장)
     */
    public static class Builder {
        private String configPath;
        private String profile;
        private String applicationName;
        private LoggerConfig config;
        private AppenderFactory appenderFactory;
        private com.universallogger.loaders.ConfigLoader configLoader;

        private Builder() {}

        /**
         * 설정 파일 경로 지정
         */
        public Builder configPath(String configPath) {
            this.configPath = configPath;
            return this;
        }

        /**
         * 활성 프로파일 지정 (명시적)
         *
         * Spring Framework/Spring Boot 통합 시 Environment에서 읽은 프로파일을
         * 명시적으로 전달하는 것을 권장합니다.
         *
         * 지정하지 않으면 ProfileResolver가 자동으로 감지합니다.
         *
         * @param profile 활성 프로파일 (예: "dev", "prd")
         */
        public Builder profile(String profile) {
            this.profile = profile;
            return this;
        }

        /**
         * 애플리케이션 이름 지정
         *
         * 로그 파일 경로의 {appName} 플레이스홀더 치환에 사용됩니다.
         * 지정 시 YAML 설정 파일의 global.appName보다 우선 적용됩니다.
         *
         * @param applicationName 애플리케이션 이름
         */
        public Builder applicationName(String applicationName) {
            this.applicationName = applicationName;
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
         * LoggerInstance 생성
         */
        public LoggerInstance build() {
            return new LoggerInstance(this);
        }
    }
}
