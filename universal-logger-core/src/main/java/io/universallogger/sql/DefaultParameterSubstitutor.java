package io.universallogger.sql;

import java.util.Map;

/**
 * 기본 SQL 파라미터 치환 구현체
 *
 * {@link ParameterSubstitutor} 인터페이스의 기본 구현체입니다.
 * PreparedStatement의 ? 플레이스홀더를 실제 파라미터 값으로 치환합니다.
 *
 * <p>지원하는 타입별 포맷팅:</p>
 * <ul>
 *   <li>null → NULL</li>
 *   <li>String → 'value' (내부 작은따옴표 이스케이프)</li>
 *   <li>Date/Time/Timestamp → 'yyyy-MM-dd HH:mm:ss'</li>
 *   <li>byte[] → [BLOB: n bytes]</li>
 *   <li>Number/Boolean → 그대로 출력</li>
 * </ul>
 *
 * @see ParameterSubstitutor
 * @see Slf4jSqlLogger
 */
public class DefaultParameterSubstitutor implements ParameterSubstitutor {

    @Override
    public String formatParameter(Object value) {
        if (value == null) {
            return "NULL";
        }

        // String - 작은따옴표로 감싸고 내부 작은따옴표는 이스케이프
        if (value instanceof String) {
            return "'" + ((String) value).replace("'", "''") + "'";
        }

        // Date/Time - 작은따옴표로 감싸기
        if (value instanceof java.sql.Date || value instanceof java.sql.Time ||
            value instanceof java.sql.Timestamp || value instanceof java.util.Date) {
            return "'" + value.toString() + "'";
        }

        // byte[] - BLOB 표시
        if (value instanceof byte[]) {
            return "[BLOB: " + ((byte[]) value).length + " bytes]";
        }

        // Number, Boolean - 그대로 출력
        return value.toString();
    }

    @Override
    public String substitute(String sql, Map<Integer, Object> params) {
        if (sql == null) {
            return null;
        }
        if (params == null || params.isEmpty()) {
            return sql;
        }

        StringBuilder result = new StringBuilder();
        int currentPos = 0;
        int paramIndex = 1;

        for (int i = 0; i < sql.length(); i++) {
            if (sql.charAt(i) == '?') {
                // ? 이전까지의 문자열 추가
                result.append(sql, currentPos, i);

                // 파라미터 값 치환
                Object paramValue = params.get(paramIndex);
                result.append(formatParameter(paramValue));

                paramIndex++;
                currentPos = i + 1;
            }
        }

        // 남은 문자열 추가
        if (currentPos < sql.length()) {
            result.append(sql, currentPos, sql.length());
        }

        return result.toString();
    }
}
