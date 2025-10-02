package com.universallogger.spring;

import com.universallogger.UniversalLogger;
import com.universallogger.sql.SqlCaptureProxy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;
import java.io.File;

/**
 * Spring Boot Auto Configuration for UniversalLogger
 * Automatically wraps DataSource for SQL logging
 */
@Configuration
@ConditionalOnClass(DataSource.class)
@EnableConfigurationProperties(UniversalLoggerProperties.class)
public class UniversalLoggerAutoConfiguration {

    @Value("${spring.application.name:application}")
    private String applicationName;

    private final UniversalLoggerProperties properties;

    public UniversalLoggerAutoConfiguration(UniversalLoggerProperties properties) {
        this.properties = properties;
    }

    @PostConstruct
    public void initializeUniversalLogger() {
        try {
            // Auto-detect project name
            String projectName = applicationName.replaceAll("[^a-zA-Z0-9_-]", "_");

            // Set log path: C:/log/{projectName}/
            String logPath = "C:/log/" + projectName + "/";

            // Create log directory
            File logDir = new File(logPath);
            if (!logDir.exists()) {
                logDir.mkdirs();
            }

            // Initialize UniversalLogger with auto-generated config
            if (properties.getConfigFile() != null && !properties.getConfigFile().isEmpty()) {
                UniversalLogger.init(properties.getConfigFile());
            }
            // Note: If no config file specified, UniversalLogger will be initialized
            // by the user's configuration class (UniversalLoggerConfig)

            UniversalLogger.info("[UniversalLogger] Initialized for project: " + projectName);
            UniversalLogger.info("[UniversalLogger] Log directory: " + logPath);

        } catch (Exception e) {
            System.err.println("[UniversalLogger] Failed to initialize: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // DataSource wrapping is handled by BeanPostProcessor instead
    // to avoid Bean definition conflicts
}
