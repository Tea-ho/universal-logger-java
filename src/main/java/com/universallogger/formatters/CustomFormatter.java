package com.universallogger.formatters;

import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.spi.ILoggingEvent;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 커스텀 포맷터
 */
public class CustomFormatter extends PatternLayoutEncoder {

    private static final DateTimeFormatter DATE_FORMATTER =
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private String pattern = "[{timestamp}] [{level}] [{thread}] [{logger}] {message}";

    public void setPattern(String pattern) {
        if (pattern != null && !pattern.trim().isEmpty()) {
            this.pattern = pattern;
        }
    }

    @Override
    public byte[] encode(ILoggingEvent event) {
        String formattedMessage = formatMessage(event);
        return (formattedMessage + "\n").getBytes(StandardCharsets.UTF_8);
    }

    /**
     * 메시지 포맷팅
     */
    private String formatMessage(ILoggingEvent event) {
        // 로거 정보 생성
        String loggerInfo = getLoggerInfo(event);

        return this.pattern
            .replace("{timestamp}", DATE_FORMATTER.format(LocalDateTime.now()))
            .replace("{level}", String.format("%-5s", event.getLevel().toString()))
            .replace("{thread}", event.getThreadName())
            .replace("{logger}", loggerInfo)
            .replace("{class}", getClassName(event))
            .replace("{method}", getMethodName(event))
            .replace("{line}", String.valueOf(getLineNumber(event)))
            .replace("{message}", event.getFormattedMessage());
    }

    /**
     * 클래스명 추출 (전체 패키지 경로)
     */
    private String getClassName(ILoggingEvent event) {
        StackTraceElement[] stack = event.getCallerData();
        if (stack != null && stack.length > 0) {
            return stack[0].getClassName();
        }
        return event.getLoggerName();
    }

    /**
     * 로거 정보 반환
     * SQL 로깅: SqlCaptureProxy가 설정한 로거 이름 사용 (클래스.메서드:줄)
     * 일반 로깅: 스택 트레이스 분석으로 호출자 추출 (클래스.메서드:줄)
     */
    private String getLoggerInfo(ILoggingEvent event) {
        String loggerName = event.getLoggerName();

        // SQL 로깅인지 확인 (SqlCaptureProxy가 "클래스명.메서드명:줄번호" 형식으로 전달)
        if (loggerName != null && loggerName.contains(":") && loggerName.contains(".")) {
            // SQL 로깅의 경우: 이미 SqlCaptureProxy가 설정한 로거 이름 사용
            return loggerName;
        }

        // 일반 로깅의 경우 스택 트레이스를 직접 분석
        return extractCallerInfo();
    }

    /**
     * 클래스명과 메서드명을 함께 반환 (간단한 클래스명.메서드명)
     * SQL 로깅의 경우 로거 이름에 실제 호출자 정보가 포함되어 있음
     * 일반 로깅의 경우 스택 트레이스를 분석하여 실제 호출자를 찾음
     */
    private String getClassAndMethodName(ILoggingEvent event) {
        String loggerName = event.getLoggerName();

        // SQL 로깅인지 확인 (SqlCaptureProxy가 "클래스명.메서드명:줄번호" 형식으로 전달)
        if (loggerName != null && loggerName.contains(":") && loggerName.contains(".")) {
            // SQL 로깅의 경우 로거 이름에 이미 호출자 정보가 포함되어 있음
            return loggerName;
        }

        // 일반 로깅의 경우 스택 트레이스를 직접 분석
        return extractCallerInfo();
    }

    /**
     * 실제 호출자 정보 추출
     * 표준 로깅 라이브러리 방식: 순방향 탐색하여 첫 번째 애플리케이션 클래스 반환
     */
    private String extractCallerInfo() {
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();

        // 순방향 탐색: 프레임워크 클래스를 제외한 첫 번째 애플리케이션 클래스
        for (StackTraceElement element : stackTrace) {
            String className = element.getClassName();

            // 프레임워크/라이브러리 클래스 제외
            if (isFrameworkClass(className)) {
                continue;
            }

            // 첫 번째 애플리케이션 클래스 발견
            String methodName = element.getMethodName();
            int lineNumber = element.getLineNumber();
            String simpleClassName = className.substring(className.lastIndexOf('.') + 1);

            // 내부 클래스 처리 (예: MyClass$1 -> MyClass)
            if (simpleClassName.contains("$")) {
                simpleClassName = simpleClassName.substring(0, simpleClassName.indexOf('$'));
            }

            return simpleClassName + "." + methodName + ":" + lineNumber;
        }

        return "universalLogger";
    }

    /**
     * 프레임워크/라이브러리 클래스 여부 판단
     * 표준 로깅 라이브러리(Logback, Log4j2)와 동일한 방식
     */
    private boolean isFrameworkClass(String className) {
        return className.contains("CustomFormatter") ||
               className.contains("ColorFormatter") ||
               className.startsWith("com.universallogger") ||
               className.startsWith("java.lang.Thread") ||
               className.startsWith("java.") ||
               className.startsWith("javax.") ||
               className.startsWith("jakarta.") ||
               className.startsWith("sun.") ||
               className.startsWith("com.sun.") ||
               className.startsWith("jdk.") ||
               className.startsWith("org.slf4j") ||
               className.startsWith("ch.qos.logback") ||
               className.startsWith("org.apache.ibatis") ||
               className.startsWith("org.mybatis") ||
               className.startsWith("org.hibernate") ||
               className.startsWith("org.springframework.aop") ||
               className.startsWith("org.springframework.cglib") ||
               className.startsWith("org.springframework.transaction") ||
               className.startsWith("org.springframework.web") ||
               className.startsWith("org.springframework.data") ||
               className.startsWith("org.springframework.orm") ||
               className.contains("RepositoryComposition") ||
               className.contains("TaskThread") ||
               className.contains("$Proxy") ||
               className.contains("$$") ||
               className.contains("EnhancerBy") ||
               className.contains("Aspect") ||
               className.contains("Interceptor") ||
               className.startsWith("com.zaxxer.hikari");
    }

    /**
     * 메서드명 추출
     */
    private String getMethodName(ILoggingEvent event) {
        StackTraceElement[] stack = event.getCallerData();
        if (stack != null && stack.length > 0) {
            return stack[0].getMethodName();
        }
        return "universalLogger";
    }

    /**
     * 라인 번호 추출
     */
    private int getLineNumber(ILoggingEvent event) {
        StackTraceElement[] stack = event.getCallerData();
        if (stack != null && stack.length > 0) {
            return stack[0].getLineNumber();
        }
        return 0;
    }
}