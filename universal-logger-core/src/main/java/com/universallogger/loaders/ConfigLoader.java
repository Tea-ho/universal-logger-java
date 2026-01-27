package com.universallogger.loaders;

import com.universallogger.config.LoggerConfig;

/**
 * 설정 로더 인터페이스
 *
 * Universal Logger의 설정 파일(YAML/XML)을 로드하는 로더들의 공통 인터페이스입니다.
 * 다양한 설정 파일 형식과 프로파일 기반 설정 로딩을 지원합니다.
 *
 * <p>주요 기능:</p>
 * <ul>
 *   <li>설정 파일 로딩 및 {@link LoggerConfig} 객체로 변환</li>
 *   <li>기본 설정과의 병합 지원</li>
 *   <li>프로파일(dev, prod, test 등) 기반 설정 오버라이드</li>
 * </ul>
 *
 * @see UniversalConfigLoader
 * @see LoggerConfig
 */
public interface ConfigLoader {

    /**
     * 설정 파일 로드
     *
     * @param configPath 설정 파일 경로
     * @return 로드된 설정 객체
     * @throws Exception 로딩 실패 시
     */
    LoggerConfig load(String configPath) throws Exception;

    /**
     * 기본 설정 병합 옵션과 함께 설정 파일 로드
     *
     * @param configPath 설정 파일 경로
     * @param mergeDefaults 기본 설정과 병합 여부
     * @return 로드된 설정 객체
     * @throws Exception 로딩 실패 시
     */
    LoggerConfig load(String configPath, boolean mergeDefaults) throws Exception;

    /**
     * 프로파일 지원과 함께 설정 로드
     *
     * 기본 설정을 로드한 후, 프로파일이 감지되면 프로파일별 설정과 병합합니다.
     *
     * <p>로딩 순서:</p>
     * <ol>
     *   <li>defaults/universal-logger-defaults.yml (기본 기본값)</li>
     *   <li>{configPath} (사용자 기본 설정)</li>
     *   <li>{configPath에 프로파일 접미사 추가} (프로파일이 존재하면 사용자 프로파일 설정)</li>
     * </ol>
     *
     * <p>예시:</p>
     * <ul>
     *   <li>프로파일: "prod"</li>
     *   <li>기본: META-INF/universal-logger/universal-logger.yml</li>
     *   <li>프로파일: META-INF/universal-logger/universal-logger-prod.yml</li>
     * </ul>
     *
     * @param configPath 기본 설정 파일 경로
     * @param profile 프로파일 이름 (예: "dev", "prod", "test"), 프로파일 미사용 시 null
     * @param mergeDefaults 기본 설정과 병합 여부
     * @return 로드 및 병합된 설정 객체
     * @throws Exception 로딩 실패 시
     */
    LoggerConfig loadWithProfile(String configPath, String profile, boolean mergeDefaults) throws Exception;

    /**
     * 기본 설정 로드
     * JAR에 내장된 기본 설정 파일을 로드합니다.
     *
     * @return 기본 설정 객체
     * @throws Exception 로딩 실패 시
     */
    LoggerConfig loadDefaultConfig() throws Exception;
}
