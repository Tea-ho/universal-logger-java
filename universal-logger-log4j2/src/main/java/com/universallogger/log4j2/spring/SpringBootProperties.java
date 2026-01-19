package com.universallogger.log4j2.spring;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.env.Environment;

import static com.universallogger.constants.LoggerConstants.*;

/**
 * Log4j2용 Spring Boot 프로퍼티 구현체
 *
 * application.yml/properties에서 자동 프로퍼티 바인딩을 위해 @ConfigurationProperties를 사용합니다.
 * 자동 설정을 지원하는 Spring Boot 프로젝트용으로 설계되었습니다.
 *
 * <p>설정 예시 (application.yml):</p>
 * <pre>{@code
 * universal-logger:
 *   enabled: true
 *   config-path: config/universal-logger.yml
 *   sql-capture:
 *     enabled: true
 * }</pre>
 *
 * @see LoggerProperties
 * @see SpringBootConfiguration
 */
@ConfigurationProperties(prefix = "universal-logger")
public class SpringBootProperties implements LoggerProperties {

    private static final Logger logger = LoggerFactory.getLogger(SpringBootProperties.class);

    private boolean enabled = true;
    private String configPath = ConfigPath.DEFAULT;
    private SqlCaptureConfig sqlCapture = new SqlCaptureConfig();

    @Autowired(required = false)
    private Environment environment;

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
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

        // Spring Environment에서 활성 프로파일 읽기 (Spring Boot에서 자동 주입)
        if (environment != null) {
            String[] activeProfiles = environment.getActiveProfiles();
            if (activeProfiles != null && activeProfiles.length > 0) {
                String firstProfile = activeProfiles[0];
                logger.debug("sing profile from Spring Environment: {}", firstProfile);
                return firstProfile;
            }
        }

        logger.debug("No active profile detected");
        return null;
    }
}
