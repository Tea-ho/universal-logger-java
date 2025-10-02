package com.universallogger.examples;

import com.universallogger.UniversalLogger;
import org.slf4j.Logger;

/**
 * 자동 감지 예시 - 설정 파일 자동 로드
 *
 * 이 예시를 실행하기 전에:
 * 1. 프로젝트 루트에 universal-logger.yml 파일 생성
 * 2. 또는 config/universal-logger.yml 파일 생성
 */
public class AutoDetectionExample {

    public static void main(String[] args) {
        System.out.println("=== Universal Logger - 자동 감지 예시 ===\n");

        // 설정 파일을 자동으로 찾아 로드
        // 우선순위: universal-logger.yml > config/universal-logger.yml > logback.xml 등
        Logger logger = UniversalLogger.getLogger("AutoDetection");

        logger.info("설정 파일이 자동으로 감지되었습니다!");
        logger.debug("디버그 로그");
        logger.warn("경고 로그");
        logger.error("에러 로그");

        // 모듈별 로거 사용
        Logger dbLogger = UniversalLogger.getLogger("database");
        dbLogger.debug("데이터베이스 연결 중...");
        dbLogger.info("데이터베이스 연결 완료");

        Logger apiLogger = UniversalLogger.getLogger("api");
        apiLogger.info("API 요청 처리: GET /users");
        apiLogger.debug("응답 시간: 150ms");

        System.out.println("\n✅ 설정 파일 자동 감지 완료!");
    }
}