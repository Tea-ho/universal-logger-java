package com.universallogger.filters;

/**
 * 메시지 필터 인터페이스
 *
 * @deprecated 더 이상 사용되지 않습니다. Logback Filter를 사용하세요.
 * @see SensitiveDataLogbackFilter
 */
@Deprecated
public interface MessageFilter {

    /**
     * 메시지 필터링
     *
     * @param message 원본 메시지
     * @return 필터링된 메시지
     * @deprecated 더 이상 사용되지 않습니다.
     */
    @Deprecated
    String filter(String message);
}
