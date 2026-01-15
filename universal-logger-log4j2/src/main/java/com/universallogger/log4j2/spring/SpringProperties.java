package com.universallogger.log4j2.spring;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;

import static com.universallogger.constants.UniversalLoggerConstants.*;

/**
 * Spring Framework Properties Implementation for Log4j2
 *
 * Plain POJO for Spring Framework (non-Boot) projects.
 * Environment must be manually injected.
 */
public class SpringProperties implements LoggerProperties {

    private static final Logger logger = LoggerFactory.getLogger(SpringProperties.class);

    private boolean enabled = true;
    private String configPath = ConfigPath.DEFAULT;
    private SqlCaptureConfig sqlCapture = new SqlCaptureConfig();
    private Environment environment;

    /**
     * Set Spring Environment
     *
     * Required for profile resolution in Spring Framework projects.
     *
     * @param environment Spring Environment
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

        // Read active profiles from Spring Environment (manually injected)
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
