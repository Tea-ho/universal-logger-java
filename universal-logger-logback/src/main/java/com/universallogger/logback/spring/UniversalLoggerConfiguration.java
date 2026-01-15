package com.universallogger.logback.spring;

import com.universallogger.logback.LoggerInstance;
import com.universallogger.sql.SqlCaptureProxy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportAware;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotationMetadata;

import javax.sql.DataSource;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Universal Logger Configuration for Spring Framework projects
 *
 * This configuration is imported by @EnableUniversalLogger annotation.
 * It provides the same functionality as UniversalLoggerAutoConfiguration but for Spring Framework (non-Boot).
 *
 * Features:
 * - Automatically creates LoggerInstance bean
 * - Supports profile-based configuration (spring.profiles.active)
 * - Automatically wraps DataSource beans for SQL logging
 *
 * Usage:
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
 * Or with custom config path:
 * <pre>
 * {@code
 * @Configuration
 * @EnableUniversalLogger(configPath = "custom/logger.yml", enableSqlCapture = false)
 * public class AppConfig {
 * }
 * }
 * </pre>
 */
@Configuration
@Order(Ordered.HIGHEST_PRECEDENCE)
public class UniversalLoggerConfiguration implements BeanPostProcessor, ApplicationContextAware, ImportAware {

    private static final Logger logger = LoggerFactory.getLogger(UniversalLoggerConfiguration.class);

    @Value("${spring.application.name:application}")
    private String applicationName;

    @Autowired
    private Environment environment;

    private ApplicationContext applicationContext;
    private String configPath = "config/universal-logger.yml";
    private boolean enableSqlCapture = true;
    private final Set<String> wrappedDataSources = new HashSet<String>();

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public void setImportMetadata(AnnotationMetadata importMetadata) {
        // Read @EnableUniversalLogger annotation attributes
        Map<String, Object> attributes = importMetadata.getAnnotationAttributes(
            EnableUniversalLogger.class.getName());

        if (attributes != null) {
            String annotationConfigPath = (String) attributes.get("configPath");
            if (annotationConfigPath != null && !annotationConfigPath.trim().isEmpty()) {
                this.configPath = annotationConfigPath;
            }

            Boolean annotationEnableSql = (Boolean) attributes.get("enableSqlCapture");
            if (annotationEnableSql != null) {
                this.enableSqlCapture = annotationEnableSql;
            }
        }

        // Allow system property override
        String systemConfigPath = System.getProperty("universal-logger.config-path");
        if (systemConfigPath != null && !systemConfigPath.trim().isEmpty()) {
            this.configPath = systemConfigPath;
            logger.info("Config path overridden by system property: {}", systemConfigPath);
        }

        String systemEnableSql = System.getProperty("universal-logger.sql-capture.enabled");
        if (systemEnableSql != null) {
            this.enableSqlCapture = Boolean.parseBoolean(systemEnableSql);
            logger.info("SQL capture overridden by system property: {}", this.enableSqlCapture);
        }
    }

    /**
     * Create LoggerInstance bean
     *
     * Automatically resolves active profile from Spring Environment:
     * 1. web.xml context-param: <param-name>spring.profiles.active</param-name>
     * 2. System property: -Dspring.profiles.active=dev
     * 3. Environment variable: SPRING_PROFILES_ACTIVE=dev
     * 4. System property: -Dlogger.profile=dev (explicit override)
     *
     * Note: @ConditionalOnMissingBean ensures compatibility with Spring Boot
     * UniversalLoggerAutoConfiguration. Only one LoggerInstance bean will be created.
     */
    @Bean
    @ConditionalOnMissingBean(LoggerInstance.class)
    public LoggerInstance loggerInstance() {
        logger.info("Initializing UniversalLogger (Logback) for application: {}", applicationName);
        logger.info("Config path: {}", configPath);

        // Set active profile as system property so ProfileResolver can detect it
        // This allows web.xml context-param to work properly
        setActiveProfileFromSpringEnvironment();

        LoggerInstance instance = LoggerInstance.builder()
                .configPath(configPath)
                .build();

        logger.info("UniversalLogger (Logback) initialized successfully");
        return instance;
    }

    /**
     * Read active profile from Spring Environment and set as system property
     *
     * Spring Environment reads from multiple sources in priority order:
     * 1. ServletContext init parameters (web.xml context-param)
     * 2. System properties (-Dspring.profiles.active)
     * 3. Environment variables (SPRING_PROFILES_ACTIVE)
     *
     * This method bridges Spring's profile detection with ProfileResolver.
     */
    private void setActiveProfileFromSpringEnvironment() {
        // Skip if logger.profile is already explicitly set
        String explicitProfile = System.getProperty("logger.profile");
        if (explicitProfile != null && !explicitProfile.trim().isEmpty()) {
            logger.info("Profile explicitly set via logger.profile: {}", explicitProfile);
            return;
        }

        // Read active profiles from Spring Environment
        String[] activeProfiles = environment.getActiveProfiles();

        if (activeProfiles != null && activeProfiles.length > 0) {
            String firstProfile = activeProfiles[0];
            logger.info("Detected active profile from Spring Environment: {}", firstProfile);

            // Set as system property for ProfileResolver
            System.setProperty("logger.profile", firstProfile);
            logger.info("Set logger.profile system property to: {}", firstProfile);
        } else {
            logger.info("No active profile detected from Spring Environment");
        }
    }

    /**
     * Automatically wrap DataSource beans with SqlCaptureProxy
     */
    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        // Skip if SQL capture is disabled
        if (!enableSqlCapture) {
            return bean;
        }

        // Only process DataSource beans
        if (!(bean instanceof DataSource)) {
            return bean;
        }

        // Skip if already wrapped
        if (bean instanceof SqlCaptureProxy) {
            return bean;
        }

        // Prevent duplicate wrapping
        if (wrappedDataSources.contains(beanName)) {
            return bean;
        }

        wrappedDataSources.add(beanName);

        // Get LoggerInstance
        LoggerInstance loggerInstance = null;
        try {
            loggerInstance = applicationContext.getBean(LoggerInstance.class);
            if (loggerInstance != null) {
                logger.info("SQL logging enabled - DataSource '{}' wrapped", beanName);
            }
        } catch (Exception e) {
            logger.warn("LoggerInstance not found, using fallback for DataSource '{}'", beanName);
        }

        return new SqlCaptureProxy((DataSource) bean, loggerInstance != null ? loggerInstance.getConfig() : null);
    }
}
