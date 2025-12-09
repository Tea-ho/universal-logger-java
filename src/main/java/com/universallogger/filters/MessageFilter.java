package com.universallogger.filters;

/**
 * 메시지 필터 인터페이스
 */
public interface MessageFilter {

    /**
     * 메시지 필터링
     *
     * @param message 원본 메시지
     * @return 필터링된 메시지
     */
    String filter(String message);
}
