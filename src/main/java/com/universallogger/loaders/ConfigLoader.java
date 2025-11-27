package com.universallogger.loaders;

import com.universallogger.config.LoggerConfig;

/**
 * 설정 로더 인터페이스
 *
 * 의존성 역전 원칙(DIP) 준수:
 * - 구체 클래스가 아닌 인터페이스에 의존
 * - 다양한 설정 포맷 지원 가능 (YAML, JSON, XML, Properties 등)
 * - 테스트 시 Mock 주입 가능
 *
 * 개방-폐쇄 원칙(OCP) 준수:
 * - 새로운 포맷 추가 시 인터페이스 구현만 하면 됨
 * - 기존 코드 변경 없이 확장 가능
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
