package io.universallogger.logback.appenders;

import io.universallogger.config.LoggerConfig;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.core.Appender;
import ch.qos.logback.classic.spi.ILoggingEvent;

/**
 * Logback 앱펜더 생성을 위한 팩토리 인터페이스
 *
 * 다양한 출력 대상(콘솔, 파일, 웹훅, 이메일)을 위한 Logback 앱펜더를 생성합니다.
 * 팩토리 패턴을 사용하여 앱펜더 생성 로직을 캡슐화하고 테스트 용이성을 높입니다.
 *
 * <p>지원하는 앱펜더 유형:</p>
 * <ul>
 *   <li>콘솔 앱펜더 - 컬러 지원 콘솔 출력</li>
 *   <li>파일 앱펜더 - 롤링 파일 출력</li>
 *   <li>에러 파일 앱펜더 - 에러 전용 롤링 파일 출력</li>
 *   <li>웹훅 앱펜더 - Slack/Discord 등 웹훅 전송</li>
 *   <li>이메일 앱펜더 - SMTP 이메일 전송</li>
 * </ul>
 *
 * @see DefaultAppenderFactory
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
}
