package com.universallogger.sql;

import com.universallogger.LoggerInstance;
import com.universallogger.UniversalLogger;

import java.util.Map;

/**
 * SQL 로깅 유틸리티
 * SQL 실행 정보를 포맷팅하여 로깅
 */
public class SqlLogger {

    private final LoggerInstance loggerInstance;
    private final SqlFormatter sqlFormatter;
    private final ParameterSubstitutor parameterSubstitutor;

    /**
     * Constructor with instance-based LoggerInstance
     */
    public SqlLogger(LoggerInstance loggerInstance) {
        this.loggerInstance = loggerInstance;
        this.sqlFormatter = new SqlFormatter();
        this.parameterSubstitutor = new ParameterSubstitutor();
    }

    /**
     * Constructor for static API (backward compatibility)
     */
    public SqlLogger() {
        this(null);
    }

    /**
     * 단순 SQL 로깅 (Statement용)
     */
    public void logSql(String sql, String callerInfo) {
        if (loggerInstance != null) {
            loggerInstance.getLogger(callerInfo).info("[SQL] " + sql);
        } else {
            UniversalLogger.getLogger(callerInfo).info("[SQL] " + sql);
        }
    }

    /**
     * 실행 시간 로깅
     */
    public void logExecutionTime(String sql, long timeMs, String callerInfo) {
        if (loggerInstance != null) {
            if (timeMs > 1000) {
                loggerInstance.getLogger(callerInfo).warn("[SQL-SLOW] Execution time: " + timeMs + "ms - " + sql);
            } else {
                loggerInstance.getLogger(callerInfo).info("[SQL-TIME] " + timeMs + "ms");
            }
        } else {
            if (timeMs > 1000) {
                UniversalLogger.getLogger(callerInfo).warn("[SQL-SLOW] Execution time: " + timeMs + "ms - " + sql);
            } else {
                UniversalLogger.getLogger(callerInfo).info("[SQL-TIME] " + timeMs + "ms");
            }
        }
    }

    /**
     * Row count 로깅
     */
    public void logRowCount(int rowCount, String callerInfo) {
        if (loggerInstance != null) {
            loggerInstance.getLogger(callerInfo).info("[SQL-ROWS] Total: " + rowCount);
        } else {
            UniversalLogger.getLogger(callerInfo).info("[SQL-ROWS] Total: " + rowCount);
        }
    }

    /**
     * Update count 로깅
     */
    public void logUpdateCount(int updateCount, String callerInfo) {
        if (loggerInstance != null) {
            loggerInstance.getLogger(callerInfo).info("[SQL-ROWS] Updates: " + updateCount);
        } else {
            UniversalLogger.getLogger(callerInfo).info("[SQL-ROWS] Updates: " + updateCount);
        }
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

        if (loggerInstance != null) {
            loggerInstance.getLogger(callerInfo).info(sb.toString());
        } else {
            UniversalLogger.getLogger(callerInfo).info(sb.toString());
        }
    }

    /**
     * SQL 로깅을 위한 logger 이름 반환
     * 스택 트레이스 분석 없이 고정된 이름 사용
     */
    public String getCallerInfo() {
        return "UniversalLogger";
    }
}
