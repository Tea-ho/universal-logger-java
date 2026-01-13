package com.universallogger.loaders;

import com.universallogger.config.LoggerConfig;

/**
 * Configuration loader interface
 *
 * Supports profile-based configuration loading for Spring/Spring Boot integration.
 */
public interface ConfigLoader {

    /**
     * Load configuration file
     *
     * @param configPath Configuration file path
     * @return Loaded configuration object
     * @throws Exception If loading fails
     */
    LoggerConfig load(String configPath) throws Exception;

    /**
     * Load configuration file with merge option
     *
     * @param configPath Configuration file path
     * @param mergeDefaults Whether to merge with default configuration
     * @return Loaded configuration object
     * @throws Exception If loading fails
     */
    LoggerConfig load(String configPath, boolean mergeDefaults) throws Exception;

    /**
     * Load configuration with profile support
     *
     * Loads base config and merges with profile-specific config if profile is detected.
     *
     * Loading order:
     * 1. defaults/universal-logger-defaults.yml (base defaults)
     * 2. {configPath} (user base config)
     * 3. {configPath with profile suffix} (user profile config, if profile exists)
     *
     * Example:
     * - Profile: "prod"
     * - Base: config/universal-logger.yml
     * - Profile: config/universal-logger-prod.yml
     *
     * @param configPath Base configuration file path
     * @param profile Profile name (e.g., "dev", "prod", "test"), null for no profile
     * @param mergeDefaults Whether to merge with default configuration
     * @return Loaded and merged configuration object
     * @throws Exception If loading fails
     */
    LoggerConfig loadWithProfile(String configPath, String profile, boolean mergeDefaults) throws Exception;

    /**
     * Load default configuration
     * Loads embedded defaults file from JAR
     *
     * @return Default configuration object
     * @throws Exception If loading fails
     */
    LoggerConfig loadDefaultConfig() throws Exception;
}
