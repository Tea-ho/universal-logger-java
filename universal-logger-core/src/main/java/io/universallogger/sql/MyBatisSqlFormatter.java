package io.universallogger.sql;

import java.util.regex.Pattern;

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
 * @see SqlFormatter
 * @see Slf4jSqlLogger
 */
public class MyBatisSqlFormatter implements SqlFormatter {

    // 컴파일된 Pattern 캐싱 (성능 최적화)
    private static final Pattern P_SELECT = Pattern.compile("(?i)\\s+(SELECT)\\s+");
    private static final Pattern P_FROM = Pattern.compile("(?i)\\s+(FROM)\\s+");
    private static final Pattern P_WHERE = Pattern.compile("(?i)\\s+(WHERE)\\s+");
    private static final Pattern P_AND = Pattern.compile("(?i)\\s+(AND)\\s+");
    private static final Pattern P_OR = Pattern.compile("(?i)\\s+(OR)\\s+");
    private static final Pattern P_ORDER_BY = Pattern.compile("(?i)\\s+(ORDER BY)\\s+");
    private static final Pattern P_GROUP_BY = Pattern.compile("(?i)\\s+(GROUP BY)\\s+");
    private static final Pattern P_HAVING = Pattern.compile("(?i)\\s+(HAVING)\\s+");
    private static final Pattern P_LIMIT = Pattern.compile("(?i)\\s+(LIMIT)\\s+");
    private static final Pattern P_OFFSET = Pattern.compile("(?i)\\s+(OFFSET)\\s+");
    private static final Pattern P_JOIN = Pattern.compile("(?i)\\s+(LEFT JOIN|RIGHT JOIN|INNER JOIN|OUTER JOIN|CROSS JOIN|JOIN)\\s+");
    private static final Pattern P_ON = Pattern.compile("(?i)\\s+(ON)\\s+");
    private static final Pattern P_INSERT_INTO = Pattern.compile("(?i)\\s+(INSERT INTO)\\s+");
    private static final Pattern P_VALUES = Pattern.compile("(?i)\\s+(VALUES)\\s+");
    private static final Pattern P_UPDATE = Pattern.compile("(?i)\\s+(UPDATE)\\s+");
    private static final Pattern P_SET = Pattern.compile("(?i)\\s+(SET)\\s+");
    private static final Pattern P_DELETE_FROM = Pattern.compile("(?i)\\s+(DELETE FROM)\\s+");
    private static final Pattern P_DELETE = Pattern.compile("(?i)\\s+(DELETE)\\s+");

    @Override
    public String format(String sql) {
        if (sql == null || sql.trim().isEmpty()) {
            return sql;
        }

        // 이미 포맷팅된 SQL인지 확인 (줄바꿈이 많으면 이미 포맷팅됨)
        if (sql.split("\n").length > 3) {
            return sql;  // 이미 포맷팅되어 있으면 그대로 반환
        }

        String formatted = sql;

        // SELECT 구문
        formatted = P_SELECT.matcher(formatted).replaceAll("\n$1 ");
        formatted = P_FROM.matcher(formatted).replaceAll("\n       $1 ");
        formatted = P_WHERE.matcher(formatted).replaceAll("\n       $1 ");
        formatted = P_AND.matcher(formatted).replaceAll("\n       $1 ");
        formatted = P_OR.matcher(formatted).replaceAll("\n       $1 ");
        formatted = P_ORDER_BY.matcher(formatted).replaceAll("\n       $1 ");
        formatted = P_GROUP_BY.matcher(formatted).replaceAll("\n       $1 ");
        formatted = P_HAVING.matcher(formatted).replaceAll("\n       $1 ");
        formatted = P_LIMIT.matcher(formatted).replaceAll("\n       $1 ");
        formatted = P_OFFSET.matcher(formatted).replaceAll("\n       $1 ");
        // JOIN 구문
        formatted = P_JOIN.matcher(formatted).replaceAll("\n       $1 ");
        formatted = P_ON.matcher(formatted).replaceAll(" $1 ");
        // INSERT 구문
        formatted = P_INSERT_INTO.matcher(formatted).replaceAll("\n$1 ");
        formatted = P_VALUES.matcher(formatted).replaceAll("\n       $1 ");
        // UPDATE 구문
        formatted = P_UPDATE.matcher(formatted).replaceAll("\n$1 ");
        formatted = P_SET.matcher(formatted).replaceAll("\n       $1 ");
        // DELETE 구문
        formatted = P_DELETE_FROM.matcher(formatted).replaceAll("\n$1 ");
        formatted = P_DELETE.matcher(formatted).replaceAll("\n$1 ");

        return formatted.trim();
    }
}
