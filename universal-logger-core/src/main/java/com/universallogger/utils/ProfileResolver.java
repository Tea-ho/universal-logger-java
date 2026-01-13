package com.universallogger.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Profile resolution utility for Spring/Spring Boot integration
 *
 * Priority order:
 * 1. logger.profile system property (explicit override)
 * 2. spring.profiles.active system property (Spring Boot)
 * 3. SPRING_PROFILES_ACTIVE environment variable (Spring Boot)
 * 4. null (no profile, load base config only)
 */
public class ProfileResolver {

    private static final Logger logger = LoggerFactory.getLogger(ProfileResolver.class);

    private static final String LOGGER_PROFILE_PROPERTY = "logger.profile";
    private static final String SPRING_PROFILES_ACTIVE_PROPERTY = "spring.profiles.active";
    private static final String SPRING_PROFILES_ACTIVE_ENV = "SPRING_PROFILES_ACTIVE";

    /**
     * Resolve active profile
     *
     * @return Active profile name, or null if no profile is active
     */
    public static String resolveProfile() {
        // 1. Check logger.profile system property (highest priority)
        String loggerProfile = System.getProperty(LOGGER_PROFILE_PROPERTY);
        if (loggerProfile != null && !loggerProfile.trim().isEmpty()) {
            logger.info("Using profile from logger.profile: {}", loggerProfile.trim());
            return extractFirstProfile(loggerProfile.trim());
        }

        // 2. Check spring.profiles.active system property
        String springProfilesProperty = System.getProperty(SPRING_PROFILES_ACTIVE_PROPERTY);
        if (springProfilesProperty != null && !springProfilesProperty.trim().isEmpty()) {
            String profile = extractFirstProfile(springProfilesProperty.trim());
            logger.info("Using profile from spring.profiles.active: {}", profile);
            return profile;
        }

        // 3. Check SPRING_PROFILES_ACTIVE environment variable
        String springProfilesEnv = System.getenv(SPRING_PROFILES_ACTIVE_ENV);
        if (springProfilesEnv != null && !springProfilesEnv.trim().isEmpty()) {
            String profile = extractFirstProfile(springProfilesEnv.trim());
            logger.info("Using profile from SPRING_PROFILES_ACTIVE: {}", profile);
            return profile;
        }

        // 4. No profile
        logger.info("No active profile detected, using base configuration only");
        return null;
    }

    /**
     * Extract first profile from comma-separated list
     *
     * Examples:
     * - "prod" -> "prod"
     * - "dev,local" -> "dev"
     * - "prod, test" -> "prod"
     *
     * @param profiles Comma-separated profile list
     * @return First profile name
     */
    private static String extractFirstProfile(String profiles) {
        if (profiles == null || profiles.trim().isEmpty()) {
            return null;
        }

        String[] profileArray = profiles.split(",");
        String firstProfile = profileArray[0].trim();

        if (profileArray.length > 1) {
            logger.debug("Multiple profiles detected: {}, using first: {}", profiles, firstProfile);
        }

        return firstProfile;
    }

    /**
     * Check if a specific profile is active
     *
     * @param profile Profile name to check
     * @return true if the profile is active
     */
    public static boolean isProfileActive(String profile) {
        if (profile == null || profile.trim().isEmpty()) {
            return false;
        }

        String activeProfile = resolveProfile();
        return profile.trim().equalsIgnoreCase(activeProfile);
    }

    /**
     * Build profile-specific config file name
     *
     * Examples:
     * - "universal-logger.yml", "prod" -> "universal-logger-prod.yml"
     * - "config/logging.yml", "dev" -> "config/logging-dev.yml"
     *
     * @param baseConfigPath Base config file path
     * @param profile Profile name
     * @return Profile-specific config file path
     */
    public static String buildProfileConfigPath(String baseConfigPath, String profile) {
        if (baseConfigPath == null || profile == null || profile.trim().isEmpty()) {
            return baseConfigPath;
        }

        // Extract directory, filename, and extension
        int lastSlash = baseConfigPath.lastIndexOf('/');
        if (lastSlash == -1) {
            lastSlash = baseConfigPath.lastIndexOf('\\');
        }

        int lastDot = baseConfigPath.lastIndexOf('.');

        String directory = lastSlash >= 0 ? baseConfigPath.substring(0, lastSlash + 1) : "";
        String filename;
        String extension;

        if (lastDot > lastSlash) {
            filename = baseConfigPath.substring(lastSlash + 1, lastDot);
            extension = baseConfigPath.substring(lastDot);
        } else {
            filename = baseConfigPath.substring(lastSlash + 1);
            extension = "";
        }

        return directory + filename + "-" + profile.trim() + extension;
    }
}
