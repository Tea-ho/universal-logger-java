package com.universallogger.loaders;

import com.universallogger.config.LoggerConfig;
import com.universallogger.parsers.ConfigParser;
import com.universallogger.parsers.YamlParser;
import com.universallogger.parsers.JsonParser;
import com.universallogger.parsers.XmlParser;

import java.io.File;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * 설정 로더 구현체
 *
 * YAML, JSON, XML 포맷을 모두 지원하는 설정 로더입니다.
 * 파일 확장자에 따라 적절한 ConfigParser를 선택하여 파싱합니다.
 * 기본적으로 defaults.yml과 병합하여 설정을 로드합니다.
 *
 * Strategy Pattern 적용:
 * - ConfigLoader: Context (이 클래스)
 * - ConfigParser: Strategy Interface
 * - YamlParser, JsonParser, XmlParser: Concrete Strategies
 *
 * 사용법:
 * <pre>
 * ConfigLoader loader = new YamlConfigLoader();
 * LoggerConfig config = loader.load("logging.yml");   // YAML
 * LoggerConfig config = loader.load("logging.json");  // JSON
 * LoggerConfig config = loader.load("logging.xml");   // XML
 * </pre>
 */
public class YamlConfigLoader implements ConfigLoader {

    private final Map<String, ConfigParser> parsers;
    private final ConfigParser defaultParser;

    /**
     * 기본 생성자
     *
     * YAML, JSON, XML 파서를 등록합니다.
     */
    public YamlConfigLoader() {
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
    public YamlConfigLoader(Map<String, ConfigParser> parsers, ConfigParser defaultParser) {
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

        // 클래스패스 리소스 처리
        if (configPath.startsWith("classpath:")) {
            String resourcePath = configPath.substring("classpath:".length());
            InputStream inputStream = getClass().getClassLoader().getResourceAsStream(resourcePath);
            if (inputStream == null) {
                throw new IllegalArgumentException("Classpath resource not found: " + resourcePath);
            }

            String extension = getFileExtension(resourcePath).toLowerCase();
            userConfig = parseFromStream(inputStream, extension);
        } else {
            // 파일 시스템 처리
            File configFile = new File(configPath);
            if (!configFile.exists()) {
                throw new IllegalArgumentException("Config file not found: " + configPath);
            }

            String extension = getFileExtension(configPath).toLowerCase();
            userConfig = parseFromFile(configFile, extension);
        }

        // Merge with defaults if requested
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

        System.out.println("[Universal Logger] Loading default configuration from JAR");
        return defaultParser.parse(defaultStream);
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
            System.out.println("[Universal Logger] Unknown file extension: " + extension +
                             ", using default YAML parser");
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
