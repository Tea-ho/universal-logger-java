package com.universallogger.appenders;

import ch.qos.logback.classic.spi.ILoggingEvent;

/**
 * 로그 Appender 인터페이스
 * 모든 커스텀 Appender는 이 인터페이스를 구현해야 함
 */
public interface LogAppender {

    /**
     * Appender 초기화
     */
    void start();

    /**
     * Appender 종료
     */
    void stop();

    /**
     * 로그 이벤트 처리
     *
     * @param event 로그 이벤트
     */
    void append(ILoggingEvent event);

    /**
     * Appender 이름 반환
     *
     * @return Appender 이름
     */
    String getName();

    /**
     * Appender가 시작되었는지 확인
     *
     * @return 시작 여부
     */
    boolean isStarted();
}