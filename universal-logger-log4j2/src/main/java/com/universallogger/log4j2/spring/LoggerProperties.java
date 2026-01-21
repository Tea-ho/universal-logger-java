package com.universallogger.log4j2.spring;

/**
 * 로거 프로퍼티 인터페이스
 *
 * Spring Framework와 Spring Boot 간 로거 설정 프로퍼티를 위한 추상화 인터페이스입니다.
 * 구현체들은 환경별 프로퍼티 관리를 제공합니다.
 *
 * <p>구현체:</p>
 * <ul>
 *   <li>{@link SpringProperties} - Spring Framework용 (순수 POJO)</li>
 *   <li>{@link SpringBootProperties} - Spring Boot용 (@ConfigurationProperties 사용)</li>
 * </ul>
 *
 * @see SpringProperties
 * @see SpringBootProperties
 */
public interface LoggerProperties {

    /**
     * Universal Logger 활성화 여부 확인
     *
     * @return 활성화되어 있으면 true
     */
    boolean isEnabled();

    /**
     * 애플리케이션 이름 조회
     *
     * @return 애플리케이션 이름
     */
    String getApplicationName();

    /**
     * 애플리케이션 이름 설정
     *
     * @param applicationName 애플리케이션 이름
     */
    void setApplicationName(String applicationName);

    /**
     * 설정 파일 경로 조회
     *
     * @return 설정 파일 경로
     */
    String getConfigPath();

    /**
     * 설정 파일 경로 설정
     *
     * @param configPath 설정 파일 경로
     */
    void setConfigPath(String configPath);

    /**
     * SQL 캡처 설정 조회
     *
     * @return SQL 캡처 설정
     */
    SqlCaptureConfig getSqlCapture();

    /**
     * 활성 프로파일 해석
     *
     * <p>우선순위:</p>
     * <ol>
     *   <li>logger.profile 시스템 프로퍼티 (명시적 오버라이드, 최우선)</li>
     *   <li>Spring Environment 활성 프로파일</li>
     *   <li>null (프로파일 없음)</li>
     * </ol>
     *
     * @return 활성 프로파일 이름, 활성 프로파일이 없으면 null
     */
    String getActiveProfile();

    /**
     * SQL 캡처 설정 클래스
     */
    class SqlCaptureConfig {
        private boolean enabled = true;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }
    }
}
