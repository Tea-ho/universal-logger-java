package com.universallogger.examples;

import com.universallogger.UniversalLogger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Logback 호환성 예시
 *
 * 기존 Logback을 사용하는 프로젝트에서도
 * Universal Logger를 그대로 사용할 수 있습니다.
 */
public class LogbackCompatibilityExample {

    // 방법 1: SLF4J + UniversalLogger (권장)
    private static final Logger universalLogger =
        UniversalLogger.getLogger("UniversalStyle");

    // 방법 2: 기존 Logback 방식 (기존 코드 수정 불필요)
    private static final Logger logbackLogger =
        LoggerFactory.getLogger(LogbackCompatibilityExample.class);

    // 방법 3: Logback 직접 사용 (기존 프로젝트)
    private static final ch.qos.logback.classic.Logger directLogback =
        (ch.qos.logback.classic.Logger) LoggerFactory.getLogger("DirectLogback");

    public static void main(String[] args) {
        System.out.println("=== Logback 호환성 테스트 ===\n");

        // Universal Logger 초기화 (한 번만!)
        UniversalLogger.init("config/minimal.yml");

        System.out.println("1. SLF4J + UniversalLogger 방식:");
        testUniversalLogger();

        System.out.println("\n2. 기존 Logback 방식 (LoggerFactory 사용):");
        testLogbackStyle();

        System.out.println("\n3. Logback 직접 사용:");
        testDirectLogback();

        System.out.println("\n✅ 모든 방식이 동일한 설정으로 동작합니다!");
        System.out.println("   → logging.yml의 설정이 모두에게 적용됨");
    }

    /**
     * 방법 1: UniversalLogger 스타일
     */
    private static void testUniversalLogger() {
        universalLogger.info("UniversalLogger 스타일로 로그 출력");
        universalLogger.debug("디버그 레벨 메시지");
        universalLogger.warn("경고 메시지");
    }

    /**
     * 방법 2: 기존 Logback 스타일 (변경 불필요)
     */
    private static void testLogbackStyle() {
        logbackLogger.info("기존 Logback 코드가 그대로 동작");
        logbackLogger.debug("기존 코드 수정 불필요!");
        logbackLogger.error("에러 메시지도 정상 출력");
    }

    /**
     * 방법 3: Logback 직접 사용
     */
    private static void testDirectLogback() {
        directLogback.info("Logback을 직접 사용해도 문제없음");
        directLogback.debug("Universal Logger 설정 적용됨");
        directLogback.warn("모두 같은 Logback 인스턴스 사용");
    }

    /**
     * 실제 사용 예시: 서비스 클래스
     */
    public static class UserService {
        // 기존 프로젝트에서 이미 사용 중인 코드
        private static final Logger logger =
            LoggerFactory.getLogger(UserService.class);

        public void createUser(String username) {
            // 기존 코드 그대로 사용
            logger.info("사용자 생성 시작: {}", username);

            try {
                // 비즈니스 로직
                logger.debug("사용자 검증 중...");
                logger.info("사용자 생성 완료: {}", username);
            } catch (Exception e) {
                logger.error("사용자 생성 실패", e);
            }
        }
    }

    /**
     * 실제 사용 예시: 혼용
     */
    public static class MixedService {
        // 새 코드: UniversalLogger 사용
        private static final Logger newLogger =
            UniversalLogger.getLogger("NewFeature");

        // 기존 코드: Logback 사용
        private static final Logger oldLogger =
            LoggerFactory.getLogger(MixedService.class);

        public void processData() {
            // 둘 다 같은 설정으로 동작!
            newLogger.info("새 기능 실행 (UniversalLogger)");
            oldLogger.info("기존 기능 실행 (Logback)");
            // → 모두 logging.yml 설정 적용됨
        }
    }
}
