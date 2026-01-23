package com.universallogger.log4j2;

import com.universallogger.config.LoggerConfig;
import com.universallogger.loaders.UniversalConfigLoader;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.ConfigurationSource;
import org.apache.logging.log4j.core.config.Order;
import org.apache.logging.log4j.core.config.builder.api.AppenderComponentBuilder;
import org.apache.logging.log4j.core.config.builder.api.ConfigurationBuilder;
import org.apache.logging.log4j.core.config.builder.api.ConfigurationBuilderFactory;
import org.apache.logging.log4j.core.config.builder.impl.BuiltConfiguration;
import org.apache.logging.log4j.core.config.plugins.Plugin;

import java.io.File;
import java.util.Map;

/**
 * Log4j2 ConfigurationFactory - Log4j2 시작 시 자동으로 호출됨
 *
 * ServiceLoader를 통해 자동 발견:
 * META-INF/services/org.apache.logging.log4j.core.config.ConfigurationFactory
 */
@Plugin(name = "UniversalLoggerConfigurationFactory", category = org.apache.logging.log4j.core.config.ConfigurationFactory.CATEGORY)
@Order(50)
public class ConfigurationFactory extends org.apache.logging.log4j.core.config.ConfigurationFactory {

    private static final com.universallogger.loaders.ConfigLoader configLoader = new UniversalConfigLoader();

    @Override
    protected String[] getSupportedTypes() {
        return new String[]{"*"};
    }

    @Override
    public Configuration getConfiguration(LoggerContext loggerContext, ConfigurationSource source) {
        return getConfiguration(loggerContext, source.toString(), null);
    }

    @Override
    public Configuration getConfiguration(LoggerContext loggerContext, String name, java.net.URI configLocation) {
        ConfigurationBuilder<BuiltConfiguration> builder = ConfigurationBuilderFactory.newConfigurationBuilder();

        try {
            // UniversalLogger 설정 로드
            LoggerConfig config = loadConfig();

            // 루트 로거 레벨 설정
            Level rootLevel = Level.valueOf(config.getLevels().getOrDefault("root", "INFO"));
            builder.setStatusLevel(rootLevel);
            builder.setConfigurationName("UniversalLogger");

            // Appender 설정
            setupAppenders(builder, config);

            // 루트 로거 설정
            org.apache.logging.log4j.core.config.builder.api.RootLoggerComponentBuilder rootLogger = builder.newRootLogger(rootLevel);

            // 활성화된 appender만 참조 추가
            if (isOutputEnabled(config.getOutputs(), "console")) {
                rootLogger.add(builder.newAppenderRef("Console"));
            }
            if (isOutputEnabled(config.getOutputs(), "file")) {
                rootLogger.add(builder.newAppenderRef("File"));
            }
            if (isOutputEnabled(config.getOutputs(), "errorFile")) {
                rootLogger.add(builder.newAppenderRef("ErrorFile"));
            }
            if (isOutputEnabled(config.getOutputs(), "webhook")) {
                rootLogger.add(builder.newAppenderRef("Webhook"));
            }
            if (isOutputEnabled(config.getOutputs(), "email")) {
                rootLogger.add(builder.newAppenderRef("Email"));
            }

            builder.add(rootLogger);

            // 모듈별 로거 레벨 설정 (appender는 루트에서 상속받음)
            for (Map.Entry<String, String> entry : config.getLevels().entrySet()) {
                if (!"root".equals(entry.getKey())) {
                    Level level = Level.valueOf(entry.getValue());
                    org.apache.logging.log4j.core.config.builder.api.LoggerComponentBuilder logger = builder.newLogger(entry.getKey(), level)
                        .addAttribute("additivity", "true");  // 루트 로거의 appender 사용
                    builder.add(logger);
                }
            }

            return builder.build();

        } catch (Exception e) {
            System.err.println("Failed to configure UniversalLogger: " + e.getMessage());
            e.printStackTrace();
            // 기본 설정 반환
            return builder.build();
        }
    }

