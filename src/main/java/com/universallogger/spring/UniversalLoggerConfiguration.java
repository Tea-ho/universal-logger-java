package com.universallogger.spring;

import com.universallogger.LoggerInstance;
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
 * Universal Logger Configuration for Spring (non-Boot) projects
 *
 * Spring Boot가 아닌 일반 Spring 프로젝트용 설정
 * 사용자가 별도로 Configuration 클래스를 만들 필요 없음!
 *
 * 사용법:
 * <pre>
 * // JavaConfig 방식
 * {@code
 * @Configuration
 * @Import(UniversalLoggerConfiguration.class)
 * public class AppConfig {
 * }
 * }
 *
 * // XML 방식
 * {@code
 * <context:component-scan base-package="com.universallogger.spring"/>
 * }
 * </pre>
 *
 * application.properties:
 * <pre>
 * spring.application.name=MyApp
 * universal-logger.config-path=config/universal-logger.yml
 * universal-logger.sql-capture.enabled=true
 * </pre>
 */
@Configuration
@Order(Ordered.HIGHEST_PRECEDENCE)
public class UniversalLoggerConfiguration implements BeanPostProcessor, ApplicationContextAware {

    private static final Logger logger = LoggerFactory.getLogger(UniversalLoggerConfiguration.class);

    @Value("${spring.application.name:application}")
    private String applicationName;

    @Value("${universal-logger.config-path:config/universal-logger.yml}")
    private String configPath;

    @Value("${universal-logger.sql-capture.enabled:true}")
    private boolean sqlCaptureEnabled;

    private final Set<String> wrappedDataSources = new HashSet<>();
    private ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    /**
     * LoggerInstance Bean 생성
     */
    @Bean
    public LoggerInstance loggerInstance() {
        logger.info("🚀 Initializing UniversalLogger for application: {}", applicationName);

        LoggerInstance instance = LoggerInstance.builder()
                .configPath(configPath)
                .build();

        logger.info("✅ UniversalLogger initialized successfully");
        return instance;
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

            // LoggerInstance 가져오기
            LoggerInstance loggerInstance = null;
            try {
                loggerInstance = applicationContext.getBean(LoggerInstance.class);
                if (loggerInstance != null) {
                    loggerInstance.info("🔍 SQL logging enabled - DataSource '{}' wrapped", beanName);
                }
            } catch (Exception e) {
                logger.warn("⚠️ LoggerInstance not found, using fallback");
            }

            return new SqlCaptureProxy((DataSource) bean, loggerInstance);
        }
        return bean;
    }
}
