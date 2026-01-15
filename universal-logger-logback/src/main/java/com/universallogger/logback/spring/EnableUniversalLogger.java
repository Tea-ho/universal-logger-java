package com.universallogger.logback.spring;

import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * Enable Universal Logger for Spring Framework projects
 *
 * Add this annotation to your Spring @Configuration class to enable Universal Logger.
 *
 * Usage example:
 * <pre>
 * {@code
 * @Configuration
 * @EnableUniversalLogger
 * public class AppConfig {
 *     // your beans
 * }
 * }
 * </pre>
 *
 * This will:
 * - Automatically create LoggerInstance bean
 * - Enable profile-based configuration (spring.profiles.active)
 * - Automatically wrap DataSource beans for SQL logging (if enabled)
 *
 * Configuration:
 * - Default config path: config/universal-logger.yml
 * - Override with system property: -Duniversal-logger.config-path=custom/path.yml
 * - Profile support: -Dspring.profiles.active=dev (loads universal-logger-dev.yml)
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import(UniversalLoggerConfiguration.class)
public @interface EnableUniversalLogger {

    /**
     * Custom config file path
     * Default: config/universal-logger.yml
     */
    String configPath() default "config/universal-logger.yml";

    /**
     * Enable SQL capture for DataSource beans
     * Default: true
     */
    boolean enableSqlCapture() default true;
}
