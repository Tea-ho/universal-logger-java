package io.universallogger.loaders;

import io.universallogger.config.LoggerConfig;
import io.universallogger.constants.LoggerConstants;
import io.universallogger.parsers.ConfigParser;
import io.universallogger.parsers.YamlParser;
import io.universallogger.parsers.JsonParser;
import io.universallogger.parsers.XmlParser;
import io.universallogger.utils.ProfileResolver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * 범용 설정 로더 구현체
 *
 * YAML, JSON, XML 포맷을 모두 지원하는 설정 로더입니다.
 * 파일 확장자에 따라 적절한 ConfigParser를 선택하여 파싱합니다.
 * 기본적으로 defaults.yml과 병합하여 설정을 로드합니다.
 */
public class UniversalConfigLoader implements ConfigLoader {

    private static final Logger logger = LoggerFactory.getLogger(UniversalConfigLoader.class);

    private final Map<String, ConfigParser> parsers;
    private final ConfigParser defaultParser;

    /** SEARCH_PATHS 폴백으로 발견된 실제 경로 (loadWithProfile에서 profile 경로 생성에 사용) */
    private volatile String lastResolvedPath;

    /**
     * 기본 생성자
     *
     * YAML, JSON, XML 파서를 등록합니다.
     */
    public UniversalConfigLoader() {
        this.parsers = new HashMap<String, ConfigParser>();
        this.defaultParser = new YamlParser();

        // 파서 등록
        YamlParser yamlParser = new YamlParser();
        this.parsers.put("yml", yamlParser);
        this.parsers.put("yaml", yamlParser);
        this.parsers.put("json", new JsonParser());
        this.parsers.put("xml", new XmlParser());
    }

    /**
     * 커스텀 파서들로 생성 (테스트/확장용)
     *
     * @param parsers 확장자별 파서 맵
     * @param defaultParser 기본 파서
     */
    public UniversalConfigLoader(Map<String, ConfigParser> parsers, ConfigParser defaultParser) {
        this.parsers = parsers;
        this.defaultParser = defaultParser;
    }

    @Override
    public LoggerConfig load(String configPath) throws Exception {
        return load(configPath, true);
    }

    @Override
    public LoggerConfig load(String configPath, boolean mergeDefaults) throws Exception {
        LoggerConfig userConfig;

        // "classpath:" 프리픽스가 명시된 경우 (backward compatibility)
        if (configPath.startsWith("classpath:")) {
            String resourcePath = configPath.substring("classpath:".length());
            InputStream inputStream = getClass().getClassLoader().getResourceAsStream(resourcePath);
            if (inputStream == null) {
                throw new IllegalArgumentException("Classpath resource not found: " + resourcePath);
            }

            String extension = getFileExtension(resourcePath).toLowerCase();
            try {
                userConfig = parseFromStream(inputStream, extension);
            } finally {
                inputStream.close();
            }
        }
        // "file:" 프리픽스가 명시된 경우
        else if (configPath.startsWith("file:")) {
            String filePath = configPath.substring("file:".length());
            File configFile = new File(filePath);
            if (!configFile.exists()) {
                throw new IllegalArgumentException("File not found: " + filePath);
            }

            String extension = getFileExtension(filePath).toLowerCase();
            userConfig = parseFromFile(configFile, extension);
        }
        // 프리픽스가 없는 경우: 스마트 탐색 (classpath 우선, file system fallback)
        else {
            userConfig = searchAndLoad(configPath);
        }

        // 요청 시 기본 설정과 병합
        if (mergeDefaults) {
            LoggerConfig defaultConfig = loadDefaultConfig();
            defaultConfig.merge(userConfig);
            return defaultConfig;
        }

        return userConfig;
    }

    @Override
    public LoggerConfig loadDefaultConfig() throws Exception {
        InputStream defaultStream = getClass().getClassLoader()
                .getResourceAsStream("defaults/universal-logger-defaults.yml");

        if (defaultStream == null) {
            throw new IllegalStateException(
                "[Universal Logger] CRITICAL: defaults/universal-logger-defaults.yml not found in JAR. " +
                "The JAR may be corrupted or not built correctly."
            );
        }

        logger.info("Loading default configuration from JAR");
        try {
            return defaultParser.parse(defaultStream);
        } finally {
            defaultStream.close();
        }
    }

