package com.universallogger.sql;

/**
 * MyBatis 스타일 SQL 포맷터 구현체
 *
 * {@link SqlFormatter} 인터페이스의 MyBatis 스타일 구현체입니다.
 * SQL 문자열을 MyBatis 로그 출력과 동일한 스타일로 포맷팅합니다.
 *
 * <p>포맷팅 규칙:</p>
 * <ul>
 *   <li>주요 키워드(SELECT, FROM, WHERE 등) 앞에 줄바꿈 추가</li>
 *   <li>AND, OR 등 조건절 키워드 앞에 줄바꿈 및 들여쓰기</li>
 *   <li>JOIN 구문 앞에 줄바꿈 및 들여쓰기</li>
 *   <li>INSERT, UPDATE, DELETE 구문 지원</li>
 * </ul>
 *
 * <p>사용 예시:</p>
 * <pre>{@code
 * SqlFormatter formatter = new MyBatisSqlFormatter();
 * String formatted = formatter.format("SELECT * FROM users WHERE id = ?");
 * // 결과:
 * // SELECT *
 * //        FROM users
 * //        WHERE id = ?
 * }</pre>
 *
 * @see SqlFormatter
 * @see Slf4jSqlLogger
 */
public class MyBatisSqlFormatter implements SqlFormatter {

    @Override
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
