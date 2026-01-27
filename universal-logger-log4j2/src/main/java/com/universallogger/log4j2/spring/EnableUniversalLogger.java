package com.universallogger.log4j2.spring;

import com.universallogger.constants.LoggerConstants;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * Spring Framework 프로젝트용 Universal Logger 활성화 어노테이션 (Log4j2)
 *
 * Spring @Configuration 클래스에 이 어노테이션을 추가하여 Log4j2 기반의 Universal Logger를 활성화합니다.
 *
 * <p>사용 예시:</p>
 * <pre>{@code
 * @Configuration
 * @EnableUniversalLogger(
 *     applicationName = "MyApp",
 *     configPath = "META-INF/universal-logger/universal-logger.yml",
 *     enableSqlCapture = true
 * )
 * public class AppConfig {
 *     // 사용자 빈 정의
 * }
 * }</pre>
 *
 * <p>이 어노테이션이 수행하는 작업:</p>
 * <ul>
 *   <li>LoggerConfig 빈 자동 생성</li>
 *   <li>프로파일 기반 설정 활성화 (spring.profiles.active)</li>
 *   <li>SQL 로깅을 위한 DataSource 빈 자동 래핑 (활성화된 경우)</li>
 * </ul>
 *
 * <p>설정 우선순위:</p>
 * <ol>
 *   <li>시스템 프로퍼티 (최우선)</li>
 *   <li>@EnableUniversalLogger 어노테이션 속성</li>
 *   <li>YAML 설정 파일 (global.appName)</li>
 *   <li>기본값</li>
 * </ol>
 *
 * @see SpringConfiguration
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import(SpringConfiguration.class)
public @interface EnableUniversalLogger {

    /**
     * 애플리케이션 이름
     *
     * 로그 파일 경로의 {appName} 플레이스홀더 치환에 사용됩니다.
     * 빈 문자열("")인 경우 spring.application.name 또는 YAML 설정값이 사용됩니다.
     *
     * @return 애플리케이션 이름
     */
    String applicationName() default "";

    /**
     * 사용자 정의 설정 파일 경로
     * 기본값: META-INF/universal-logger/universal-logger.yml
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
