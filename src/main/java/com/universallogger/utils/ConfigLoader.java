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
     * 설정 파일 로드 (확장자 자동 감지)
     */
    public static LoggerConfig load(String configPath) throws Exception {
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
                    return loadYamlFromStream(inputStream);
                case "xml":
                    return loadXmlFromStream(inputStream);
                case "json":
                    return loadJsonFromStream(inputStream);
                default:
                    throw new IllegalArgumentException("Unsupported config file type: " + extension);
            }
        }

        // 파일 시스템 처리
        File configFile = new File(configPath);
        if (!configFile.exists()) {
            throw new IllegalArgumentException("Config file not found: " + configPath);
        }

        String extension = getFileExtension(configPath).toLowerCase();

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

        // 앱 이름 설정
        String appName = properties.getOrDefault("APP_NAME", "JavaApp");
        config.getGlobal().setAppName(appName);

        // root 로거 레벨 추출
        NodeList rootList = doc.getElementsByTagName("root");
        if (rootList.getLength() > 0) {
            Element root = (Element) rootList.item(0);
            String level = root.getAttribute("level");
            if (level != null && !level.isEmpty()) {
                config.getLevels().put("root", level);
            } else {
                config.getLevels().put("root", "INFO");
            }

            // appender-ref에서 활성화된 appender 확인
            NodeList appenderRefs = root.getElementsByTagName("appender-ref");
            for (int i = 0; i < appenderRefs.getLength(); i++) {
                Element appenderRef = (Element) appenderRefs.item(i);
                String ref = appenderRef.getAttribute("ref");

                if ("CONSOLE".equals(ref)) {
                    LoggerConfig.OutputConfig consoleConfig = new LoggerConfig.OutputConfig();
                    consoleConfig.setEnabled(true);
                    consoleConfig.setLevel(config.getLevels().getOrDefault("root", "INFO"));
                    config.getOutputs().put("console", consoleConfig);
                } else if ("FILE".equals(ref)) {
                    LoggerConfig.OutputConfig fileConfig = new LoggerConfig.OutputConfig();
                    fileConfig.setEnabled(true);
                    fileConfig.setLevel("DEBUG");
                    fileConfig.setPath("logs/" + appName + ".log");
                    config.getOutputs().put("file", fileConfig);
                } else if ("ERROR_FILE".equals(ref)) {
                    LoggerConfig.OutputConfig errorFileConfig = new LoggerConfig.OutputConfig();
                    errorFileConfig.setEnabled(true);
                    errorFileConfig.setLevel("ERROR");
                    errorFileConfig.setPath("logs/" + appName + "-error.log");
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

    /**
     * 기본 설정 생성
     */
    public static LoggerConfig createDefaultConfig() {
        LoggerConfig config = new LoggerConfig();

        // 글로벌 설정
        config.getGlobal().setAppName("JavaApp");

        // 레벨 설정
        config.getLevels().put("root", "INFO");

        // 콘솔 출력 설정
        LoggerConfig.OutputConfig consoleConfig = new LoggerConfig.OutputConfig();
        consoleConfig.setEnabled(true);
        consoleConfig.setLevel("INFO");
        consoleConfig.setFormat("[{timestamp}] {level} - {message}");
        consoleConfig.getColors().put("DEBUG", "cyan");
        consoleConfig.getColors().put("INFO", "green");
        consoleConfig.getColors().put("WARN", "yellow");
        consoleConfig.getColors().put("ERROR", "red");
        config.getOutputs().put("console", consoleConfig);

        // 파일 출력 설정
        LoggerConfig.OutputConfig fileConfig = new LoggerConfig.OutputConfig();
        fileConfig.setEnabled(false);
        fileConfig.setLevel("DEBUG");
        fileConfig.setPath("logs/{app_name}_{date}.log");
        fileConfig.setFormat("{timestamp} | {level} | {logger} | {message}");
        config.getOutputs().put("file", fileConfig);

        return config;
    }
}