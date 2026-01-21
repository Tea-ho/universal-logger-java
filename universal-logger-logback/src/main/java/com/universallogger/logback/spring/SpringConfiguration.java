package com.universallogger.logback.spring;

import com.universallogger.logback.LoggerInstance;
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

import static com.universallogger.constants.LoggerConstants.*;

/**
 * Logback용 Spring Framework 설정 클래스
 *
 * Spring Framework(비-Boot) 프로젝트에서 @EnableUniversalLogger 어노테이션에 의해 임포트됩니다.
 * @ConfigurationProperties 대신 SpringProperties(순수 POJO)를 사용합니다.
 *
 * <p>주요 기능:</p>
 * <ul>
 *   <li>LoggerInstance 빈 생성</li>
 *   <li>프로파일 기반 설정 지원</li>
 *   <li>SQL 로깅을 위한 DataSource 자동 래핑</li>
 * </ul>
 *
 * <p>사용 예시:</p>
 * <pre>{@code
 * @Configuration
 * @EnableUniversalLogger
 * public class AppConfig {
 *     // 사용자 설정
 * }
 * }</pre>
 *
 * @see EnableUniversalLogger
 * @see SpringProperties
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

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
        this.environment = applicationContext.getEnvironment();
    }

    /**
     * SpringProperties 빈 생성
     *
     * @return SpringProperties 인스턴스
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
         * 설정 우선순위:
         * 1. 시스템 프로퍼티 (최우선)
         * 2. @EnableUniversalLogger 어노테이션
         * 3. spring.application.name (Spring 환경)
         * 4. YAML 설정 파일 (global.appName)
         * 5. 기본값
         */

        // 프로퍼티가 아직 초기화되지 않은 경우 초기화
        if (this.properties == null) {
            this.properties = springProperties();
        }

        String defaultConfigPath = ConfigPath.DEFAULT;

        // @EnableUniversalLogger 어노테이션 속성 읽기
        Map<String, Object> attributes = importMetadata.getAnnotationAttributes(
            EnableUniversalLogger.class.getName());

        if (attributes != null) {
            // applicationName 처리
            String annotationAppName = (String) attributes.get("applicationName");
            if (annotationAppName != null && !annotationAppName.trim().isEmpty()) {
                properties.setApplicationName(annotationAppName.trim());
                logger.debug("Using applicationName from @EnableUniversalLogger: {}", annotationAppName);
            }

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

        // applicationName이 설정되지 않은 경우 spring.application.name 사용
        if (properties.getApplicationName() == null || properties.getApplicationName().isEmpty()) {
            if (applicationName != null && !applicationName.equals("application")) {
                properties.setApplicationName(applicationName);
                logger.debug("Using applicationName from spring.application.name: {}", applicationName);
            }
        }

        // 시스템 프로퍼티 오버라이드 (최우선 순위)
        String systemAppName = System.getProperty(PropertyKeys.APPLICATION_NAME);
        if (systemAppName != null && !systemAppName.trim().isEmpty()) {
            properties.setApplicationName(systemAppName.trim());
            logger.info("Application name overridden by system property: {}", systemAppName);
        }

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

        logger.info("Final applicationName: {}", properties.getApplicationName());
        logger.info("Final configPath: {}", properties.getConfigPath());
        logger.info("Final enableSqlCapture: {}", properties.getSqlCapture().isEnabled());
    }

    /**
     * LoggerInstance 빈 생성
     *
     * @return LoggerInstance 인스턴스
     */
    @Bean
    @ConditionalOnMissingBean(LoggerInstance.class)
    public LoggerInstance loggerInstance() {
        if (this.properties == null) {
            this.properties = springProperties();
        }

        String appName = properties.getApplicationName();
        logger.info("Initializing UniversalLogger (Logback) for application: {}",
            appName != null ? appName : applicationName);
        logger.info("Config path: {}", properties.getConfigPath());

        String activeProfile = properties.getActiveProfile();
        if (activeProfile != null) {
            logger.info("Active profile: {}", activeProfile);
        }

        LoggerInstance.Builder builder = LoggerInstance.builder()
                .configPath(properties.getConfigPath())
                .profile(activeProfile);

        // applicationName이 설정된 경우 적용
        if (appName != null && !appName.isEmpty()) {
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
