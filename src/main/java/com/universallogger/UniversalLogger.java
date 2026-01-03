package com.universallogger;

import com.universallogger.config.LoggerConfig;
import com.universallogger.constants.LoggerConstants;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

/**
 * Universal Logger - Java용 범용 로깅 라이브러리
 *
 * Bill Pugh 싱글톤 패턴을 적용하여 thread-safe한 lazy initialization 제공.
 * 기존 static API 호환성 유지 + 인스턴스 기반 사용 지원.
 *
 * 사용법 1 - 기존 방식 (static API):
 * <pre>
 * UniversalLogger.init("logging.yml");
 * UniversalLogger.info("Hello World!");
 * </pre>
 *
 * 사용법 2 - 인스턴스 방식 (DI 지원):
 * <pre>
 * LoggerInstance logger = UniversalLogger.createInstance("logging.yml");
 * logger.info("Hello World!");
 * </pre>
 *
 * 사용법 3 - 싱글톤 인스턴스 직접 접근:
 * <pre>
 * LoggerInstance instance = UniversalLogger.getInstance();
 * instance.info("Hello World!");
 * </pre>
 */
public final class UniversalLogger {

    private static final Logger logger = LoggerFactory.getLogger(UniversalLogger.class);

    /**
     * Bill Pugh 싱글톤 홀더 - Thread-safe한 지연 초기화 제공
     */
    private static class InstanceHolder {
        private static volatile LoggerInstance INSTANCE;
        private static volatile boolean explicitlyInitialized = false;

        private static synchronized LoggerInstance getOrCreate() {
            if (INSTANCE == null) {
                INSTANCE = createDefaultInstance();
            }
            return INSTANCE;
        }

        private static synchronized void initialize(LoggerConfig config) {
            INSTANCE = LoggerInstance.builder()
                .config(config)
                .build();
            explicitlyInitialized = true;
        }

        private static synchronized void initialize(String configPath) {
            INSTANCE = LoggerInstance.builder()
                .configPath(configPath)
                .build();
            explicitlyInitialized = true;
        }

        private static LoggerInstance createDefaultInstance() {
            // 자동 설정 파일 탐색
            String[] configPaths = LoggerConstants.ConfigPath.SEARCH_PATHS;

            for (String path : configPaths) {
                try {
                    if (path.startsWith("classpath:")) {
                        String resourcePath = path.substring("classpath:".length());
                        if (UniversalLogger.class.getClassLoader().getResource(resourcePath) != null) {
                            logger.info("Auto-detected config: {}", path);
                            return LoggerInstance.builder()
                                .configPath(path)
                                .build();
                        }
                    } else {
                        File file = new File(path);
                        if (file.exists()) {
                            logger.info("Auto-detected config: {}", path);
                            return LoggerInstance.builder()
                                .configPath(path)
                                .build();
                        }
                    }
                } catch (Exception e) {
                    // 다음 경로 시도
                    continue;
                }
            }

            // 설정 파일을 찾지 못한 경우 기본 설정
            logger.info("No config file found. Using default settings.");
            return LoggerInstance.builder().build();
        }
    }

    /**
     * Private 생성자 - 인스턴스화 방지
     */
    private UniversalLogger() {
        throw new UnsupportedOperationException(
            "UniversalLogger is a utility class. Use static methods or getInstance().");
    }

    // ========== 싱글톤 인스턴스 접근 ==========

    /**
     * 싱글톤 LoggerInstance 반환
     *
     * @return 싱글톤 LoggerInstance
     */
    public static LoggerInstance getInstance() {
        return InstanceHolder.getOrCreate();
    }

    // ========== 초기화 메서드 ==========

    /**
     * 설정 파일로 로거 초기화
     *
     * @param configPath 설정 파일 경로 (YAML, XML, JSON 지원)
     */
    public static synchronized void init(String configPath) {
        if (InstanceHolder.explicitlyInitialized) {
            logger.info("Re-initializing with config: {}", configPath);
        }

        try {
            InstanceHolder.initialize(configPath);
            logger.info("Initialized with config: {}", configPath);
        } catch (Exception e) {
            logger.error("Failed to initialize with config: {}", configPath, e);
        }
    }

    /**
     * 설정 객체로 직접 초기화
     *
     * @param config LoggerConfig 설정 객체
     */
    public static synchronized void init(LoggerConfig config) {
        InstanceHolder.initialize(config);
        logger.info("Initialized with config object.");
    }

    // ========== 인스턴스 생성 (DI용) ==========

    /**
     * 새로운 독립적인 LoggerInstance 생성 (DI, 테스트용)
     *
     * @param configPath 설정 파일 경로
     * @return 새로운 LoggerInstance
     */
    public static LoggerInstance createInstance(String configPath) {
        return LoggerInstance.builder()
            .configPath(configPath)
            .build();
    }

    /**
     * 새로운 독립적인 LoggerInstance 생성 (DI, 테스트용)
     *
     * @param config LoggerConfig 설정 객체
     * @return 새로운 LoggerInstance
     */
    public static LoggerInstance createInstance(LoggerConfig config) {
        return LoggerInstance.builder()
            .config(config)
            .build();
    }

    /**
     * 기본 설정으로 새 LoggerInstance 생성
     *
     * @return 기본 설정의 LoggerInstance
     */
    public static LoggerInstance createInstance() {
        return LoggerInstance.builder().build();
    }

    // ========== 로거 인스턴스 접근 ==========

    /**
     * 로거 인스턴스 반환
     *
     * @param name 로거 이름
     * @return SLF4J Logger
     */
    public static Logger getLogger(String name) {
        return getInstance().getLogger(name);
    }

    /**
     * 로거 인스턴스 반환 (클래스 기반)
     *
     * @param clazz 클래스
     * @return SLF4J Logger
     */
    public static Logger getLogger(Class<?> clazz) {
        return getInstance().getLogger(clazz);
    }

    // ========== 유틸리티 메서드 ==========

    /**
     * 현재 설정 반환
     *
     * @return LoggerConfig 설정 객체
     */
    public static LoggerConfig getConfig() {
        return getInstance().getConfig();
    }

    /**
     * 초기화 상태 확인
     *
     * @return 초기화 여부
     */
    public static boolean isInitialized() {
        return getInstance().isInitialized();
    }

    /**
     * 명시적으로 초기화되었는지 확인
     * (자동 초기화가 아닌 init() 호출로 초기화된 경우)
     *
     * @return 명시적 초기화 여부
     */
    public static boolean isExplicitlyInitialized() {
        return InstanceHolder.explicitlyInitialized;
    }

    /**
     * 싱글톤 인스턴스 리셋 (테스트용)
     * 프로덕션 코드에서는 사용하지 않는 것을 권장
     */
    public static synchronized void reset() {
        InstanceHolder.INSTANCE = null;
        InstanceHolder.explicitlyInitialized = false;
    }
}
