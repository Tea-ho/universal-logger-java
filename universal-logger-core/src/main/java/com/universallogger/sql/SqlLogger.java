package com.universallogger.sql;

import com.universallogger.config.LoggerConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * SQL 로깅 유틸리티
 * SQL 실행 정보를 포맷팅하여 로깅
 */
public class SqlLogger {

    private static final Logger logger = LoggerFactory.getLogger("SQL");
    private final LoggerConfig config;
    private final SqlFormatter sqlFormatter;
    private final ParameterSubstitutor parameterSubstitutor;

    /**
     * Constructor with LoggerConfig
     */
    public SqlLogger(LoggerConfig config) {
        this.config = config;
        this.sqlFormatter = new SqlFormatter();
        this.parameterSubstitutor = new ParameterSubstitutor();
    }

    /**
     * Constructor with default config
     */
    public SqlLogger() {
        this(null);
    }

    /**
     * 단순 SQL 로깅 (Statement용)
     */
    public void logSql(String sql, String callerInfo) {
        logger.info("[SQL] " + sql);
    }

    /**
     * 실행 시간 로깅
     */
    public void logExecutionTime(String sql, long timeMs, String callerInfo) {
        if (timeMs > 1000) {
            logger.warn("[SQL-SLOW] Execution time: " + timeMs + "ms - " + sql);
        } else {
            logger.info("[SQL-TIME] " + timeMs + "ms");
        }
    }

    /**
     * Row count 로깅
     */
    public void logRowCount(int rowCount, String callerInfo) {
        logger.info("[SQL-ROWS] Total: " + rowCount);
    }

    /**
     * Update count 로깅
     */
    public void logUpdateCount(int updateCount, String callerInfo) {
        logger.info("[SQL-ROWS] Updates: " + updateCount);
    }

    /**
     * SQL 실행 결과를 한 번에 로깅 (PreparedStatement용)
     * 멀티스레드 환경에서 로그가 섞이지 않도록
     */
    public void logAllResults(String sql, Map<Integer, Object> parameters,
                             int rowCount, int updateCount, long executionTime,
                             boolean useParameterSubstitution, String callerInfo) {
        StringBuilder sb = new StringBuilder();

        // 1. SQL + Parameters
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

        // 2. Row Count or Update Count
        if (rowCount >= 0) {
            sb.append("[SQL-ROWS] Total: ").append(rowCount).append("\n");
        } else if (updateCount >= 0) {
            sb.append("[SQL-ROWS] Updates: ").append(updateCount).append("\n");
        } else {
            sb.append("[SQL-ROWS] Total: 0\n");
        }

        // 3. Execution Time
        if (executionTime > 1000) {
            sb.append("[SQL-SLOW] Execution time: ").append(executionTime).append("ms");
        } else {
            sb.append("[SQL-TIME] ").append(executionTime).append("ms");
        }

        logger.info(sb.toString());
    }

    /**
     * SQL 로깅을 위한 logger 이름 반환
     * 스택 트레이스 분석 없이 고정된 이름 사용
     */
    public String getCallerInfo() {
        return "UniversalLogger";
    }
}
