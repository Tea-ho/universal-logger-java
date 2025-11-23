package com.universallogger.examples;

import com.universallogger.LoggerInstance;
import com.universallogger.config.LoggerConfig;
import com.universallogger.config.LoggerConfigBuilder;

/**
 * Builder 패턴으로 설정 생성 예시
 *
 * YAML/JSON 설정 파일 없이 코드에서 직접 로깅 설정을 구성할 때 사용합니다.
 * 테스트 코드나 동적 설정이 필요한 경우 유용합니다.
 */
public class BuilderExample {

    public static void main(String[] args) {
        System.out.println("=== Universal Logger - Builder 패턴 사용 예시 ===\n");

        // 1. 기본 Builder 사용
        System.out.println("1. 기본 설정으로 Builder 사용:");
        basicBuilderExample();

        // 2. 상세 설정 Builder 사용
        System.out.println("\n2. 상세 설정으로 Builder 사용:");
        detailedBuilderExample();

        // 3. 파일 로깅 설정
        System.out.println("\n3. 파일 로깅 설정:");
        fileLoggingExample();

        System.out.println("\n[완료] Builder 패턴 테스트 완료!");
    }

    /**
     * 기본 설정으로 Builder 사용
     */
    private static void basicBuilderExample() {
        LoggerConfig config = LoggerConfigBuilder.create()
            .appName("my-app")
            .rootLevel("DEBUG")
            .console()
                .enabled(true)
                .level("INFO")
                .and()
            .build();

        LoggerInstance logger = LoggerInstance.builder()
            .config(config)
            .build();

        logger.info("Builder로 생성된 로거입니다");
        logger.debug("DEBUG 레벨 (콘솔에서는 INFO 레벨이라 출력 안됨)");
        logger.warn("경고 메시지");
    }

    /**
     * 상세 설정으로 Builder 사용
     */
    private static void detailedBuilderExample() {
        LoggerConfig config = LoggerConfigBuilder.create()
            .appName("detailed-app")
            .timezone("Asia/Seoul")
            .encoding("UTF-8")
            .rootLevel("DEBUG")
            .level("com.myapp.service", "DEBUG")
            .level("com.myapp.repository", "INFO")
            .console()
                .enabled(true)
                .level("DEBUG")
                .format("[%d{HH:mm:ss}] %-5level %logger{36} - %msg%n")
                .color("DEBUG", "cyan")
                .color("INFO", "green")
                .color("WARN", "yellow")
                .color("ERROR", "red")
                .and()
            .sensitiveDataFilter(true)
            .build();

        LoggerInstance logger = LoggerInstance.builder()
            .config(config)
            .build();

        logger.debug("상세 설정이 적용된 DEBUG 메시지");
        logger.info("INFO 메시지");
        logger.info("민감정보 테스트: password=secret123");
    }

    /**
     * 파일 로깅 설정 예시
     */
    private static void fileLoggingExample() {
        LoggerConfig config = LoggerConfigBuilder.create()
            .appName("file-logging-app")
            .rootLevel("INFO")
            .console()
                .enabled(true)
                .level("INFO")
                .and()
            .file()
                .enabled(true)
                .level("DEBUG")
                .path("logs/builder-example.log")
                .maxSize("10MB")
                .maxFiles(30)
                .compress(true)
                .and()
            .errorFile()
                .enabled(true)
                .level("ERROR")
                .and()
            .build();

        LoggerInstance logger = LoggerInstance.builder()
            .config(config)
            .build();

        logger.info("파일 로깅이 설정되었습니다");
        logger.debug("DEBUG 메시지 (파일에만 기록됨)");
        logger.error("ERROR 메시지 (에러 파일에도 기록됨)");
    }
}
