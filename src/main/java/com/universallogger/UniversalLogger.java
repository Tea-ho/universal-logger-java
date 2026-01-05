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
 * 설정 파일을 자동으로 탐색하여 초기화합니다.
 *
 * 자동 탐색 경로:
 * - universal-logger.yml, config/universal-logger.yml
 * - universal-logger.xml, config/universal-logger.xml
 * - logback.xml, config/logback.xml
 * - classpath:universal-logger.yml, classpath:logback.xml
 *
 * 사용법 1 - Logger 직접 사용 (권장):
 * <pre>
 * Logger logger = UniversalLogger.getLogger(MyClass.class);
 * logger.info("Hello World!");
 * </pre>
 *
 * 사용법 2 - LoggerInstance를 통한 사용 (DI 등):
 * <pre>
 * LoggerInstance instance = UniversalLogger.getInstance();
 * Logger logger = instance.getLogger(MyClass.class);
 * logger.info("Hello World!");
 * </pre>
 */
public final class UniversalLogger {

    private static final Logger logger = LoggerFactory.getLogger(UniversalLogger.class);

    /**
     * Bill Pugh 싱글톤 홀더 - Thread-safe한 지연 초기화 제공
     */
    private static class InstanceHolder {
        private static volatile LoggerInstance INSTANCE;

        private static synchronized LoggerInstance getOrCreate() {
            if (INSTANCE == null) {
                INSTANCE = createDefaultInstance();
            }
            return INSTANCE;
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
     * 싱글톤 인스턴스 리셋 (테스트용)
     * 프로덕션 코드에서는 사용하지 않는 것을 권장
     */
    public static synchronized void reset() {
        InstanceHolder.INSTANCE = null;
    }
}
