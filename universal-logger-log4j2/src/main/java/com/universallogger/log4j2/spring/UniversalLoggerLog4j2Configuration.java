package com.universallogger.log4j2.spring;

import com.universallogger.config.LoggerConfig;
import com.universallogger.loaders.UniversalConfigLoader;
import com.universallogger.sql.SqlCaptureProxy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

import javax.sql.DataSource;
import java.util.HashSet;
import java.util.Set;

/**
 * Universal Logger Configuration for Spring (non-Boot) projects with Log4j2
 *
 * Spring Boot가 아닌 일반 Spring 프로젝트용 설정
 * 사용자가 별도로 Configuration 클래스를 만들 필요 없음!
 *
 * 사용법:
 * <pre>
 * // JavaConfig 방식
 * {@code
 * @Configuration
 * @Import(UniversalLoggerLog4j2Configuration.class)
 * public class AppConfig {
 * }
 * }
 *
 * // XML 방식
 * {@code
 * <context:component-scan base-package="com.universallogger.log4j2.spring"/>
 * }
 * </pre>
 *
 * application.properties:
 * <pre>
 * spring.application.name=MyApp
 * universal-logger-log4j2.config-path=config/universal-logger.yml
 * universal-logger-log4j2.sql-capture.enabled=true
 * </pre>
 */
@Configuration
@Order(Ordered.HIGHEST_PRECEDENCE)
public class UniversalLoggerLog4j2Configuration implements BeanPostProcessor, ApplicationContextAware {

    private static final Logger logger = LoggerFactory.getLogger(UniversalLoggerLog4j2Configuration.class);

    @Value("${spring.application.name:application}")
    private String applicationName;

    @Value("${universal-logger-log4j2.config-path:config/universal-logger.yml}")
    private String configPath;

    @Value("${universal-logger-log4j2.sql-capture.enabled:true}")
    private boolean sqlCaptureEnabled;

    private final Set<String> wrappedDataSources = new HashSet<>();
    private ApplicationContext applicationContext;
    private LoggerConfig loggerConfig;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    /**
     * LoggerConfig Bean 생성
     */
    @Bean
    public LoggerConfig loggerConfig() {
        logger.info("🚀 Initializing UniversalLogger (Log4j2) for application: {}", applicationName);

        try {
            UniversalConfigLoader configLoader = new UniversalConfigLoader();
            this.loggerConfig = configLoader.load(configPath);
            logger.info("✅ UniversalLogger (Log4j2) initialized successfully");
            return this.loggerConfig;
        } catch (Exception e) {
            logger.error("❌ Failed to initialize UniversalLogger (Log4j2)", e);
            throw new IllegalStateException("Failed to load UniversalLogger configuration", e);
        }
    }

    /**
     * DataSource를 SqlCaptureProxy로 자동 래핑
     */
    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        if (sqlCaptureEnabled && bean instanceof DataSource) {
            // 이미 SqlCaptureProxy로 래핑된 경우 스킵
            if (bean instanceof SqlCaptureProxy) {
                return bean;
            }

            // 중복 래핑 방지
            if (wrappedDataSources.contains(beanName)) {
                return bean;
            }

            wrappedDataSources.add(beanName);

            // LoggerConfig 가져오기
            LoggerConfig config = this.loggerConfig;
            if (config == null) {
                try {
                    config = applicationContext.getBean(LoggerConfig.class);
                } catch (Exception e) {
                    logger.warn("⚠️ LoggerConfig not found, using fallback");
                }
            }

            if (config != null) {
                logger.info("🔍 SQL logging enabled - DataSource '{}' wrapped", beanName);
            }

            return new SqlCaptureProxy((DataSource) bean, config);
        }
        return bean;
    }
}
