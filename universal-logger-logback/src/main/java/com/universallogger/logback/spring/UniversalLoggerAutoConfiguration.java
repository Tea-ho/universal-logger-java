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
 * Spring Boot Auto Configuration for UniversalLogger
 *
 * 자동으로 활성화됩니다:
 * - 의존성에 universal-logger-java가 포함되어 있으면 자동 활성화
 * - application.yml에서 설정 가능
 *
 * 사용자가 별도로 Configuration 클래스를 만들 필요 없음!
 *
 * 사용 예 (application.yml):
 * <pre>
 * universal-logger:
 *   enabled: true
 *   config-path: "config/universal-logger.yml"
 *   sql-capture:
 *     enabled: true
 * </pre>
 */
@AutoConfiguration
@ConditionalOnClass({LoggerInstance.class, DataSource.class})
@ConditionalOnProperty(prefix = "universal-logger", name = "enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(UniversalLoggerProperties.class)
@Order(Ordered.HIGHEST_PRECEDENCE)
public class UniversalLoggerAutoConfiguration implements BeanPostProcessor, ApplicationContextAware {

    private static final Logger logger = LoggerFactory.getLogger(UniversalLoggerAutoConfiguration.class);

    @Value("${spring.application.name:application}")
    private String applicationName;

    private final UniversalLoggerProperties properties;
    private final Set<String> wrappedDataSources = new HashSet<>();
    private ApplicationContext applicationContext;

    public UniversalLoggerAutoConfiguration(UniversalLoggerProperties properties) {
        this.properties = properties;
    }

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
                .configPath(properties.getConfigPath())
                .build();

        logger.info("✅ UniversalLogger initialized successfully");
        return instance;
    }

    /**
     * DataSource를 SqlCaptureProxy로 자동 래핑
     */
    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        // SQL 캡처가 활성화되고, bean이 DataSource인 경우
        if (properties.getSqlCapture().isEnabled() && bean instanceof DataSource) {
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
                    logger.info("🔍 SQL logging enabled - DataSource '{}' wrapped", beanName);
                }
            } catch (Exception e) {
                logger.warn("⚠️ LoggerInstance not found, using fallback");
            }

            return new SqlCaptureProxy((DataSource) bean, loggerInstance != null ? loggerInstance.getConfig() : null);
        }
        return bean;
    }
}
