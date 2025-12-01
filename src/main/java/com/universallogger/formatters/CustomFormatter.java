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

    private String pattern = "{timestamp} | {level} | {logger} | {message}";

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
        // 클래스명과 메서드명을 함께 표시
        String classAndMethod = getClassAndMethodName(event);

        return this.pattern
            .replace("{timestamp}", DATE_FORMATTER.format(LocalDateTime.now()))
            .replace("{level}", String.format("%-5s", event.getLevel().toString()))
            .replace("{logger}", String.format("%-15s", classAndMethod))
            .replace("{class}", getClassName(event))
            .replace("{method}", getMethodName(event))
            .replace("{line}", String.valueOf(getLineNumber(event)))
            .replace("{thread}", event.getThreadName())
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
     * 실제 호출자 정보 추출 (SQL 로깅과 동일한 방식)
     * com.universallogger 패키지를 제외하고 실제 애플리케이션 코드를 찾음
     */
    private String extractCallerInfo() {
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();

        // 우선순위: Mapper > Repository > Dao > Service > Controller > 기타
        StackTraceElement mapperElement = null;
        StackTraceElement repositoryElement = null;
        StackTraceElement daoElement = null;
        StackTraceElement serviceElement = null;
        StackTraceElement controllerElement = null;
        StackTraceElement lastValidElement = null;

        // 스택 트레이스 탐색
        for (StackTraceElement element : stackTrace) {
            String className = element.getClassName();

            // 제외할 프레임워크 클래스들
            if (className.contains("CustomFormatter") ||
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
                className.contains("$Proxy") ||
                className.contains("$$") ||
                className.contains("EnhancerBy") ||
                className.contains("Aspect") ||
                className.contains("Interceptor")) {
                continue;
            }

            // 유효한 애플리케이션 클래스 저장
            lastValidElement = element;

            // 각 계층별로 마지막으로 발견된 것 저장
            if (className.contains("Mapper")) {
                mapperElement = element;
            } else if (className.contains("Repository")) {
                repositoryElement = element;
            } else if (className.contains("Dao")) {
                daoElement = element;
            } else if (className.contains("Service")) {
                serviceElement = element;
            } else if (className.contains("Controller")) {
                controllerElement = element;
            }
        }

        // 우선순위대로 반환
        StackTraceElement targetElement = null;
        if (mapperElement != null) {
            targetElement = mapperElement;
        } else if (repositoryElement != null) {
            targetElement = repositoryElement;
        } else if (daoElement != null) {
            targetElement = daoElement;
        } else if (serviceElement != null) {
            targetElement = serviceElement;
        } else if (controllerElement != null) {
            targetElement = controllerElement;
        } else if (lastValidElement != null) {
            targetElement = lastValidElement;
        }

        if (targetElement != null) {
            String className = targetElement.getClassName();
            String methodName = targetElement.getMethodName();
            int lineNumber = targetElement.getLineNumber();
            String simpleClassName = className.substring(className.lastIndexOf('.') + 1);
            if (simpleClassName.contains("$")) {
                simpleClassName = simpleClassName.substring(0, simpleClassName.indexOf('$'));
            }
            return simpleClassName + "." + methodName + ":" + lineNumber;
        }

        return "universalLogger";
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