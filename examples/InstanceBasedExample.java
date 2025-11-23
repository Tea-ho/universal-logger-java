package com.universallogger.examples;

import com.universallogger.LoggerInstance;
import com.universallogger.UniversalLogger;
import org.slf4j.Logger;

/**
 * 인스턴스 기반 로거 사용 예시
 *
 * DI(의존성 주입)가 필요한 경우 또는 테스트 용이성이 필요한 경우 사용합니다.
 * Spring Framework 등에서 Bean으로 등록하여 사용할 수 있습니다.
 *
 * Spring Boot 예시:
 * <pre>
 * @Configuration
 * public class LoggerConfig {
 *     @Bean
 *     public LoggerInstance logger() {
 *         return UniversalLogger.createInstance("logging.yml");
 *     }
 * }
 *
 * @Service
 * public class MyService {
 *     private final LoggerInstance logger;
 *
 *     public MyService(LoggerInstance logger) {
 *         this.logger = logger;  // DI로 주입
 *     }
 * }
 * </pre>
 */
public class InstanceBasedExample {

    public static void main(String[] args) {
        System.out.println("=== Universal Logger - 인스턴스 기반 사용 예시 ===\n");

        // 방법 1: UniversalLogger.createInstance() 사용
        System.out.println("1. createInstance()로 새 인스턴스 생성:");
        LoggerInstance logger1 = UniversalLogger.createInstance("logging.yml");
        logger1.info("첫 번째 인스턴스에서 로깅");
        logger1.debug("디버그 메시지");

        // 방법 2: Builder 패턴으로 생성
        System.out.println("\n2. Builder 패턴으로 인스턴스 생성:");
        LoggerInstance logger2 = LoggerInstance.builder()
            .configPath("logging.yml")
            .build();
        logger2.info("두 번째 인스턴스에서 로깅");

        // 인스턴스에서 SLF4J Logger 획득
        System.out.println("\n3. 인스턴스에서 SLF4J Logger 획득:");
        Logger moduleLogger = logger1.getLogger("MyModule");
        moduleLogger.info("모듈별 로거 사용 가능");
        moduleLogger.warn("경고 메시지");

        // 서비스 클래스에서 사용 예시
        System.out.println("\n4. 서비스 클래스에서 DI로 사용:");
        MyService service = new MyService(logger1);
        service.processData("테스트 데이터");

        System.out.println("\n[완료] 인스턴스 기반 로깅 테스트 완료!");
    }

    /**
     * DI로 LoggerInstance를 주입받는 서비스 클래스 예시
     */
    static class MyService {
        private final LoggerInstance logger;

        public MyService(LoggerInstance logger) {
            this.logger = logger;  // DI로 주입받음
        }

        public void processData(String data) {
            logger.info("데이터 처리 시작: {}", data);
            try {
                // 비즈니스 로직
                Thread.sleep(100);
                logger.info("데이터 처리 완료");
            } catch (Exception e) {
                logger.error("데이터 처리 실패", e);
            }
        }
    }
}
