package com.universallogger.log4j2.spring;

import com.universallogger.constants.UniversalLoggerConstants;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * Enable Universal Logger for Spring Framework projects (Log4j2)
 *
 * Add this annotation to your Spring @Configuration class to enable Universal Logger with Log4j2.
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
 * - Automatically create LoggerConfig bean
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
@Import(SpringConfiguration.class)
public @interface EnableUniversalLogger {

    /**
     * Custom config file path
     * Default: config/universal-logger.yml
     */
    String configPath() default UniversalLoggerConstants.ConfigPath.DEFAULT;

    /**
     * Enable SQL capture for DataSource beans
     * Default: true
     */
    boolean enableSqlCapture() default true;
}
