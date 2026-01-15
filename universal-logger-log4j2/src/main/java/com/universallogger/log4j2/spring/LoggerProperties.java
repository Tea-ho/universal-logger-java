package com.universallogger.log4j2.spring;

/**
 * Logger Properties Interface
 *
 * Abstraction for logger configuration properties across Spring Framework and Spring Boot.
 * Implementations provide environment-specific property management.
 */
public interface LoggerProperties {

    /**
     * Check if Universal Logger is enabled
     *
     * @return true if enabled
     */
    boolean isEnabled();

    /**
     * Get configuration file path
     *
     * @return config file path
     */
    String getConfigPath();

    /**
     * Set configuration file path
     *
     * @param configPath config file path
     */
    void setConfigPath(String configPath);

    /**
     * Get SQL capture configuration
     *
     * @return SQL capture config
     */
    SqlCaptureConfig getSqlCapture();

    /**
     * Resolve active profile
     *
     * Priority:
     * 1. logger.profile system property (explicit override, highest priority)
     * 2. Spring Environment active profiles
     * 3. null (no profile)
     *
     * @return Active profile name, or null if no profile is active
     */
    String getActiveProfile();

    /**
     * SQL Capture Configuration
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
