package io.universallogger.sql;

/**
 * SQL 포맷팅 인터페이스
 *
 * SQL 문자열을 읽기 쉽게 포맷팅하는 인터페이스입니다.
 * 다양한 포맷팅 스타일(MyBatis, Hibernate, 커스텀 등)을 지원할 수 있도록 추상화합니다.
 *
 * <p>구현체 예시:</p>
 * <ul>
 *   <li>{@link MyBatisSqlFormatter} - MyBatis 스타일 포맷팅</li>
 *   <li>CompactSqlFormatter - 한 줄 압축 포맷팅 (향후 확장)</li>
 *   <li>PrettySqlFormatter - 들여쓰기 포함 정렬 포맷팅 (향후 확장)</li>
 * </ul>
 *
 * @see MyBatisSqlFormatter
 * @see Slf4jSqlLogger
 */
public interface SqlFormatter {

    /**
     * SQL을 읽기 쉽게 포맷팅
     *
     * @param sql 원본 SQL
     * @return 포맷팅된 SQL
     */
    String format(String sql);
}
