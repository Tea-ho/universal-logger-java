package com.universallogger.examples;

import com.universallogger.UniversalLogger;
import org.slf4j.Logger;

import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * 웹 애플리케이션에서의 Universal Logger Java 사용 예시
 */
public class WebAppExample {

    private Logger logger;
    private Logger apiLogger;
    private Logger dbLogger;
    private Logger securityLogger;
    private Random random = new Random();

    public WebAppExample() {
        // 로거 초기화
        UniversalLogger.init("config/logging.yml");
        this.logger = UniversalLogger.getLogger("webapp");
        this.apiLogger = UniversalLogger.getLogger("api");
        this.dbLogger = UniversalLogger.getLogger("database");
        this.securityLogger = UniversalLogger.getLogger("security");
    }

    public void start() {
        logger.info("🚀 웹 애플리케이션 시작");
        logger.info("서버 포트: 8080");
        logger.info("환경: production");
    }

    public void handleRequest(String requestId, String endpoint, String userId) {
        apiLogger.info("[{}] {} 요청 시작", requestId, endpoint);

        try {
            // 랜덤 처리 시간 시뮬레이션
            long processingTime = 100 + random.nextInt(1900); // 100~2000ms
            Thread.sleep(processingTime);

            // 가끔 에러 발생
            if (random.nextInt(10) == 0) { // 10% 확률로 에러
                apiLogger.error("[{}] {} 처리 실패", requestId, endpoint);
                throw new RuntimeException("서버 내부 오류");
            }

            apiLogger.info("[{}] {} 완료 (처리시간: {}ms)", requestId, endpoint, processingTime);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            apiLogger.error("[{}] {} 인터럽트 발생", requestId, endpoint);
        } catch (RuntimeException e) {
            apiLogger.error("[{}] {} 런타임 에러: {}", requestId, endpoint, e.getMessage());
            throw e;
        }
    }

    public void databaseOperation(String operation, String table) {
        dbLogger.debug("DB 작업 시작: {} on {}", operation, table);

        try {
            // 가끔 느린 쿼리 시뮬레이션
            if (random.nextInt(5) == 0) { // 20% 확률로 느린 쿼리
                long queryTime = 3000 + random.nextInt(2000); // 3~5초
                Thread.sleep(queryTime);
                dbLogger.warn("느린 쿼리 감지: {} on {} ({}ms)", operation, table, queryTime);
            } else {
                Thread.sleep(10 + random.nextInt(90)); // 10~100ms
                dbLogger.debug("DB 작업 완료: {} on {}", operation, table);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public void securityCheck(String ipAddress, String userAgent) {
        // 의심스러운 IP 체크
        String[] suspiciousIps = {"192.168.1.100", "10.0.0.50"};
        for (String suspiciousIp : suspiciousIps) {
            if (suspiciousIp.equals(ipAddress)) {
                securityLogger.warn("의심스러운 IP 접근: {}", ipAddress);
                break;
            }
        }

        // 봇 탐지
        if (userAgent.toLowerCase().contains("bot") || userAgent.toLowerCase().contains("crawler")) {
            securityLogger.info("봇 접근 감지: {}", userAgent);
        }

        // 가끔 보안 위협 시뮬레이션
        if (random.nextInt(20) == 0) { // 5% 확률
            securityLogger.error("보안 위협 감지! IP: {}", ipAddress);
        }
    }

    public void simulateTraffic() {
        String[] endpoints = {"/api/users", "/api/products", "/api/orders", "/login", "/dashboard"};
        String[] ips = {"192.168.1.10", "192.168.1.20", "192.168.1.100", "10.0.0.50"};
        String[] userAgents = {
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36",
            "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36",
            "Googlebot/2.1 (+http://www.google.com/bot.html)",
            "Mozilla/5.0 (compatible; YandexBot/3.0)"
        };

        for (int i = 0; i < 20; i++) {
            String requestId = String.format("req_%04d", i);
            String endpoint = endpoints[random.nextInt(endpoints.length)];
            String ip = ips[random.nextInt(ips.length)];
            String userAgent = userAgents[random.nextInt(userAgents.length)];

            try {
                // 보안 검사
                securityCheck(ip, userAgent);

                // 데이터베이스 작업
                if (endpoint.contains("api")) {
                    databaseOperation("SELECT", "users");
                }

                // 요청 처리
                handleRequest(requestId, endpoint, null);

            } catch (Exception e) {
                logger.error("요청 처리 실패: {}", e.getMessage());
            }

            // 잠시 대기
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    public static void main(String[] args) {
        System.out.println("=== 웹 애플리케이션 로깅 시뮬레이션 ===\n");

        WebAppExample app = new WebAppExample();
        app.start();

        System.out.println("트래픽 시뮬레이션 시작... (약 5초 소요)");
        app.simulateTraffic();

        System.out.println("\n시뮬레이션 완료!");
        System.out.println("생성된 로그 파일들:");
        System.out.println("- logs/JavaApp_*.log (전체 로그)");
        System.out.println("- logs/errors_*.log (에러 로그)");
    }
}