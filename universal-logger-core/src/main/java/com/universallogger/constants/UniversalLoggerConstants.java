package com.universallogger.constants;

/**
 * Universal Logger Constants
 *
 * Centralized constants for configuration keys, default values, and property names.
 * This eliminates hardcoded strings and magic values throughout the codebase.
 */
public final class UniversalLoggerConstants {

    private UniversalLoggerConstants() {
        // Prevent instantiation
    }

    /**
     * Configuration file paths
     */
    public static final class ConfigPath {
        public static final String DEFAULT = "config/universal-logger.yml";
        public static final String DEFAULTS_RESOURCE = "defaults/universal-logger-defaults.yml";

        private ConfigPath() {}
    }

    /**
     * System property keys
     */
    public static final class PropertyKeys {
        // Logger profile (highest priority)
        public static final String LOGGER_PROFILE = "logger.profile";

        // Spring profile (standard Spring property)
        public static final String SPRING_PROFILES_ACTIVE = "spring.profiles.active";

        // Universal Logger configuration
        public static final String CONFIG_PATH = "universal-logger.config-path";
        public static final String SQL_CAPTURE_ENABLED = "universal-logger.sql-capture.enabled";

        private PropertyKeys() {}
    }

    /**
     * Environment variable keys
     */
    public static final class EnvironmentKeys {
        public static final String SPRING_PROFILES_ACTIVE = "SPRING_PROFILES_ACTIVE";

        private EnvironmentKeys() {}
    }

    /**
     * Spring property prefixes (for @ConfigurationProperties)
     */
    public static final class SpringPropertyPrefix {
        public static final String UNIVERSAL_LOGGER = "universal-logger";

        private SpringPropertyPrefix() {}
    }

    /**
     * Default values
     */
    public static final class Defaults {
        public static final String APPLICATION_NAME = "application";
        public static final boolean SQL_CAPTURE_ENABLED = true;

        private Defaults() {}
    }

    /**
     * Bean names
     */
    public static final class BeanNames {
        public static final String LOGGER_INSTANCE = "loggerInstance";
        public static final String LOGGER_CONFIG = "loggerConfig";

        private BeanNames() {}
    }
}
