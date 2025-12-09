package com.universallogger.sql;

/**
 * SQL 포맷팅 유틸리티
 * SQL 문자열을 읽기 쉽게 포맷팅하여 MyBatis 스타일과 동일하게 출력
 */
public class SqlFormatter {

    /**
     * SQL을 읽기 쉽게 포맷팅
     * - 주요 키워드 앞에 줄바꿈 추가
     * - MyBatis 스타일과 동일하게 출력
     */
    public String format(String sql) {
        if (sql == null || sql.trim().isEmpty()) {
            return sql;
        }

        // 이미 포맷팅된 SQL인지 확인 (줄바꿈이 많으면 이미 포맷팅됨)
        if (sql.split("\n").length > 3) {
            return sql;  // 이미 포맷팅되어 있으면 그대로 반환
        }

        String formatted = sql
            // SELECT 구문
            .replaceAll("(?i)\\s+(SELECT)\\s+", "\n$1 ")
            .replaceAll("(?i)\\s+(FROM)\\s+", "\n       $1 ")
            .replaceAll("(?i)\\s+(WHERE)\\s+", "\n       $1 ")
            .replaceAll("(?i)\\s+(AND)\\s+", "\n       $1 ")
            .replaceAll("(?i)\\s+(OR)\\s+", "\n       $1 ")
            .replaceAll("(?i)\\s+(ORDER BY)\\s+", "\n       $1 ")
            .replaceAll("(?i)\\s+(GROUP BY)\\s+", "\n       $1 ")
            .replaceAll("(?i)\\s+(HAVING)\\s+", "\n       $1 ")
            .replaceAll("(?i)\\s+(LIMIT)\\s+", "\n       $1 ")
            .replaceAll("(?i)\\s+(OFFSET)\\s+", "\n       $1 ")
            // JOIN 구문
            .replaceAll("(?i)\\s+(LEFT JOIN|RIGHT JOIN|INNER JOIN|OUTER JOIN|CROSS JOIN|JOIN)\\s+", "\n       $1 ")
            .replaceAll("(?i)\\s+(ON)\\s+", " $1 ")
            // INSERT 구문
            .replaceAll("(?i)\\s+(INSERT INTO)\\s+", "\n$1 ")
            .replaceAll("(?i)\\s+(VALUES)\\s+", "\n       $1 ")
            // UPDATE 구문
            .replaceAll("(?i)\\s+(UPDATE)\\s+", "\n$1 ")
            .replaceAll("(?i)\\s+(SET)\\s+", "\n       $1 ")
            // DELETE 구문
            .replaceAll("(?i)\\s+(DELETE FROM)\\s+", "\n$1 ")
            .replaceAll("(?i)\\s+(DELETE)\\s+", "\n$1 ")
            .trim();

        return formatted;
    }
}
