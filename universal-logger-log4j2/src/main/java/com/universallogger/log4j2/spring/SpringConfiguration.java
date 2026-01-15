package com.universallogger.log4j2.spring;

import com.universallogger.config.LoggerConfig;
import com.universallogger.loaders.UniversalConfigLoader;
import com.universallogger.sql.SqlCaptureProxy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
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

import static com.universallogger.constants.UniversalLoggerConstants.*;

/**
 * Spring Framework Configuration for Log4j2
 *
 * Imported by @EnableUniversalLogger annotation for Spring Framework (non-Boot) projects.
 * Uses SpringProperties (plain POJO) instead of @ConfigurationProperties.
 *
 * Features:
 * - LoggerConfig bean creation
 * - Profile-based configuration support
 * - Automatic DataSource wrapping for SQL logging
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
 */
@Configuration
@Order(Ordered.HIGHEST_PRECEDENCE)
public class SpringConfiguration implements BeanPostProcessor, ApplicationContextAware, ImportAware {

    private static final Logger logger = LoggerFactory.getLogger(SpringConfiguration.class);

    @Value("${spring.application.name:application}")
    private String applicationName;

    private LoggerProperties properties;
    private final Set<String> wrappedDataSources = new HashSet<>();
    private ApplicationContext applicationContext;
    private Environment environment;
    private LoggerConfig loggerConfig;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
        this.environment = applicationContext.getEnvironment();
    }

    /**
     * Create SpringProperties bean
     */
    @Bean
    @ConditionalOnMissingBean(LoggerProperties.class)
    public SpringProperties springProperties() {
        SpringProperties props = new SpringProperties();
        if (this.environment != null) {
            props.setEnvironment(this.environment);
        }
        this.properties = props;
        return props;
    }

    @Override
    public void setImportMetadata(AnnotationMetadata importMetadata) {
        /*
         * Configuration Priority:
         * 1. System Property (highest)
         * 2. @EnableUniversalLogger annotation
         * 3. Default value
         */

        // Initialize properties if not already done
        if (this.properties == null) {
            this.properties = springProperties();
        }

        String defaultConfigPath = ConfigPath.DEFAULT;

        // Read @EnableUniversalLogger annotation attributes
        Map<String, Object> attributes = importMetadata.getAnnotationAttributes(
            EnableUniversalLogger.class.getName());

        if (attributes != null) {
            String annotationConfigPath = (String) attributes.get("configPath");
            if (annotationConfigPath != null &&
                !annotationConfigPath.trim().isEmpty() &&
                !defaultConfigPath.equals(annotationConfigPath)) {
                properties.setConfigPath(annotationConfigPath);
                logger.debug("Using config path from @EnableUniversalLogger: {}", annotationConfigPath);
            }

            Boolean annotationEnableSql = (Boolean) attributes.get("enableSqlCapture");
            if (annotationEnableSql != null) {
                properties.getSqlCapture().setEnabled(annotationEnableSql);
                logger.debug("Using SQL capture from @EnableUniversalLogger: {}", annotationEnableSql);
            }
        }

        // System property override (highest priority)
        String systemConfigPath = System.getProperty(PropertyKeys.CONFIG_PATH);
        if (systemConfigPath != null && !systemConfigPath.trim().isEmpty()) {
            properties.setConfigPath(systemConfigPath);
            logger.info("Config path overridden by system property: {}", systemConfigPath);
        }

        String systemEnableSql = System.getProperty(PropertyKeys.SQL_CAPTURE_ENABLED);
        if (systemEnableSql != null) {
            properties.getSqlCapture().setEnabled(Boolean.parseBoolean(systemEnableSql));
            logger.info("SQL capture overridden by system property: {}", systemEnableSql);
        }

        logger.info("Final configPath: {}", properties.getConfigPath());
        logger.info("Final enableSqlCapture: {}", properties.getSqlCapture().isEnabled());
    }

    /**
     * Create LoggerConfig bean
     */
    @Bean
    @ConditionalOnMissingBean(LoggerConfig.class)
    public LoggerConfig loggerConfig() {
        if (this.properties == null) {
            this.properties = springProperties();
        }

        logger.info("Initializing UniversalLogger (Log4j2) for application: {}", applicationName);
        logger.info("Config path: {}", properties.getConfigPath());

        String activeProfile = properties.getActiveProfile();
        if (activeProfile != null) {
            logger.info("Active profile: {}", activeProfile);
        }

        try {
            UniversalConfigLoader configLoader = new UniversalConfigLoader();
            this.loggerConfig = configLoader.loadWithProfile(
                properties.getConfigPath(),
                activeProfile,
                true
            );
            logger.info("UniversalLogger (Log4j2) initialized successfully");
            return this.loggerConfig;
        } catch (Exception e) {
            logger.error("Failed to initialize UniversalLogger (Log4j2)", e);
            throw new IllegalStateException("Failed to load UniversalLogger configuration", e);
        }
    }

    /**
     * Automatically wrap DataSource beans with SqlCaptureProxy
     */
    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        if (this.properties == null) {
            this.properties = springProperties();
        }

        if (!properties.getSqlCapture().isEnabled()) {
            return bean;
        }

        if (!(bean instanceof DataSource)) {
            return bean;
        }

        if (bean instanceof SqlCaptureProxy) {
            return bean;
        }

        if (wrappedDataSources.contains(beanName)) {
            return bean;
        }

        wrappedDataSources.add(beanName);

        LoggerConfig config = this.loggerConfig;
        if (config == null) {
            try {
                config = applicationContext.getBean(LoggerConfig.class);
            } catch (Exception e) {
                logger.warn("LoggerConfig not found, using fallback");
            }
        }

        if (config != null) {
            logger.info("SQL logging enabled - DataSource '{}' wrapped", beanName);
        }

        return new SqlCaptureProxy((DataSource) bean, config);
    }
}
