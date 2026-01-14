package com.universallogger.logback.appenders;

import com.universallogger.config.LoggerConfig;
import com.universallogger.logback.formatters.ConsoleFormatter;
import com.universallogger.logback.formatters.FileFormatter;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.filter.ThresholdFilter;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.ConsoleAppender;
import ch.qos.logback.core.rolling.RollingFileAppender;
import ch.qos.logback.core.rolling.SizeAndTimeBasedRollingPolicy;
import ch.qos.logback.core.util.FileSize;

/**
 * DefaultAppenderFactory - AppenderFactory의 기본 구현
 *
 * 사용법:
 * <pre>
 * AppenderFactory factory = new DefaultAppenderFactory();
 * Appender appender = factory.createConsoleAppender(context, config, "myApp");
 * </pre>
 */
public class DefaultAppenderFactory implements AppenderFactory {

    /**
     * 기본 생성자
     */
    public DefaultAppenderFactory() {
        // 기본 생성자
    }

    @Override
    public Appender<ILoggingEvent> createConsoleAppender(
            LoggerContext context,
            LoggerConfig.OutputConfig outputConfig,
            String appName) {

        ConsoleAppender<ILoggingEvent> appender = new ConsoleAppender<>();
        appender.setContext(context);
        appender.setName("CONSOLE");

        // 콘솔 포맷터 설정 (컬러 지원)
        ConsoleFormatter formatter = new ConsoleFormatter();
        formatter.setContext(context);
        formatter.setPattern(outputConfig.getFormat());
        formatter.setColors(outputConfig.getColors());
        formatter.start();

        appender.setEncoder(formatter);

        // 임계값 필터 추가
        addThresholdFilter(appender, outputConfig.getLevel());

        appender.start();
        return appender;
    }

    @Override
    public Appender<ILoggingEvent> createFileAppender(
            LoggerContext context,
            LoggerConfig.OutputConfig outputConfig,
            String appName) {

        RollingFileAppender<ILoggingEvent> appender = new RollingFileAppender<>();
        appender.setContext(context);
        appender.setName("FILE");

        // 파일 경로 설정
        String filePath = replacePlaceholders(outputConfig.getPath(), appName);
        appender.setFile(filePath);

        // 롤링 정책 설정
        SizeAndTimeBasedRollingPolicy<ILoggingEvent> rollingPolicy =
            new SizeAndTimeBasedRollingPolicy<>();
        rollingPolicy.setContext(context);
        rollingPolicy.setParent(appender);
        // compress 설정에 따라 파일 패턴 결정
        String filePattern = buildFileNamePattern(filePath, outputConfig.getRotation().isCompress());
        rollingPolicy.setFileNamePattern(filePattern);
        rollingPolicy.setMaxFileSize(FileSize.valueOf(outputConfig.getRotation().getMaxSize()));
        rollingPolicy.setMaxHistory(outputConfig.getRotation().getMaxFiles());
        rollingPolicy.start();

        appender.setRollingPolicy(rollingPolicy);

        // 파일 포맷터 설정
        FileFormatter formatter = new FileFormatter();
        formatter.setContext(context);
        formatter.setPattern(outputConfig.getFormat());
        formatter.start();

        appender.setEncoder(formatter);

        // 임계값 필터 추가
        addThresholdFilter(appender, outputConfig.getLevel());

        appender.start();
        return appender;
    }

    @Override
    public Appender<ILoggingEvent> createErrorFileAppender(
            LoggerContext context,
            LoggerConfig.OutputConfig outputConfig,
            String appName) {

        RollingFileAppender<ILoggingEvent> appender = new RollingFileAppender<>();
        appender.setContext(context);
        appender.setName("ERRORFILE");

        // 파일 경로 설정
        String filePath = replacePlaceholders(outputConfig.getPath(), appName);
        appender.setFile(filePath);

        // 롤링 정책 설정
        SizeAndTimeBasedRollingPolicy<ILoggingEvent> rollingPolicy =
            new SizeAndTimeBasedRollingPolicy<>();
        rollingPolicy.setContext(context);
        rollingPolicy.setParent(appender);
        // compress 설정에 따라 파일 패턴 결정
        String filePattern = buildFileNamePattern(filePath, outputConfig.getRotation().isCompress());
        rollingPolicy.setFileNamePattern(filePattern);
        rollingPolicy.setMaxFileSize(FileSize.valueOf(outputConfig.getRotation().getMaxSize()));
        rollingPolicy.setMaxHistory(outputConfig.getRotation().getMaxFiles());
        rollingPolicy.start();

        appender.setRollingPolicy(rollingPolicy);

        // 파일 포맷터 설정
        FileFormatter formatter = new FileFormatter();
        formatter.setContext(context);
        formatter.setPattern(outputConfig.getFormat());
        formatter.start();

        appender.setEncoder(formatter);

        // 임계값 필터 추가 (에러 레벨 이상만)
        addThresholdFilter(appender, outputConfig.getLevel());

        appender.start();
        return appender;
    }

    @Override
    public Appender<ILoggingEvent> createWebhookAppender(
            LoggerContext context,
            LoggerConfig.OutputConfig outputConfig) {

        WebhookAppender appender = new WebhookAppender();
        appender.setContext(context);
        appender.setName("WEBHOOK");

        if (outputConfig.getWebhook() != null) {
            appender.setUrl(outputConfig.getWebhook().getUrl());
            appender.setFormat(outputConfig.getWebhook().getFormat());
        }

        // 임계값 필터 추가 (레벨 필터링)
        addThresholdFilter(appender, outputConfig.getLevel());

        appender.start();
        return appender;
    }

    @Override
    public Appender<ILoggingEvent> createEmailAppender(
            LoggerContext context,
            LoggerConfig.OutputConfig outputConfig) {

        EmailAppender appender = new EmailAppender();
        appender.setContext(context);
        appender.setName("EMAIL");

        if (outputConfig.getEmail() != null) {
            appender.setEmailConfig(outputConfig.getEmail());
        }

        // 임계값 필터 추가
        addThresholdFilter(appender, outputConfig.getLevel());

        appender.start();
        return appender;
    }

    // ========== Helper Methods ==========

    /**
     * 임계값 필터 추가
     */
    private void addThresholdFilter(Appender<ILoggingEvent> appender, String level) {
        if (level != null && !level.isEmpty()) {
            ThresholdFilter thresholdFilter = new ThresholdFilter();
            thresholdFilter.setLevel(level);
            thresholdFilter.start();
            appender.addFilter(thresholdFilter);
        }
    }

    /**
     * 플레이스홀더 치환
     */
    private String replacePlaceholders(String path, String appName) {
        if (path == null) {
            return path;
        }
        if (appName != null) {
            return path.replace("{appName}", appName);
        }
        return path;
    }

    /**
     * 파일명 패턴 생성
     *
     * compress가 true이면 .gz 확장자 추가
     * compress가 false이면 압축 없이 일반 파일로 저장
     *
     * @param filePath 기본 파일 경로
     * @param compress 압축 여부
     * @return 롤링 파일 패턴
     */
    private String buildFileNamePattern(String filePath, boolean compress) {
        if (compress) {
            return filePath + ".%d{yyyy-MM-dd}.%i.gz";
        } else {
            return filePath + ".%d{yyyy-MM-dd}.%i";
        }
    }
}