    @Override
    public LoggerConfig loadWithProfile(String configPath, String profile, boolean mergeDefaults) throws Exception {
        // 1. 기본 설정 로드 (없으면 defaults.yml 사용)
        logger.info("Loading base configuration: {}", configPath);
        LoggerConfig baseConfig;
        String resolvedPath = configPath; // 실제로 발견된 경로 추적
        try {
            baseConfig = load(configPath, mergeDefaults);
            // load()에서 SEARCH_PATHS 폴백으로 찾은 경우 실제 경로 확인
            if (lastResolvedPath != null) {
                resolvedPath = lastResolvedPath;
            }
        } catch (IllegalArgumentException e) {
            // 기본 설정 파일이 없으면 defaults.yml을 base로 사용
            logger.warn("Base config not found: {}, using defaults as base", configPath);
            baseConfig = loadDefaultConfig();
        }

        // 2. 프로파일이 없으면 기본 설정 반환
        if (profile == null || profile.trim().isEmpty()) {
            logger.info("No profile specified, using base configuration only");
            return baseConfig;
        }

        // 3. 실제 발견된 경로 기준으로 프로파일별 설정 파일 경로 생성
        String profileConfigPath = ProfileResolver.buildProfileConfigPath(resolvedPath, profile);
        logger.info("Attempting to load profile-specific configuration: {}", profileConfigPath);

        // 4. 프로파일별 설정 로드 시도
        try {
            LoggerConfig profileConfig = loadProfileConfig(profileConfigPath);

            if (profileConfig != null) {
                logger.info("Successfully loaded profile configuration: {}", profileConfigPath);
                // 프로파일 설정을 기본 설정에 병합
                baseConfig.merge(profileConfig);
                logger.info("Profile configuration merged successfully");
            }
        } catch (Exception e) {
            // 프로파일 설정이 없어도 허용 - 경고만 로그
            logger.warn("Profile-specific configuration not found: {} (this is optional)", profileConfigPath);
            logger.debug("Profile config load error details:", e);
        }

        return baseConfig;
    }

    /**
     * 지정 경로 → SEARCH_PATHS 순서로 설정 파일 탐색 및 로드
     *
     * classpath → filesystem 순서로 각 경로를 시도합니다.
     * 발견된 경로는 lastResolvedPath에 저장됩니다.
     *
     * @param configPath 최초 시도할 경로
     * @return 로드된 설정
     * @throws IllegalArgumentException 모든 경로에서 파일을 찾지 못한 경우
     */
    private LoggerConfig searchAndLoad(String configPath) throws Exception {
        lastResolvedPath = null;

        // 1. 지정된 configPath에서 먼저 시도
        LoggerConfig result = tryLoadFromPath(configPath);
        if (result != null) {
            lastResolvedPath = configPath;
            return result;
        }

        // 2. SEARCH_PATHS 폴백 순회
        for (String searchPath : LoggerConstants.ConfigPath.SEARCH_PATHS) {
            // 이미 시도한 경로는 건너뜀
            if (searchPath.equals(configPath)) {
                continue;
            }
            result = tryLoadFromPath(searchPath);
            if (result != null) {
                lastResolvedPath = searchPath;
                logger.info("Config found via fallback search: {}", searchPath);
                return result;
            }
        }

        // 3. 모든 경로에서 실패
        throw new IllegalArgumentException(
            "Config file not found in any search path. Tried: " + configPath +
            " and " + LoggerConstants.ConfigPath.SEARCH_PATHS.length + " fallback paths"
        );
    }

