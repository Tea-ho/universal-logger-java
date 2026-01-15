package com.universallogger.log4j2.spring;

import com.universallogger.config.LoggerConfig;
import com.universallogger.loaders.UniversalConfigLoader;
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
 * Spring Boot Auto Configuration for Log4j2
 *
 * Automatically activated when universal-logger-log4j2 dependency is present.
 * Uses SpringBootProperties for @ConfigurationProperties-based configuration.
 *
 * Features:
 * - Automatic LoggerConfig bean creation
 * - Profile-based configuration support
 * - Automatic DataSource wrapping for SQL logging
 */
@AutoConfiguration
@ConditionalOnClass({DataSource.class})
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
    private LoggerConfig loggerConfig;

    public SpringBootAutoConfiguration(SpringBootProperties properties) {
        this.properties = properties;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    /**
     * Create LoggerConfig bean
     */
    @Bean
    @ConditionalOnMissingBean(LoggerConfig.class)
    public LoggerConfig loggerConfig() {
        logger.info("Initializing UniversalLogger (Log4j2) for application: {}", applicationName);

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
        if (properties.getSqlCapture().isEnabled() && bean instanceof DataSource) {
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
        return bean;
    }
}
