package com.universallogger.logback.spring;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.env.Environment;

import static com.universallogger.constants.UniversalLoggerConstants.*;

/**
 * Spring Boot Properties Implementation
 *
 * Uses @ConfigurationProperties for automatic property binding from application.yml/properties.
 * Designed for Spring Boot projects with auto-configuration.
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
        // Check if logger.profile is already explicitly set (highest priority)
        String explicitProfile = System.getProperty(PropertyKeys.LOGGER_PROFILE);
        if (explicitProfile != null && !explicitProfile.trim().isEmpty()) {
            logger.debug("Using profile from {}: {}", PropertyKeys.LOGGER_PROFILE, explicitProfile);
            return explicitProfile.trim();
        }

        // Read active profiles from Spring Environment (auto-injected in Spring Boot)
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
