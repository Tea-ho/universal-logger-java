package com.universallogger.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.universallogger.constants.LoggerConstants.EnvironmentKeys;
import static com.universallogger.constants.LoggerConstants.PropertyKeys;

/**
 * Spring/Spring Boot 통합을 위한 프로파일 해석 유틸리티
 *
 * <p>우선순위:</p>
 * <ol>
 *   <li>logger.profile 시스템 프로퍼티 (명시적 오버라이드)</li>
 *   <li>spring.profiles.active 시스템 프로퍼티 (Spring Boot)</li>
 *   <li>SPRING_PROFILES_ACTIVE 환경 변수 (Spring Boot)</li>
 *   <li>null (프로파일 없음, 기본 설정만 로드)</li>
 * </ol>
 *
 * @see com.universallogger.loaders.UniversalConfigLoader#loadWithProfile
 */
public class ProfileResolver {

    private static final Logger logger = LoggerFactory.getLogger(ProfileResolver.class);

    /**
     * 활성 프로파일 해석
     *
     * @return 활성 프로파일 이름, 활성 프로파일이 없으면 null
     */
    public static String resolveProfile() {
        // 1. logger.profile 시스템 프로퍼티 확인 (최우선)
        String loggerProfile = System.getProperty(PropertyKeys.LOGGER_PROFILE);
        if (loggerProfile != null && !loggerProfile.trim().isEmpty()) {
            logger.info("Using profile from logger.profile: {}", loggerProfile.trim());
            return extractFirstProfile(loggerProfile.trim());
        }

        // 2. spring.profiles.active 시스템 프로퍼티 확인
        String springProfilesProperty = System.getProperty(PropertyKeys.SPRING_PROFILES_ACTIVE);
        if (springProfilesProperty != null && !springProfilesProperty.trim().isEmpty()) {
            String profile = extractFirstProfile(springProfilesProperty.trim());
            logger.info("Using profile from spring.profiles.active: {}", profile);
            return profile;
        }

        // 3. SPRING_PROFILES_ACTIVE 환경 변수 확인
        String springProfilesEnv = System.getenv(EnvironmentKeys.SPRING_PROFILES_ACTIVE);
        if (springProfilesEnv != null && !springProfilesEnv.trim().isEmpty()) {
            String profile = extractFirstProfile(springProfilesEnv.trim());
            logger.info("Using profile from SPRING_PROFILES_ACTIVE: {}", profile);
            return profile;
        }

        // 4. 프로파일 없음
        logger.info("No active profile detected, using base configuration only");
        return null;
    }

    /**
     * 쉼표로 구분된 목록에서 첫 번째 프로파일 추출
     *
     * @param profiles 쉼표로 구분된 프로파일 목록
     * @return 첫 번째 프로파일 이름
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
     * 특정 프로파일이 활성화되어 있는지 확인
     *
     * @param profile 확인할 프로파일 이름
     * @return 해당 프로파일이 활성화되어 있으면 true
     */
    public static boolean isProfileActive(String profile) {
        if (profile == null || profile.trim().isEmpty()) {
            return false;
        }

        String activeProfile = resolveProfile();
        return profile.trim().equalsIgnoreCase(activeProfile);
    }

    /**
     * 프로파일별 설정 파일 경로 생성
     *
     * <p>예시:</p>
     * <ul>
     *   <li>baseConfigPath: "META-INF/universal-logger/universal-logger.yml", profile: "prod"</li>
     *   <li>결과: "META-INF/universal-logger/universal-logger-prod.yml"</li>
     * </ul>
     *
     * @param baseConfigPath 기본 설정 파일 경로
     * @param profile 프로파일 이름
     * @return 프로파일별 설정 파일 경로
     */
    public static String buildProfileConfigPath(String baseConfigPath, String profile) {
        if (baseConfigPath == null || profile == null || profile.trim().isEmpty()) {
            return baseConfigPath;
        }

        // 디렉토리, 파일명, 확장자 추출
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
