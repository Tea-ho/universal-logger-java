package io.universallogger.parsers;

import io.universallogger.config.LoggerConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.InputStream;

/**
 * JSON 파서 구현체
 *
 * Jackson ObjectMapper를 사용하여 JSON 형식의 설정을 파싱합니다.
 */
public class JsonParser implements ConfigParser {

    private final ObjectMapper mapper;

    public JsonParser() {
        this.mapper = new ObjectMapper();
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
