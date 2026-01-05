package com.universallogger.log4j2;

import com.universallogger.config.LoggerConfig;
import com.universallogger.loaders.UniversalConfigLoader;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.ConfigurationFactory;
import org.apache.logging.log4j.core.config.ConfigurationSource;
import org.apache.logging.log4j.core.config.Order;
import org.apache.logging.log4j.core.config.builder.api.AppenderComponentBuilder;
import org.apache.logging.log4j.core.config.builder.api.ConfigurationBuilder;
import org.apache.logging.log4j.core.config.builder.api.ConfigurationBuilderFactory;
import org.apache.logging.log4j.core.config.builder.impl.BuiltConfiguration;
import org.apache.logging.log4j.core.config.plugins.Plugin;

import java.util.Map;

/**
 * Log4j2 ConfigurationFactory - Log4j2 시작 시 자동으로 호출됨
 *
 * ServiceLoader를 통해 자동 발견:
 * META-INF/services/org.apache.logging.log4j.core.config.ConfigurationFactory
 */
@Plugin(name = "UniversalLoggerConfigurationFactory", category = ConfigurationFactory.CATEGORY)
@Order(50)
public class Log4j2ConfigurationFactory extends ConfigurationFactory {

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
            builder.add(builder.newRootLogger(rootLevel)
                .add(builder.newAppenderRef("Console"))
                .add(builder.newAppenderRef("File"))
                .add(builder.newAppenderRef("ErrorFile")));

            // 모듈별 로거 레벨 설정
            for (Map.Entry<String, String> entry : config.getLevels().entrySet()) {
                if (!"root".equals(entry.getKey())) {
                    Level level = Level.valueOf(entry.getValue());
                    builder.add(builder.newLogger(entry.getKey(), level)
                        .add(builder.newAppenderRef("Console"))
                        .add(builder.newAppenderRef("File"))
                        .add(builder.newAppenderRef("ErrorFile"))
                        .setAdditivity(false));
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
     */
    private LoggerConfig loadConfig() {
        try {
            return configLoader.loadDefaultConfig();
        } catch (Exception e) {
            System.err.println("Failed to load config, using minimal defaults");
            throw new IllegalStateException("Failed to load default configuration", e);
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
            String pattern = consoleConfig.getPattern().replace("${appName}", appName);

            AppenderComponentBuilder consoleAppender = builder.newAppender("Console", "CONSOLE")
                .addAttribute("target", "SYSTEM_OUT");
            consoleAppender.add(builder.newLayout("PatternLayout")
                .addAttribute("pattern", pattern));
            builder.add(consoleAppender);
        }

        // 파일 Appender
        if (isOutputEnabled(outputs, "file")) {
            LoggerConfig.OutputConfig fileConfig = outputs.get("file");
            String filePath = fileConfig.getFilePath().replace("${appName}", appName);
            String pattern = fileConfig.getPattern().replace("${appName}", appName);

            AppenderComponentBuilder fileAppender = builder.newAppender("File", "RollingFile")
                .addAttribute("fileName", filePath)
                .addAttribute("filePattern", filePath + ".%d{yyyy-MM-dd}.gz");
            fileAppender.add(builder.newLayout("PatternLayout")
                .addAttribute("pattern", pattern));
            fileAppender.addComponent(builder.newComponent("Policies")
                .addComponent(builder.newComponent("TimeBasedTriggeringPolicy")
                    .addAttribute("interval", "1")
                    .addAttribute("modulate", "true")));
            builder.add(fileAppender);
        }

        // 에러 파일 Appender
        if (isOutputEnabled(outputs, "errorFile")) {
            LoggerConfig.OutputConfig errorConfig = outputs.get("errorFile");
            String filePath = errorConfig.getFilePath().replace("${appName}", appName);
            String pattern = errorConfig.getPattern().replace("${appName}", appName);

            AppenderComponentBuilder errorAppender = builder.newAppender("ErrorFile", "RollingFile")
                .addAttribute("fileName", filePath)
                .addAttribute("filePattern", filePath + ".%d{yyyy-MM-dd}.gz");
            errorAppender.add(builder.newLayout("PatternLayout")
                .addAttribute("pattern", pattern));
            errorAppender.addComponent(builder.newComponent("Policies")
                .addComponent(builder.newComponent("TimeBasedTriggeringPolicy")
                    .addAttribute("interval", "1")
                    .addAttribute("modulate", "true")));
            errorAppender.addComponent(builder.newFilter("ThresholdFilter", "ACCEPT", "DENY")
                .addAttribute("level", "ERROR"));
            builder.add(errorAppender);
        }
    }

    /**
     * 출력 활성화 여부 확인
     */
    private boolean isOutputEnabled(Map<String, LoggerConfig.OutputConfig> outputs, String key) {
        return outputs.containsKey(key) && outputs.get(key).isEnabled();
    }
}
