package com.universallogger.logback.spring;

import com.universallogger.logback.LoggerInstance;
import com.universallogger.sql.SqlCaptureProxy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

import javax.sql.DataSource;
import java.util.HashSet;
import java.util.Set;

/**
 * Spring Boot Auto Configuration
 *
 * Automatically activated when universal-logger dependency is present.
 * Uses SpringBootProperties for @ConfigurationProperties-based configuration.
 *
 * Features:
 * - Automatic LoggerInstance bean creation
 * - Profile-based configuration support
 * - Automatic DataSource wrapping for SQL logging
 */
@AutoConfiguration
@ConditionalOnClass({LoggerInstance.class, DataSource.class})
@ConditionalOnProperty(prefix = "universal-logger", name = "enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(SpringBootProperties.class)
@Order(Ordered.HIGHEST_PRECEDENCE)
public class SpringBootAutoConfiguration implements BeanPostProcessor, ApplicationContextAware {

    private static final Logger logger = LoggerFactory.getLogger(SpringBootAutoConfiguration.class);

    @Value("${spring.application.name:application}")
    private String applicationName;

    private final LoggerProperties properties;
    private final Set<String> wrappedDataSources = new HashSet<>();
    private ApplicationContext applicationContext;

    public SpringBootAutoConfiguration(SpringBootProperties properties) {
        this.properties = properties;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    /**
     * Create LoggerInstance bean
     */
    @Bean
    @ConditionalOnMissingBean(LoggerInstance.class)
    public LoggerInstance loggerInstance() {
        logger.info("Initializing UniversalLogger (Logback) for application: {}", applicationName);

        String activeProfile = properties.getActiveProfile();
        if (activeProfile != null) {
            logger.info("Active profile: {}", activeProfile);
        }

        LoggerInstance instance = LoggerInstance.builder()
                .configPath(properties.getConfigPath())
                .profile(activeProfile)
                .build();

        logger.info("UniversalLogger (Logback) initialized successfully");
        return instance;
    }

    /**
     * Automatically wrap DataSource beans with SqlCaptureProxy
     */
    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        if (properties.getSqlCapture().isEnabled() && bean instanceof DataSource) {
            if (bean instanceof SqlCaptureProxy) {
                return bean;
            }

            if (wrappedDataSources.contains(beanName)) {
                return bean;
            }

            wrappedDataSources.add(beanName);

            LoggerInstance loggerInstance = null;
            try {
                loggerInstance = applicationContext.getBean(LoggerInstance.class);
                if (loggerInstance != null) {
                    logger.info("SQL logging enabled - DataSource '{}' wrapped", beanName);
                }
            } catch (Exception e) {
                logger.warn("LoggerInstance not found, using fallback");
            }

            return new SqlCaptureProxy((DataSource) bean, loggerInstance != null ? loggerInstance.getConfig() : null);
        }
        return bean;
    }
}
