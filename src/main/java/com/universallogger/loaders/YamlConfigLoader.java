package com.universallogger.loaders;

import com.universallogger.config.LoggerConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * YAML 설정 로더 구현체
 *
 * YAML, JSON, XML 포맷을 모두 지원하는 설정 로더입니다.
 * 기본적으로 defaults.yml과 병합하여 설정을 로드합니다.
 *
 * 사용법:
 * <pre>
 * ConfigLoader loader = new YamlConfigLoader();
 * LoggerConfig config = loader.load("logging.yml");
 * </pre>
 */
public class YamlConfigLoader implements ConfigLoader {

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
            userConfig = loadFromStream(inputStream, extension);
        } else {
            // 파일 시스템 처리
            File configFile = new File(configPath);
            if (!configFile.exists()) {
                throw new IllegalArgumentException("Config file not found: " + configPath);
            }

            String extension = getFileExtension(configPath).toLowerCase();
            userConfig = loadFromFile(configFile, extension);
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
        return loadYamlFromStream(defaultStream);
    }

    /**
     * 파일에서 설정 로드
     */
    private LoggerConfig loadFromFile(File configFile, String extension) throws Exception {
        switch (extension) {
            case "yml":
            case "yaml":
                return loadYaml(configFile);
            case "xml":
                return loadXml(configFile);
            case "json":
                return loadJson(configFile);
            default:
                throw new IllegalArgumentException("Unsupported config file type: " + extension);
        }
    }

    /**
     * 스트림에서 설정 로드
     */
    private LoggerConfig loadFromStream(InputStream inputStream, String extension) throws Exception {
        switch (extension) {
            case "yml":
            case "yaml":
                return loadYamlFromStream(inputStream);
            case "xml":
                return loadXmlFromStream(inputStream);
            case "json":
                return loadJsonFromStream(inputStream);
            default:
                throw new IllegalArgumentException("Unsupported config file type: " + extension);
        }
    }

    // ========== YAML Loaders ==========

    private LoggerConfig loadYaml(File configFile) throws Exception {
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        return mapper.readValue(configFile, LoggerConfig.class);
    }

    private LoggerConfig loadYamlFromStream(InputStream inputStream) throws Exception {
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        return mapper.readValue(inputStream, LoggerConfig.class);
    }

    // ========== JSON Loaders ==========

    private LoggerConfig loadJson(File configFile) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(configFile, LoggerConfig.class);
    }

    private LoggerConfig loadJsonFromStream(InputStream inputStream) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(inputStream, LoggerConfig.class);
    }

    // ========== XML Loaders ==========

    private LoggerConfig loadXml(File configFile) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(configFile);
        return parseXmlDocument(doc);
    }

    private LoggerConfig loadXmlFromStream(InputStream inputStream) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(inputStream);
        return parseXmlDocument(doc);
    }

    /**
     * XML 문서 파싱 (Logback 표준 XML 형식)
     * 하드코딩 없음 - 값이 없으면 빈 설정 반환 (defaults와 merge 시 defaults 값 사용)
     */
    private LoggerConfig parseXmlDocument(Document doc) {
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

            // appender-ref에서 활성화된 appender 확인
            NodeList appenderRefs = root.getElementsByTagName("appender-ref");
            for (int i = 0; i < appenderRefs.getLength(); i++) {
                Element appenderRef = (Element) appenderRefs.item(i);
                String ref = appenderRef.getAttribute("ref");

                if ("CONSOLE".equals(ref)) {
                    LoggerConfig.OutputConfig consoleConfig = new LoggerConfig.OutputConfig();
                    consoleConfig.setEnabled(true);
                    config.getOutputs().put("console", consoleConfig);
                } else if ("FILE".equals(ref)) {
                    LoggerConfig.OutputConfig fileConfig = new LoggerConfig.OutputConfig();
                    fileConfig.setEnabled(true);
                    config.getOutputs().put("file", fileConfig);
                } else if ("ERROR_FILE".equals(ref)) {
                    LoggerConfig.OutputConfig errorFileConfig = new LoggerConfig.OutputConfig();
                    errorFileConfig.setEnabled(true);
                    config.getOutputs().put("error_file", errorFileConfig);
                }
            }
        }

        return config;
    }

    // ========== Helper Methods ==========

    private String getFileExtension(String filename) {
        int lastDotIndex = filename.lastIndexOf('.');
        if (lastDotIndex == -1) {
            return "";
        }
        return filename.substring(lastDotIndex + 1);
    }
}