    /**
     * 설정 파일 로드
     *
     * 로드 순서:
     * 1. 클래스패스에서 config/universal-logger.yml 찾기
     * 2. 없으면 JAR 내부 defaults.yml 사용
     */
    private LoggerConfig loadConfig() {
        try {
            // 사용자 설정 파일 로드 시도
            String configPath = "config/universal-logger.yml";
            return configLoader.load(configPath, true);
        } catch (Exception e) {
            System.err.println("User config not found, using defaults: " + e.getMessage());
            try {
                return configLoader.loadDefaultConfig();
            } catch (Exception ex) {
                System.err.println("Failed to load default config: " + ex.getMessage());
                throw new IllegalStateException("Failed to load configuration", ex);
            }
        }
    }

    /**
     * Appender 설정
     */
    private void setupAppenders(ConfigurationBuilder<BuiltConfiguration> builder, LoggerConfig config) {
        Map<String, LoggerConfig.OutputConfig> outputs = config.getOutputs();
        String appName = config.getGlobal().getAppName();

        // 콘솔 Appender
        if (isOutputEnabled(outputs, "console")) {
            LoggerConfig.OutputConfig consoleConfig = outputs.get("console");
            String pattern = consoleConfig.getFormat().replace("{appName}", appName);

            AppenderComponentBuilder consoleAppender = builder.newAppender("Console", "CONSOLE")
                .addAttribute("target", "SYSTEM_OUT");
            consoleAppender.add(builder.newLayout("PatternLayout")
                .addAttribute("pattern", pattern));
            builder.add(consoleAppender);
        }

        // 파일 Appender
        if (isOutputEnabled(outputs, "file")) {
            LoggerConfig.OutputConfig fileConfig = outputs.get("file");
            String filePath = fileConfig.getPath().replace("{appName}", appName);
            String pattern = fileConfig.getFormat().replace("{appName}", appName);

            // 부모 디렉토리 생성
            createParentDirectories(filePath);

            // 롤링 설정
            LoggerConfig.RotationConfig rotation = fileConfig.getRotation();
            String maxSize = rotation != null && rotation.getMaxSize() != null ? rotation.getMaxSize() : "50MB";
            int maxFiles = rotation != null && rotation.getMaxFiles() > 0 ? rotation.getMaxFiles() : 30;
            boolean compress = rotation != null && rotation.isCompress();
            String filePattern = compress
                ? filePath + ".%d{yyyy-MM-dd}.%i.gz"
                : filePath + ".%d{yyyy-MM-dd}.%i";

            AppenderComponentBuilder fileAppender = builder.newAppender("File", "RollingFile")
                .addAttribute("fileName", filePath)
                .addAttribute("filePattern", filePattern)
                .addAttribute("append", "true");
            fileAppender.add(builder.newLayout("PatternLayout")
                .addAttribute("pattern", pattern));
            fileAppender.addComponent(builder.newComponent("Policies")
                .addComponent(builder.newComponent("SizeBasedTriggeringPolicy")
                    .addAttribute("size", maxSize)));
            fileAppender.addComponent(builder.newComponent("DefaultRolloverStrategy")
                .addAttribute("max", String.valueOf(maxFiles)));
            builder.add(fileAppender);
        }

        // 에러 파일 Appender
        if (isOutputEnabled(outputs, "errorFile")) {
            LoggerConfig.OutputConfig errorConfig = outputs.get("errorFile");
            String filePath = errorConfig.getPath().replace("{appName}", appName);
            String pattern = errorConfig.getFormat().replace("{appName}", appName);

            // 부모 디렉토리 생성
            createParentDirectories(filePath);

            // 롤링 설정
            LoggerConfig.RotationConfig rotation = errorConfig.getRotation();
            String maxSize = rotation != null && rotation.getMaxSize() != null ? rotation.getMaxSize() : "50MB";
            int maxFiles = rotation != null && rotation.getMaxFiles() > 0 ? rotation.getMaxFiles() : 30;
            boolean compress = rotation != null && rotation.isCompress();
            String filePattern = compress
                ? filePath + ".%d{yyyy-MM-dd}.%i.gz"
                : filePath + ".%d{yyyy-MM-dd}.%i";

            AppenderComponentBuilder errorAppender = builder.newAppender("ErrorFile", "RollingFile")
                .addAttribute("fileName", filePath)
                .addAttribute("filePattern", filePattern)
                .addAttribute("append", "true");
            errorAppender.add(builder.newLayout("PatternLayout")
                .addAttribute("pattern", pattern));
            errorAppender.addComponent(builder.newComponent("Policies")
                .addComponent(builder.newComponent("SizeBasedTriggeringPolicy")
                    .addAttribute("size", maxSize)));
            errorAppender.addComponent(builder.newComponent("DefaultRolloverStrategy")
                .addAttribute("max", String.valueOf(maxFiles)));
            errorAppender.addComponent(builder.newFilter("ThresholdFilter", "ACCEPT", "DENY")
                .addAttribute("level", "ERROR"));
            builder.add(errorAppender);
        }

        // Webhook Appender
        if (isOutputEnabled(outputs, "webhook")) {
            LoggerConfig.OutputConfig webhookOutput = outputs.get("webhook");
            LoggerConfig.WebhookConfig webhookConfig = webhookOutput.getWebhook();

            if (webhookConfig != null && webhookConfig.getUrl() != null) {
                String messageFormat = webhookConfig.getFormat();
                if (messageFormat != null) {
                    messageFormat = messageFormat.replace("{appName}", appName);
                }

                AppenderComponentBuilder webhookAppender = builder.newAppender("Webhook", "UniversalWebhook")
                    .addAttribute("webhookUrl", webhookConfig.getUrl())
                    .addAttribute("messageFormat", messageFormat);

                // Add level filter if specified
                String level = webhookOutput.getLevel();
                if (level != null && !level.isEmpty()) {
                    webhookAppender.add(builder.newFilter("ThresholdFilter", "ACCEPT", "DENY")
                        .addAttribute("level", level));
                }

                builder.add(webhookAppender);
            }
        }

        // Email Appender
        if (isOutputEnabled(outputs, "email")) {
            LoggerConfig.OutputConfig emailOutput = outputs.get("email");
            LoggerConfig.EmailConfig emailConfig = emailOutput.getEmail();

            if (emailConfig != null && emailConfig.getSmtp() != null) {
                LoggerConfig.EmailConfig.SmtpConfig smtp = emailConfig.getSmtp();

                AppenderComponentBuilder emailAppender = builder.newAppender("Email", "UniversalEmail")
                    .addAttribute("smtpHost", smtp.getHost())
                    .addAttribute("smtpPort", smtp.getPort())
                    .addAttribute("username", smtp.getUsername())
                    .addAttribute("password", smtp.getPassword())
                    .addAttribute("from", emailConfig.getFrom())
                    .addAttribute("to", emailConfig.getTo() != null ? String.join(",", emailConfig.getTo()) : "")
                    .addAttribute("subject", emailConfig.getSubject());

                // Add level filter if specified
                String level = emailOutput.getLevel();
                if (level != null && !level.isEmpty()) {
                    emailAppender.add(builder.newFilter("ThresholdFilter", "ACCEPT", "DENY")
                        .addAttribute("level", level));
                }

                builder.add(emailAppender);
            }
        }
    }

    /**
     * 출력 활성화 여부 확인
     */
    private boolean isOutputEnabled(Map<String, LoggerConfig.OutputConfig> outputs, String key) {
        return outputs.containsKey(key) && outputs.get(key).isEnabled();
    }

    /**
     * 부모 디렉토리 생성
     */
    private void createParentDirectories(String filePath) {
        if (filePath != null) {
            File file = new File(filePath);
            File parentDir = file.getParentFile();
            if (parentDir != null && !parentDir.exists()) {
                parentDir.mkdirs();
            }
        }
    }
}
