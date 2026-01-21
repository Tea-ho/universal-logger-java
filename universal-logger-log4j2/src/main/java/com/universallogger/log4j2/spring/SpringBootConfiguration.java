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
 * Log4j2용 Spring Boot 자동 설정 클래스
 *
 * universal-logger-log4j2 의존성이 존재할 때 자동으로 활성화됩니다.
 * @ConfigurationProperties 기반의 SpringBootProperties를 사용합니다.
 *
 * <p>주요 기능:</p>
 * <ul>
 *   <li>LoggerConfig 빈 자동 생성</li>
 *   <li>프로파일 기반 설정 지원</li>
 *   <li>SQL 로깅을 위한 DataSource 자동 래핑</li>
 * </ul>
 *
 * @see SpringBootProperties
 * @see LoggerConfig
 */
@AutoConfiguration
@ConditionalOnClass({DataSource.class})
@ConditionalOnProperty(prefix = "universal-logger", name = "enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(SpringBootProperties.class)
@Order(Ordered.HIGHEST_PRECEDENCE)
public class SpringBootConfiguration implements BeanPostProcessor, ApplicationContextAware {

    private static final Logger logger = LoggerFactory.getLogger(SpringBootConfiguration.class);

    @Value("${spring.application.name:application}")
    private String applicationName;

    private final LoggerProperties properties;
    private final Set<String> wrappedDataSources = new HashSet<>();
    private ApplicationContext applicationContext;
    private LoggerConfig loggerConfig;

    public SpringBootConfiguration(SpringBootProperties properties) {
        this.properties = properties;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    /**
     * LoggerConfig 빈 생성
     *
     * @return LoggerConfig 인스턴스
     */
    @Bean
    @ConditionalOnMissingBean(LoggerConfig.class)
    public LoggerConfig loggerConfig() {
        // applicationName 결정: properties > spring.application.name
        String appName = properties.getApplicationName();
        if (appName == null || appName.isEmpty()) {
            appName = applicationName;
        }

        logger.info("Initializing UniversalLogger (Log4j2) for application: {}", appName);

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

            // applicationName이 설정된 경우 적용 (YAML 설정보다 우선)
            if (appName != null && !appName.equals("application")) {
                this.loggerConfig.getGlobal().setAppName(appName);
                logger.debug("Application name overridden: {}", appName);
            }

            logger.info("UniversalLogger (Log4j2) initialized successfully");
            return this.loggerConfig;
        } catch (Exception e) {
            logger.error("Failed to initialize UniversalLogger (Log4j2)", e);
            throw new IllegalStateException("Failed to load UniversalLogger configuration", e);
        }
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
