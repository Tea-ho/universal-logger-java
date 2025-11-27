package com.universallogger.utils;

import com.universallogger.config.LoggerConfig;
import com.universallogger.loaders.UniversalConfigLoader;

/**
 * 설정 로더 유틸리티 클래스 (하위 호환성 유지)
 *
 * 기존 코드와의 호환성을 위해 정적 메서드를 제공합니다.
 * 내부적으로 UniversalConfigLoader 인스턴스를 사용합니다.
 *
 * @deprecated 새 코드에서는 ConfigLoader 인터페이스와 UniversalConfigLoader 사용을 권장합니다.
 *             DI를 통한 테스트 용이성과 확장성이 향상됩니다.
 */
public class ConfigLoader {

    private static final com.universallogger.loaders.ConfigLoader DEFAULT_LOADER = new UniversalConfigLoader();

    /**
     * 설정 파일 로드 with default config merge
     * Loads default config from JAR, then merges user config
     *
     * @param configPath 설정 파일 경로
     * @return 로드된 설정 객체
     * @throws Exception 로드 실패 시
     */
    public static LoggerConfig load(String configPath) throws Exception {
        return DEFAULT_LOADER.load(configPath);
    }

    /**
     * 설정 파일 로드 (병합 옵션)
     *
     * @param configPath 설정 파일 경로
     * @param mergeDefaults 기본 설정과 병합 여부
     * @return 로드된 설정 객체
     * @throws Exception 로드 실패 시
     */
    public static LoggerConfig load(String configPath, boolean mergeDefaults) throws Exception {
        return DEFAULT_LOADER.load(configPath, mergeDefaults);
    }

    /**
     * 기본 설정 로드
     * JAR 내장 defaults 파일 로드
     *
     * @return 기본 설정 객체
     * @throws Exception 로드 실패 시
     */
    public static LoggerConfig loadDefaultConfig() throws Exception {
        return DEFAULT_LOADER.loadDefaultConfig();
    }
}
