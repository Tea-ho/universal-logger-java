package com.universallogger.examples;

import com.universallogger.UniversalLogger;
import org.slf4j.Logger;

/**
 * Spring Boot 통합 예시
 *
 * Spring Boot 프로젝트에서 사용하는 방법:
 *
 * 1. Application.java (메인 클래스):
 *    @SpringBootApplication
 *    public class Application {
 *        public static void main(String[] args) {
 *            UniversalLogger.init("config/logging.yml");
 *            SpringApplication.run(Application.class, args);
 *        }
 *    }
 *
 * 2. Controller/Service 클래스:
 *    private static final Logger logger = UniversalLogger.getLogger("ClassName");
 */
public class SpringBootExample {

    private static final Logger logger = UniversalLogger.getLogger("SpringBootExample");

    public static void main(String[] args) {
        System.out.println("=== Universal Logger - Spring Boot 통합 예시 ===\n");

        // Spring Boot 시작 전 로거 초기화
        UniversalLogger.init("config/minimal.yml");

        // 컨트롤러 시뮬레이션
        simulateController();

        // 서비스 시뮬레이션
        simulateService();

        System.out.println("\n✅ Spring Boot 통합 예시 완료!");
    }

    /**
     * REST Controller 시뮬레이션
     */
    private static void simulateController() {
        Logger controllerLogger = UniversalLogger.getLogger("UserController");

        controllerLogger.info("=== REST API 호출 시뮬레이션 ===");
        controllerLogger.info("POST /api/users - 사용자 생성 요청");

        try {
            // 비즈니스 로직
            Thread.sleep(100);
            controllerLogger.info("사용자 생성 성공: userId=12345");
        } catch (Exception e) {
            controllerLogger.error("사용자 생성 실패: {}", e.getMessage(), e);
        }
    }

    /**
     * Service 계층 시뮬레이션
     */
    private static void simulateService() {
        Logger serviceLogger = UniversalLogger.getLogger("UserService");

        serviceLogger.info("=== 서비스 로직 시뮬레이션 ===");
        serviceLogger.debug("사용자 검증 시작");

        try {
            // DB 조회 시뮬레이션
            Logger dbLogger = UniversalLogger.getLogger("database");
            dbLogger.debug("SQL 실행: SELECT * FROM users WHERE id = ?");
            dbLogger.debug("쿼리 실행 시간: 45ms");

            serviceLogger.info("사용자 검증 완료");
        } catch (Exception e) {
            serviceLogger.error("서비스 처리 중 오류 발생", e);
        }
    }
}