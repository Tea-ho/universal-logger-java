package com.universallogger.examples;

import com.universallogger.UniversalLogger;
import org.slf4j.Logger;

/**
 * Universal Logger Java 기본 사용 예시
 */
public class BasicUsage {

    public static void main(String[] args) {
        System.out.println("=== Universal Logger Java 기본 사용 예시 ===\n");

        // 1. 설정 파일로 초기화
        System.out.println("1. 설정 파일로 로거 초기화...");
        UniversalLogger.init("logging.yml");

        // 2. 기본 로그 메시지들
        System.out.println("\n2. 기본 로그 메시지 테스트:");
        UniversalLogger.debug("이것은 디버그 메시지입니다");
        UniversalLogger.info("애플리케이션이 시작되었습니다");
        UniversalLogger.warn("이것은 경고 메시지입니다");
        UniversalLogger.error("에러가 발생했습니다!");

        // 3. 모듈별 로거 사용
        System.out.println("\n3. 모듈별 로거 사용:");
        Logger dbLogger = UniversalLogger.getLogger("database");
        Logger apiLogger = UniversalLogger.getLogger("api");
        Logger securityLogger = UniversalLogger.getLogger("security");

        dbLogger.info("데이터베이스 연결 성공");
        apiLogger.info("API 요청 처리 중...");
        securityLogger.warn("의심스러운 접근 시도 감지");

        // 4. 민감한 정보 마스킹 테스트
        System.out.println("\n4. 민감한 정보 마스킹 테스트:");
        UniversalLogger.info("사용자 로그인: username=admin, password=secret123");
        UniversalLogger.info("API 토큰: token=abc123def456");
        UniversalLogger.info("신용카드: 1234-5678-9012-3456");

        // 5. 예외 처리
        System.out.println("\n5. 예외 처리:");
        try {
            // 의도적으로 에러 발생
            int result = 10 / 0;
        } catch (Exception e) {
            UniversalLogger.error("계산 오류", e);
        }

        // 6. 포맷팅된 로그
        System.out.println("\n6. 포맷팅된 로그:");
        String username = "김개발";
        int age = 30;
        UniversalLogger.info("사용자 정보: 이름={}, 나이={}", username, age);

        System.out.println("\n로그 파일들이 logs/ 디렉토리에 생성되었습니다.");
    }
}