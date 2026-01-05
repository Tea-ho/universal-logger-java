package com.universallogger.loaders;

import com.universallogger.config.LoggerConfig;

/**
 * 설정 로더 인터페이스
 */
public interface ConfigLoader {

    /**
     * 설정 파일 로드
     *
     * @param configPath 설정 파일 경로
     * @return 로드된 설정 객체
     * @throws Exception 로드 실패 시
     */
    LoggerConfig load(String configPath) throws Exception;

    /**
     * 설정 파일 로드 (병합 옵션)
     *
     * @param configPath 설정 파일 경로
     * @param mergeDefaults 기본 설정과 병합 여부
     * @return 로드된 설정 객체
     * @throws Exception 로드 실패 시
     */
    LoggerConfig load(String configPath, boolean mergeDefaults) throws Exception;

    /**
     * 기본 설정 로드
     * JAR 내장 defaults 파일 로드
     *
     * @return 기본 설정 객체
     * @throws Exception 로드 실패 시
     */
    LoggerConfig loadDefaultConfig() throws Exception;
}
