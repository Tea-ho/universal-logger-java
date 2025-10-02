package com.universallogger.spring;

import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * Enable Universal Logger for Spring (non-Boot) projects
 *
 * Usage in JavaConfig:
 * <pre>
 * {@code
 * @Configuration
 * @EnableUniversalLogger
 * public class AppConfig {
 *     // your configuration
 * }
 * }
 * </pre>
 *
 * Usage in Spring XML:
 * <pre>
 * {@code
 * <context:component-scan base-package="com.universallogger.spring"/>
 * }
 * </pre>
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import(UniversalLoggerConfiguration.class)
public @interface EnableUniversalLogger {
}
