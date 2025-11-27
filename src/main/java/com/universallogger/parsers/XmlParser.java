package com.universallogger.parsers;

import com.universallogger.config.LoggerConfig;
import org.w3c.dom.*;
import javax.xml.parsers.*;
import java.io.*;
import java.util.*;

/**
 * XML 파서 구현체
 *
 * DOM Parser를 사용하여 XML 형식의 설정을 파싱합니다.
 */
public class XmlParser implements ConfigParser {

    @Override
    public LoggerConfig parse(InputStream stream) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(stream);
        return parseDocument(doc);
    }

    @Override
    public LoggerConfig parse(File file) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(file);
        return parseDocument(doc);
    }

    /**
     * XML 문서를 LoggerConfig로 변환
     */
    private LoggerConfig parseDocument(Document doc) {
        LoggerConfig config = new LoggerConfig();
        doc.getDocumentElement().normalize();
        Element root = doc.getDocumentElement();

        // property 태그에서 값 추출
        Map<String, String> properties = parseProperties(doc);

        // 앱 이름 설정
        String appName = properties.get("APP_NAME");
        if (appName != null && !appName.isEmpty()) {
            config.getGlobal().setAppName(appName);
        }

        // root 로거 레벨 추출
        parseRootLogger(doc, config);

        return config;
    }

    /**
     * property 태그들을 Map으로 변환
     */
    private Map<String, String> parseProperties(Document doc) {
        Map<String, String> properties = new HashMap<>();
        NodeList propertyList = doc.getElementsByTagName("property");
        
        for (int i = 0; i < propertyList.getLength(); i++) {
            Element property = (Element) propertyList.item(i);
            String name = property.getAttribute("name");
            String value = property.getAttribute("value");
            properties.put(name, value);
        }
        
        return properties;
    }

    /**
     * root 로거 파싱
     */
    private void parseRootLogger(Document doc, LoggerConfig config) {
        NodeList rootList = doc.getElementsByTagName("root");
        if (rootList.getLength() == 0) {
            return;
        }

        Element root = (Element) rootList.item(0);
        
        // 로그 레벨 설정
        String level = root.getAttribute("level");
        if (level != null && !level.isEmpty()) {
            config.getLevels().put("root", level);
        }

        // appender-ref 파싱
        NodeList appenderRefs = root.getElementsByTagName("appender-ref");
        for (int i = 0; i < appenderRefs.getLength(); i++) {
            Element appenderRef = (Element) appenderRefs.item(i);
            String ref = appenderRef.getAttribute("ref");

            LoggerConfig.OutputConfig outputConfig = new LoggerConfig.OutputConfig();
            outputConfig.setEnabled(true);

            if ("CONSOLE".equals(ref)) {
                config.getOutputs().put("console", outputConfig);
            } else if ("FILE".equals(ref)) {
                config.getOutputs().put("file", outputConfig);
            } else if ("ERROR_FILE".equals(ref)) {
                config.getOutputs().put("error_file", outputConfig);
            }
        }
    }

    /**
     * Element에서 텍스트 추출
     */
    private String getElementText(Element parent, String tagName) {
        NodeList nodes = parent.getElementsByTagName(tagName);
        if (nodes.getLength() > 0) {
            return nodes.item(0).getTextContent();
        }
        return null;
    }
}
