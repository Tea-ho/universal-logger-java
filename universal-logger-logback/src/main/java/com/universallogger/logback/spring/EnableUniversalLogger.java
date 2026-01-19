package com.universallogger.logback.spring;

import com.universallogger.constants.LoggerConstants;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * Spring Framework 프로젝트용 Universal Logger 활성화 어노테이션 (Logback)
 *
 * Spring @Configuration 클래스에 이 어노테이션을 추가하여 Logback 기반의 Universal Logger를 활성화합니다.
 *
 * <p>사용 예시:</p>
 * <pre>{@code
 * @Configuration
 * @EnableUniversalLogger
 * public class AppConfig {
 *     // 사용자 빈 정의
 * }
 * }</pre>
 *
 * <p>이 어노테이션이 수행하는 작업:</p>
 * <ul>
 *   <li>LoggerInstance 빈 자동 생성</li>
 *   <li>프로파일 기반 설정 활성화 (spring.profiles.active)</li>
 *   <li>SQL 로깅을 위한 DataSource 빈 자동 래핑 (활성화된 경우)</li>
 * </ul>
 *
 * <p>설정 방법:</p>
 * <ul>
 *   <li>기본 설정 경로: config/universal-logger.yml</li>
 *   <li>시스템 프로퍼티로 오버라이드: -Duniversal-logger.config-path=custom/path.yml</li>
 *   <li>프로파일 지원: -Dspring.profiles.active=dev (universal-logger-dev.yml 로드)</li>
 * </ul>
 *
 * @see SpringConfiguration
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import(SpringConfiguration.class)
public @interface EnableUniversalLogger {

    /**
     * 사용자 정의 설정 파일 경로
     * 기본값: config/universal-logger.yml
     *
     * @return 설정 파일 경로
     */
    String configPath() default LoggerConstants.ConfigPath.DEFAULT;

    /**
     * DataSource 빈에 대한 SQL 캡처 활성화 여부
     * 기본값: true
     *
     * @return SQL 캡처 활성화 여부
     */
    boolean enableSqlCapture() default true;
}
