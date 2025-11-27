package com.universallogger.spring;

import com.universallogger.UniversalLogger;
import com.universallogger.sql.SqlCaptureProxy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;
import java.io.File;

/**
 * Universal Logger Configuration for Spring (non-Boot) projects
 * <p>
 * Usage in Spring XML:
 * <pre>
 * {@code <context:component-scan base-package="com.universallogger.spring"/>}
 * </pre>
 * <p>
 * Usage in Spring JavaConfig:
 * <pre>
 * {@code @Import(UniversalLoggerConfiguration.class)}
 * </pre>
 */
@Configuration
public class UniversalLoggerConfiguration implements BeanPostProcessor {

    private static final Logger logger = LoggerFactory.getLogger(UniversalLoggerConfiguration.class);

    @Value("${spring.application.name:application}")
    private String applicationName;

    @Value("${universallogger.config.file:config/universal-logger.yml}")
    private String configFile;

    @Value("${universallogger.sql.enabled:true}")
    private boolean sqlLoggingEnabled;

    private boolean dataSourceWrapped = false;

    @PostConstruct
    public void initUniversalLogger() {
        try {
            // 프로젝트명 추출
            String projectName = applicationName.replaceAll("[^a-zA-Z0-9_-]", "_");
            String logPath = "C:/log/" + projectName + "/";

            // 로그 디렉토리 생성
            File logDir = new File(logPath);
            if (!logDir.exists()) {
                logDir.mkdirs();
                logger.info("Created log directory: {}", logPath);
            }

            // 설정 파일 로드
            ClassPathResource resource = new ClassPathResource(configFile);
            if (resource.exists()) {
                File config = resource.getFile();
                UniversalLogger.init(config.getAbsolutePath());
                logger.info("Initialized with config: {}", config.getAbsolutePath());

                UniversalLogger.info("[UniversalLogger] Active for project: " + projectName);
                UniversalLogger.info("[UniversalLogger] Log directory: " + logPath);
            } else {
                logger.info("Config file not found: {}", configFile);
                logger.info("Using default configuration");
            }
        } catch (Exception e) {
            logger.error("Initialization failed", e);
        }
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        if (sqlLoggingEnabled && bean instanceof DataSource) {
            if (!dataSourceWrapped) {
                if (bean instanceof SqlCaptureProxy) {
                    return bean;
                }

                logger.info("Wrapping DataSource: {}", beanName);
                UniversalLogger.info("[UniversalLogger] SQL logging enabled");

                dataSourceWrapped = true;
                return new SqlCaptureProxy((DataSource) bean);
            }
        }
        return bean;
    }
}
