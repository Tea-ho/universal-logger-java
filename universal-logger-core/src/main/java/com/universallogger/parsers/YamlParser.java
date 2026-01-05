package com.universallogger.parsers;

import com.universallogger.config.LoggerConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import java.io.File;
import java.io.InputStream;

/**
 * YAML 파서 구현체
 *
 * Jackson YAMLFactory를 사용하여 YAML 형식의 설정을 파싱합니다.
 */
public class YamlParser implements ConfigParser {

    private final ObjectMapper mapper;

    public YamlParser() {
        this.mapper = new ObjectMapper(new YAMLFactory());
    }

    @Override
    public LoggerConfig parse(InputStream stream) throws Exception {
        return mapper.readValue(stream, LoggerConfig.class);
    }

    @Override
    public LoggerConfig parse(File file) throws Exception {
        return mapper.readValue(file, LoggerConfig.class);
    }
}
