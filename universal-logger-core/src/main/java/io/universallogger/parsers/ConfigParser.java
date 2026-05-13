package io.universallogger.parsers;

import io.universallogger.config.LoggerConfig;
import java.io.File;
import java.io.InputStream;

/**
 * 설정 파서 인터페이스
 *
 * 다양한 포맷(YAML, JSON, XML 등)의 설정 파일을 파싱하는 인터페이스입니다.
 * Strategy Pattern을 적용하여 파싱 로직을 분리합니다.
 */
public interface ConfigParser {

    /**
     * InputStream에서 설정 파싱
     *
     * @param stream 입력 스트림
     * @return 파싱된 설정 객체
     * @throws Exception 파싱 실패 시
     */
    LoggerConfig parse(InputStream stream) throws Exception;

    /**
     * 파일에서 설정 파싱
     *
     * @param file 설정 파일
     * @return 파싱된 설정 객체
     * @throws Exception 파싱 실패 시
     */
    LoggerConfig parse(File file) throws Exception;
}
