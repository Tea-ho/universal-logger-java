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
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Logback용 Spring Boot 자동 설정 클래스
 *
 * universal-logger-logback 의존성이 존재할 때 자동으로 활성화됩니다.
 * @ConfigurationProperties 기반의 SpringBootProperties를 사용합니다.
 *
 * <p>주요 기능:</p>
 * <ul>
 *   <li>LoggerInstance 빈 자동 생성</li>
 *   <li>프로파일 기반 설정 지원</li>
 *   <li>SQL 로깅을 위한 DataSource 자동 래핑</li>
 * </ul>
 *
 * @see SpringBootProperties
 * @see LoggerInstance
 */
@AutoConfiguration
@ConditionalOnClass({LoggerInstance.class, DataSource.class})
@ConditionalOnProperty(prefix = "universal-logger", name = "enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(SpringBootProperties.class)
@Order(Ordered.HIGHEST_PRECEDENCE)
public class SpringBootConfiguration implements BeanPostProcessor, ApplicationContextAware {

    private static final Logger logger = LoggerFactory.getLogger(SpringBootConfiguration.class);

    @Value("${spring.application.name:application}")
    private String applicationName;

    private final LoggerProperties properties;
    private final Set<String> wrappedDataSources = ConcurrentHashMap.newKeySet();
    private ApplicationContext applicationContext;

    public SpringBootConfiguration(SpringBootProperties properties) {
        this.properties = properties;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    /**
     * LoggerInstance 빈 생성
     *
     * @return LoggerInstance 인스턴스
     */
    @Bean
    @ConditionalOnMissingBean(LoggerInstance.class)
    public LoggerInstance loggerInstance() {
        // applicationName 결정: properties > spring.application.name
        String appName = properties.getApplicationName();
        if (appName == null || appName.isEmpty()) {
            appName = applicationName;
        }

        logger.info("Initializing UniversalLogger (Logback) for application: {}", appName);

        String activeProfile = properties.getActiveProfile();
        if (activeProfile != null) {
            logger.info("Active profile: {}", activeProfile);
        }

        LoggerInstance.Builder builder = LoggerInstance.builder()
                .configPath(properties.getConfigPath())
                .profile(activeProfile);

        // applicationName이 설정된 경우 적용
        if (appName != null && !appName.equals("application")) {
            builder.applicationName(appName);
        }

        LoggerInstance instance = builder.build();

        logger.info("UniversalLogger (Logback) initialized successfully");
        return instance;
    }

    /**
     * DataSource 빈을 SqlCaptureProxy로 자동 래핑
     *
     * @param bean 처리할 빈
     * @param beanName 빈 이름
     * @return 래핑된 빈 또는 원본 빈
     * @throws BeansException 빈 처리 중 오류 발생 시
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