    /**
     * 단일 경로에서 설정 파일 로드 시도
     *
     * @param path 시도할 경로 (classpath: 프리픽스 포함 가능)
     * @return 로드된 설정, 파일을 찾지 못하면 null
     */
    private LoggerConfig tryLoadFromPath(String path) throws Exception {
        // "classpath:" 프리픽스가 있는 경우
        if (path.startsWith("classpath:")) {
            String resourcePath = path.substring("classpath:".length());
            InputStream inputStream = getClass().getClassLoader().getResourceAsStream(resourcePath);
            if (inputStream == null) {
                return null;
            }
            logger.info("Initialized with config: classpath:{}", resourcePath);
            String extension = getFileExtension(resourcePath).toLowerCase();
            try {
                return parseFromStream(inputStream, extension);
            } finally {
                inputStream.close();
            }
        }

        // classpath에서 시도
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream(path);
        if (inputStream != null) {
            logger.info("Initialized with config: classpath:{}", path);
            String extension = getFileExtension(path).toLowerCase();
            try {
                return parseFromStream(inputStream, extension);
            } finally {
                inputStream.close();
            }
        }

        // filesystem에서 시도
        File configFile = new File(path);
        if (configFile.exists()) {
            logger.info("Initialized with config: file:{}", path);
            String extension = getFileExtension(path).toLowerCase();
            return parseFromFile(configFile, extension);
        }

        return null;
    }

    /**
     * 프로파일별 설정 로드
     *
     * 파일이 존재하지 않으면 null 반환 (허용되는 경우)
     */
    private LoggerConfig loadProfileConfig(String profileConfigPath) throws Exception {
        // 클래스패스에서 먼저 시도
        if (profileConfigPath.startsWith("classpath:")) {
            String resourcePath = profileConfigPath.substring("classpath:".length());
            InputStream inputStream = getClass().getClassLoader().getResourceAsStream(resourcePath);

            if (inputStream == null) {
                return null; // 찾지 못함, 하지만 허용
            }

            String extension = getFileExtension(resourcePath).toLowerCase();
            try {
                return parseFromStream(inputStream, extension);
            } finally {
                inputStream.close();
            }
        }
        // 파일 시스템에서 시도
        else if (profileConfigPath.startsWith("file:")) {
            String filePath = profileConfigPath.substring("file:".length());
            File configFile = new File(filePath);

            if (!configFile.exists()) {
                return null; // 찾지 못함, 하지만 허용
            }

            String extension = getFileExtension(filePath).toLowerCase();
            return parseFromFile(configFile, extension);
        }
        // 스마트 탐색: 클래스패스 우선, 파일 시스템 폴백
        else {
            // 1. 클래스패스에서 시도
            InputStream inputStream = getClass().getClassLoader().getResourceAsStream(profileConfigPath);

            if (inputStream != null) {
                logger.debug("Found profile config in classpath: {}", profileConfigPath);
                String extension = getFileExtension(profileConfigPath).toLowerCase();
                try {
                    return parseFromStream(inputStream, extension);
                } finally {
                    inputStream.close();
                }
            }

            // 2. 파일 시스템에서 시도
            File configFile = new File(profileConfigPath);
            if (configFile.exists()) {
                logger.debug("Found profile config in file system: {}", profileConfigPath);
                String extension = getFileExtension(profileConfigPath).toLowerCase();
                return parseFromFile(configFile, extension);
            }

            // 찾지 못함
            return null;
        }
    }

    /**
     * 파일에서 설정 파싱
     *
     * 확장자에 맞는 파서를 선택하여 파싱합니다.
     */
    private LoggerConfig parseFromFile(File configFile, String extension) throws Exception {
        ConfigParser parser = selectParser(extension);
        return parser.parse(configFile);
    }

    /**
     * 스트림에서 설정 파싱
     *
     * 확장자에 맞는 파서를 선택하여 파싱합니다.
     */
    private LoggerConfig parseFromStream(InputStream inputStream, String extension) throws Exception {
        ConfigParser parser = selectParser(extension);
        return parser.parse(inputStream);
    }

    /**
     * 확장자에 맞는 파서 선택
     *
     * @param extension 파일 확장자
     * @return 선택된 파서
     */
    private ConfigParser selectParser(String extension) {
        ConfigParser parser = parsers.get(extension.toLowerCase());

        if (parser == null) {
            logger.warn("Unknown file extension: {}, using default YAML parser", extension);
            return defaultParser;
        }

        return parser;
    }

    /**
     * 파일 확장자 추출
     *
     * @param filename 파일명
     * @return 확장자 (예: "yml", "json", "xml")
     */
    private String getFileExtension(String filename) {
        int lastDotIndex = filename.lastIndexOf('.');
        if (lastDotIndex == -1) {
            return "";
        }
        return filename.substring(lastDotIndex + 1);
    }
}
