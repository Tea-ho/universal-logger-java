package com.universallogger.utils;

import com.universallogger.config.LoggerConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * 설정 파일 로더
 */
public class ConfigLoader {

    /**
     * 설정 파일 로드 with default config merge
     * Loads default config from JAR, then merges user config
     */
    public static LoggerConfig load(String configPath) throws Exception {
        return load(configPath, true);
    }

    /**
     * 설정 파일 로드 (확장자 자동 감지)
     * @param configPath Configuration file path
     * @param mergeDefaults Whether to merge with default configuration
     */
    public static LoggerConfig load(String configPath, boolean mergeDefaults) throws Exception {
        LoggerConfig userConfig;

        // 클래스패스 리소스 처리
        if (configPath.startsWith("classpath:")) {
            String resourcePath = configPath.substring("classpath:".length());
            InputStream inputStream = ConfigLoader.class.getClassLoader().getResourceAsStream(resourcePath);
            if (inputStream == null) {
                throw new IllegalArgumentException("Classpath resource not found: " + resourcePath);
            }

            String extension = getFileExtension(resourcePath).toLowerCase();
            switch (extension) {
                case "yml":
                case "yaml":
                    userConfig = loadYamlFromStream(inputStream);
                    break;
                case "xml":
                    userConfig = loadXmlFromStream(inputStream);
                    break;
                case "json":
                    userConfig = loadJsonFromStream(inputStream);
                    break;
                default:
                    throw new IllegalArgumentException("Unsupported config file type: " + extension);
            }
        } else {
            // 파일 시스템 처리
            File configFile = new File(configPath);
            if (!configFile.exists()) {
                throw new IllegalArgumentException("Config file not found: " + configPath);
            }

            String extension = getFileExtension(configPath).toLowerCase();

            switch (extension) {
                case "yml":
                case "yaml":
                    userConfig = loadYaml(configFile);
                    break;
                case "xml":
                    userConfig = loadXml(configFile);
                    break;
                case "json":
                    userConfig = loadJson(configFile);
                    break;
                default:
                    throw new IllegalArgumentException("Unsupported config file type: " + extension);
            }
        }

        // Merge with defaults if requested
        if (mergeDefaults) {
            LoggerConfig defaultConfig = loadDefaultConfig();
            defaultConfig.merge(userConfig);
            return defaultConfig;
        }

        return userConfig;
    }

    /**
     * Load default configuration from JAR
     * Always loads from universal-logger-defaults.yml bundled in JAR
     */
    public static LoggerConfig loadDefaultConfig() throws Exception {
        InputStream defaultStream = ConfigLoader.class.getClassLoader()
                .getResourceAsStream("defaults/universal-logger-defaults.yml");

        if (defaultStream == null) {
            throw new IllegalStateException(
                "[Universal Logger] CRITICAL: defaults/universal-logger-defaults.yml not found in JAR. " +
                "The JAR may be corrupted or not built correctly."
            );
        }

        System.out.println("[Universal Logger] Loading default configuration from JAR");
        return loadYamlFromStream(defaultStream);
    }

    /**
     * YAML 설정 파일 로드
     */
    private static LoggerConfig loadYaml(File configFile) throws Exception {
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        return mapper.readValue(configFile, LoggerConfig.class);
    }

    private static LoggerConfig loadYamlFromStream(InputStream inputStream) throws Exception {
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        return mapper.readValue(inputStream, LoggerConfig.class);
    }

    /**
     * JSON 설정 파일 로드
     */
    private static LoggerConfig loadJson(File configFile) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(configFile, LoggerConfig.class);
    }

    private static LoggerConfig loadJsonFromStream(InputStream inputStream) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(inputStream, LoggerConfig.class);
    }

    /**
     * XML 설정 파일 로드 (표준 Logback XML 파싱)
     */
    private static LoggerConfig loadXml(File configFile) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(configFile);
        return parseXmlDocument(doc);
    }

    private static LoggerConfig loadXmlFromStream(InputStream inputStream) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(inputStream);
        return parseXmlDocument(doc);
    }

    /**
     * XML 문서 파싱 (Logback 표준 XML 형식)
     * 하드코딩 없음 - 값이 없으면 빈 설정 반환 (defaults와 merge 시 defaults 값 사용)
     */
    private static LoggerConfig parseXmlDocument(Document doc) {
        LoggerConfig config = new LoggerConfig();
        doc.getDocumentElement().normalize();

        // property 태그에서 APP_NAME 추출
        NodeList propertyList = doc.getElementsByTagName("property");
        Map<String, String> properties = new HashMap<>();
        for (int i = 0; i < propertyList.getLength(); i++) {
            Element property = (Element) propertyList.item(i);
            String name = property.getAttribute("name");
            String value = property.getAttribute("value");
            properties.put(name, value);
        }

        // 앱 이름 설정 (있으면)
        String appName = properties.get("APP_NAME");
        if (appName != null && !appName.isEmpty()) {
            config.getGlobal().setAppName(appName);
        }

        // root 로거 레벨 추출 (있으면)
        NodeList rootList = doc.getElementsByTagName("root");
        if (rootList.getLength() > 0) {
            Element root = (Element) rootList.item(0);
            String level = root.getAttribute("level");
            if (level != null && !level.isEmpty()) {
                config.getLevels().put("root", level);
            }
            // level 없으면 아무것도 하지 않음 (defaults에서 가져옴)

            // appender-ref에서 활성화된 appender 확인
            NodeList appenderRefs = root.getElementsByTagName("appender-ref");
            for (int i = 0; i < appenderRefs.getLength(); i++) {
                Element appenderRef = (Element) appenderRefs.item(i);
                String ref = appenderRef.getAttribute("ref");

                if ("CONSOLE".equals(ref)) {
                    LoggerConfig.OutputConfig consoleConfig = new LoggerConfig.OutputConfig();
                    consoleConfig.setEnabled(true);
                    // level은 defaults에서 가져옴
                    config.getOutputs().put("console", consoleConfig);
                } else if ("FILE".equals(ref)) {
                    LoggerConfig.OutputConfig fileConfig = new LoggerConfig.OutputConfig();
                    fileConfig.setEnabled(true);
                    // level, path는 defaults에서 가져옴
                    config.getOutputs().put("file", fileConfig);
                } else if ("ERROR_FILE".equals(ref)) {
                    LoggerConfig.OutputConfig errorFileConfig = new LoggerConfig.OutputConfig();
                    errorFileConfig.setEnabled(true);
                    // level, path는 defaults에서 가져옴
                    config.getOutputs().put("error_file", errorFileConfig);
                }
            }
        }

        return config;
    }

    /**
     * 파일 확장자 추출
     */
    private static String getFileExtension(String filename) {
        int lastDotIndex = filename.lastIndexOf('.');
        if (lastDotIndex == -1) {
            return "";
        }
        return filename.substring(lastDotIndex + 1);
    }
}