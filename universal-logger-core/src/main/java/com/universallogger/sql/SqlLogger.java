package com.universallogger.sql;

import java.util.Map;

/**
 * SQL 로깅 인터페이스
 *
 * SQL 실행 정보를 로깅하는 로거의 공통 인터페이스입니다.
 * 다양한 로깅 구현체(SLF4J, 파일, JSON 등)를 지원할 수 있도록 추상화합니다.
 *
 * <p>로깅 형식 (구현체에 따라 다를 수 있음):</p>
 * <ul>
 *   <li>[SQL] - SQL 쿼리</li>
 *   <li>[SQL-ROWS] - 결과 행 수 또는 영향받은 행 수</li>
 *   <li>[SQL-TIME] - 실행 시간 (ms)</li>
 *   <li>[SQL-SLOW] - 느린 쿼리 경고</li>
 * </ul>
 *
 * @see Slf4jSqlLogger
 * @see SqlCaptureStatement
 * @see SqlCapturePreparedStatement
 */
public interface SqlLogger {

    /**
     * 단순 SQL 로깅 (Statement용)
     *
     * @param sql SQL 쿼리
     * @param callerInfo 호출자 정보
     */
    void logSql(String sql, String callerInfo);

    /**
     * 실행 시간 로깅
     *
     * @param sql SQL 쿼리
     * @param timeMs 실행 시간 (밀리초)
     * @param callerInfo 호출자 정보
     */
    void logExecutionTime(String sql, long timeMs, String callerInfo);

    /**
     * 결과 행 수 로깅
     *
     * @param rowCount 결과 행 수
     * @param callerInfo 호출자 정보
     */
    void logRowCount(int rowCount, String callerInfo);

    /**
     * 업데이트 수 로깅
     *
     * @param updateCount 영향받은 행 수
     * @param callerInfo 호출자 정보
     */
    void logUpdateCount(int updateCount, String callerInfo);

    /**
     * SQL 실행 결과를 한 번에 로깅 (PreparedStatement용)
     *
     * 멀티스레드 환경에서 로그가 섞이지 않도록 한 번에 출력합니다.
     *
     * @param sql SQL 쿼리
     * @param parameters 바인딩 파라미터 맵
     * @param rowCount 결과 행 수 (-1이면 미사용)
     * @param updateCount 영향받은 행 수 (-1이면 미사용)
     * @param executionTime 실행 시간 (밀리초)
     * @param useParameterSubstitution 파라미터 치환 사용 여부
     * @param callerInfo 호출자 정보
     */
    void logAllResults(String sql, Map<Integer, Object> parameters,
                       int rowCount, int updateCount, long executionTime,
                       boolean useParameterSubstitution, String callerInfo);

    /**
     * 호출자 정보 반환
     *
     * @return 호출자 정보 문자열
     */
    String getCallerInfo();
}
