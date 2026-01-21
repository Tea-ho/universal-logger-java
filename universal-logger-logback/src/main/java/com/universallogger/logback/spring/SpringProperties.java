package com.universallogger.logback.spring;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;

import static com.universallogger.constants.LoggerConstants.*;

/**
 * Logback용 Spring Framework 프로퍼티 구현체
 *
 * Spring Framework(비-Boot) 프로젝트를 위한 순수 POJO 구현체입니다.
 * Environment는 수동으로 주입해야 합니다.
 *
 * <p>주요 기능:</p>
 * <ul>
 *   <li>로거 활성화 여부 관리</li>
 *   <li>설정 파일 경로 관리</li>
 *   <li>SQL 캡처 설정 관리</li>
 *   <li>활성 프로파일 해석</li>
 * </ul>
 *
 * @see LoggerProperties
 * @see SpringConfiguration
 */
public class SpringProperties implements LoggerProperties {

    private static final Logger logger = LoggerFactory.getLogger(SpringProperties.class);

    private boolean enabled = true;
    private String applicationName;
    private String configPath = ConfigPath.DEFAULT;
    private SqlCaptureConfig sqlCapture = new SqlCaptureConfig();
    private Environment environment;

    /**
     * Spring Environment 설정
     *
     * Spring Framework 프로젝트에서 프로파일 해석을 위해 필요합니다.
     *
     * @param environment Spring Environment 인스턴스
     */
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public String getApplicationName() {
        return applicationName;
    }

    @Override
    public void setApplicationName(String applicationName) {
        this.applicationName = applicationName;
    }

    @Override
    public String getConfigPath() {
        return configPath;
    }

    @Override
    public void setConfigPath(String configPath) {
        this.configPath = configPath;
    }

    @Override
    public SqlCaptureConfig getSqlCapture() {
        return sqlCapture;
    }

    public void setSqlCapture(SqlCaptureConfig sqlCapture) {
        this.sqlCapture = sqlCapture;
    }

    @Override
    public String getActiveProfile() {
        // logger.profile이 이미 명시적으로 설정되어 있는지 확인 (최우선 순위)
        String explicitProfile = System.getProperty(PropertyKeys.LOGGER_PROFILE);
        if (explicitProfile != null && !explicitProfile.trim().isEmpty()) {
            logger.debug("Using profile from {}: {}", PropertyKeys.LOGGER_PROFILE, explicitProfile);
            return explicitProfile.trim();
        }

        // Spring Environment에서 활성 프로파일 읽기 (수동 주입)
        if (environment != null) {
            String[] activeProfiles = environment.getActiveProfiles();
            if (activeProfiles != null && activeProfiles.length > 0) {
                String firstProfile = activeProfiles[0];
                logger.debug("Using profile from Spring Environment: {}", firstProfile);
                return firstProfile;
            }
        }

        logger.debug("No active profile detected");
        return null;
    }
}
