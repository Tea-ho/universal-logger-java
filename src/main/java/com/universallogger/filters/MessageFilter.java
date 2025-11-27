package com.universallogger.filters;

/**
 * 메시지 필터 인터페이스
 *
 * 의존성 역전 원칙(DIP) 준수:
 * - 구체 클래스가 아닌 인터페이스에 의존
 * - 다양한 필터링 전략 지원 가능
 * - 테스트 시 Mock 주입 가능
 *
 * 개방-폐쇄 원칙(OCP) 준수:
 * - 새로운 필터 전략 추가 시 인터페이스 구현만 하면 됨
 * - 기존 코드 변경 없이 확장 가능
 *
 * Strategy 패턴:
 * - 다양한 필터링 전략을 런타임에 교체 가능
 * - Composite 패턴과 조합하여 여러 필터 체인 구성 가능
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
