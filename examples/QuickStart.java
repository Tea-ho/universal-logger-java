package com.universallogger.examples;

import com.universallogger.UniversalLogger;

/**
 * Universal Logger Java 빠른 시작 예시 - 3줄로 끝!
 */
public class QuickStart {

    public static void main(String[] args) {
        System.out.println("=== Universal Logger Java 빠른 시작 ===\n");

        // 1줄: 설정 파일로 초기화
        UniversalLogger.init("config/simple.yml");

        // 2줄: 로그 사용
        UniversalLogger.info("Hello Universal Logger Java!");
        UniversalLogger.error("뭔가 잘못됐어요!");

        // 3줄: 모듈별 로거 사용
        var dbLogger = UniversalLogger.getLogger("database");
        dbLogger.warn("데이터베이스 연결이 느립니다");

        System.out.println("\n끝! 이제 콘솔 출력을 확인해보세요.");
    }
}