package com.universallogger.sql;

import java.util.Map;

/**
 * SQL 파라미터 치환 유틸리티
 * PreparedStatement의 ? 플레이스홀더를 실제 파라미터 값으로 치환
 */
public class ParameterSubstitutor {

    /**
     * 파라미터 값을 SQL 문자열로 포맷팅
     * 타입에 따라 적절한 형식으로 변환
     */
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

    /**
     * SQL의 ? 플레이스홀더를 실제 파라미터 값으로 치환
     */
    public String substitute(String sql, Map<Integer, Object> params) {
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
