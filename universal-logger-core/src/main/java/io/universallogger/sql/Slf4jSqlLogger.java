package io.universallogger.sql;

import io.universallogger.config.LoggerConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * SLF4J 기반 SQL 로거 구현체
 *
 * {@link SqlLogger} 인터페이스의 SLF4J 구현체입니다.
 * SQL 실행 정보를 SLF4J를 통해 로깅합니다.
 *
 * <p>로깅 형식:</p>
 * <ul>
 *   <li>[SQL] - SQL 쿼리 (파라미터 치환 옵션에 따라 ? 또는 실제 값으로 출력)</li>
 *   <li>[SQL-ROWS] - 결과 행 수 또는 영향받은 행 수</li>
 *   <li>[SQL-TIME] - 실행 시간 (ms)</li>
 *   <li>[SQL-SLOW] - 1초 이상 걸린 느린 쿼리 경고 (WARN 레벨)</li>
 * </ul>
 *
 * <p>사용 예시:</p>
 * <pre>{@code
 * SqlLogger sqlLogger = new Slf4jSqlLogger(loggerConfig);
 * sqlLogger.logSql("SELECT * FROM users", "caller");
 * }</pre>
 *
 * @see SqlLogger
 * @see SqlFormatter
 * @see ParameterSubstitutor
 */
public class Slf4jSqlLogger implements SqlLogger {

    private static final Logger logger = LoggerFactory.getLogger("UniversalSqlLogger");

    private final LoggerConfig config;
    private final SqlFormatter sqlFormatter;
    private final ParameterSubstitutor parameterSubstitutor;

    /**
     * LoggerConfig를 받는 생성자
     *
     * @param config 로거 설정
     */
    public Slf4jSqlLogger(LoggerConfig config) {
        this.config = config;
        this.sqlFormatter = new MyBatisSqlFormatter();
        this.parameterSubstitutor = new DefaultParameterSubstitutor();
    }

    /**
     * 기본 설정을 사용하는 생성자
     */
    public Slf4jSqlLogger() {
        this(null);
    }

    @Override
    public void logSql(String sql, String callerInfo) {
        logger.info("[SQL] " + sql);
    }

    @Override
    public void logExecutionTime(String sql, long timeMs, String callerInfo) {
        if (timeMs > 1000) {
            logger.warn("[SQL-SLOW] Execution time: " + timeMs + "ms - " + sql);
        } else {
            logger.info("[SQL-TIME] " + timeMs + "ms");
        }
    }

    @Override
    public void logRowCount(int rowCount, String callerInfo) {
        logger.info("[SQL-ROWS] Total: " + rowCount);
    }

    @Override
    public void logUpdateCount(int updateCount, String callerInfo) {
        logger.info("[SQL-ROWS] Updates: " + updateCount);
    }

    @Override
    public void logAllResults(String sql, Map<Integer, Object> parameters,
                              int rowCount, int updateCount, long executionTime,
                              boolean useParameterSubstitution, String callerInfo) {
        StringBuilder sb = new StringBuilder();

        // 1. SQL + 파라미터
        String formattedSql = sqlFormatter.format(sql);
        if (useParameterSubstitution && parameters != null && !parameters.isEmpty()) {
            // ON: 파라미터 치환된 SQL 출력
            String substitutedSql = parameterSubstitutor.substitute(formattedSql, parameters);
            sb.append("[SQL] ").append(substitutedSql).append("\n");
        } else {
            // OFF: 기존 형식 (? + Parameters)
            sb.append("[SQL] ").append(formattedSql);
            if (parameters != null && !parameters.isEmpty()) {
                sb.append(" | Parameters: ").append(parameters);
            }
            sb.append("\n");
        }

        // 2. 결과 행 수 또는 업데이트 수
        if (rowCount >= 0) {
            sb.append("[SQL-ROWS] Total: ").append(rowCount).append("\n");
        } else if (updateCount >= 0) {
            sb.append("[SQL-ROWS] Updates: ").append(updateCount).append("\n");
        } else {
            sb.append("[SQL-ROWS] Total: 0\n");
        }

        // 3. 실행 시간
        if (executionTime > 1000) {
            sb.append("[SQL-SLOW] Execution time: ").append(executionTime).append("ms");
        } else {
            sb.append("[SQL-TIME] ").append(executionTime).append("ms");
        }

        logger.info(sb.toString());
    }

    @Override
    public String getCallerInfo() {
        return "UniversalLogger";
    }
}
