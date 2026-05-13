package io.universallogger.sql;

import java.util.Map;

/**
 * SQL 파라미터 치환 인터페이스
 *
 * PreparedStatement의 ? 플레이스홀더를 실제 파라미터 값으로 치환하는 인터페이스입니다.
 * 다양한 치환 전략(기본, 민감정보 마스킹 등)을 지원할 수 있도록 추상화합니다.
 *
 * <p>구현체 예시:</p>
 * <ul>
 *   <li>{@link DefaultParameterSubstitutor} - 기본 파라미터 치환</li>
 *   <li>MaskingParameterSubstitutor - 민감정보 마스킹 (향후 확장)</li>
 * </ul>
 *
 * @see DefaultParameterSubstitutor
 * @see Slf4jSqlLogger
 */
public interface ParameterSubstitutor {

    /**
     * 파라미터 값을 SQL 문자열로 포맷팅
     *
     * 타입에 따라 적절한 형식으로 변환합니다.
     * (예: String → 'value', NULL → NULL, Number → 그대로)
     *
     * @param value 파라미터 값
     * @return SQL에 삽입할 수 있는 문자열 형식
     */
    String formatParameter(Object value);

    /**
     * SQL의 ? 플레이스홀더를 실제 파라미터 값으로 치환
     *
     * @param sql 원본 SQL (? 포함)
     * @param params 파라미터 맵 (1-based index)
     * @return 파라미터가 치환된 SQL
     */
    String substitute(String sql, Map<Integer, Object> params);
}
