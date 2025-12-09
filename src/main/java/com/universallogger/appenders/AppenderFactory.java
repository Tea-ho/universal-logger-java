package com.universallogger.appenders;

import com.universallogger.config.LoggerConfig;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.core.Appender;
import ch.qos.logback.classic.spi.ILoggingEvent;

/**
 * AppenderFactory - 앱펜더 생성을 위한 팩토리 인터페이스
 */
public interface AppenderFactory {

    /**
     * 콘솔 앱펜더 생성
     *
     * @param context LoggerContext
     * @param outputConfig 출력 설정
     * @param appName 애플리케이션 이름 (플레이스홀더 치환용)
     * @return 설정된 콘솔 앱펜더
     */
    Appender<ILoggingEvent> createConsoleAppender(
        LoggerContext context,
        LoggerConfig.OutputConfig outputConfig,
        String appName
    );

    /**
     * 파일 앱펜더 생성
     *
     * @param context LoggerContext
     * @param outputConfig 출력 설정
     * @param appName 애플리케이션 이름 (플레이스홀더 치환용)
     * @return 설정된 파일 앱펜더
     */
    Appender<ILoggingEvent> createFileAppender(
        LoggerContext context,
        LoggerConfig.OutputConfig outputConfig,
        String appName
    );

    /**
     * 에러 파일 앱펜더 생성
     *
     * @param context LoggerContext
     * @param outputConfig 출력 설정
     * @param appName 애플리케이션 이름 (플레이스홀더 치환용)
     * @return 설정된 에러 파일 앱펜더
     */
    Appender<ILoggingEvent> createErrorFileAppender(
        LoggerContext context,
        LoggerConfig.OutputConfig outputConfig,
        String appName
    );

    /**
     * 웹훅 앱펜더 생성
     *
     * @param context LoggerContext
     * @param outputConfig 출력 설정
     * @return 설정된 웹훅 앱펜더
     */
    Appender<ILoggingEvent> createWebhookAppender(
        LoggerContext context,
        LoggerConfig.OutputConfig outputConfig
    );

    /**
     * 이메일 앱펜더 생성
     *
     * @param context LoggerContext
     * @param outputConfig 출력 설정
     * @return 설정된 이메일 앱펜더
     */
    Appender<ILoggingEvent> createEmailAppender(
        LoggerContext context,
        LoggerConfig.OutputConfig outputConfig
    );

    /**
     * 데이터베이스 앱펜더 생성
     *
     * @param context LoggerContext
     * @param outputConfig 출력 설정
     * @return 설정된 데이터베이스 앱펜더
     */
    Appender<ILoggingEvent> createDatabaseAppender(
        LoggerContext context,
        LoggerConfig.OutputConfig outputConfig
    );
}
