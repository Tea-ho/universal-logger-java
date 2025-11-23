package com.universallogger.examples;

import com.universallogger.UniversalLogger;

/**
 * 최소 설정 예시 - 설정 파일 없이 사용
 */
public class MinimalExample {

    public static void main(String[] args) {
        System.out.println("=== Universal Logger - 최소 설정 예시 ===\n");

        // 설정 파일 없이 사용 - 자동으로 기본 설정 적용
        UniversalLogger.info("안녕하세요! Universal Logger입니다.");
        UniversalLogger.debug("디버그 메시지 (기본 레벨 INFO라 출력 안됨)");
        UniversalLogger.warn("경고 메시지");
        UniversalLogger.error("에러 메시지");

        System.out.println("\n[완료] 설정 파일 없이도 정상 동작!");
    }
}