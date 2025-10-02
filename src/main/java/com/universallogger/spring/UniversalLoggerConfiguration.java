package com.universallogger.spring;

import com.universallogger.UniversalLogger;
import com.universallogger.sql.SqlCaptureProxy;
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
 *
 * Usage in Spring XML:
 * <context:component-scan base-package="com.universallogger.spring"/>
 *
 * Usage in Spring JavaConfig:
 * @Import(UniversalLoggerConfiguration.class)
 */
@Configuration
public class UniversalLoggerConfiguration implements BeanPostProcessor {

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
                System.out.println("[UniversalLogger] Created log directory: " + logPath);
            }

            // 설정 파일 로드
            ClassPathResource resource = new ClassPathResource(configFile);
            if (resource.exists()) {
                File config = resource.getFile();
                UniversalLogger.init(config.getAbsolutePath());
                System.out.println("[UniversalLogger] Initialized with config: " + config.getAbsolutePath());

                UniversalLogger.info("[UniversalLogger] Active for project: " + projectName);
                UniversalLogger.info("[UniversalLogger] Log directory: " + logPath);
            } else {
                System.out.println("[UniversalLogger] Config file not found: " + configFile);
                System.out.println("[UniversalLogger] Using default configuration");
            }
        } catch (Exception e) {
            System.err.println("[UniversalLogger] Initialization failed: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        if (sqlLoggingEnabled && bean instanceof DataSource) {
            if (!dataSourceWrapped) {
                if (bean instanceof SqlCaptureProxy) {
                    return bean;
                }

                System.out.println("[UniversalLogger] Wrapping DataSource: " + beanName);
                UniversalLogger.info("[UniversalLogger] SQL logging enabled");

                dataSourceWrapped = true;
                return new SqlCaptureProxy((DataSource) bean);
            }
        }
        return bean;
    }
}
