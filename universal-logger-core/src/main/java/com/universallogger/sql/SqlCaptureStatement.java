package com.universallogger.sql;

import com.universallogger.config.LoggerConfig;

import java.sql.*;

/**
 * SQL 캡처를 위한 Statement 래퍼 클래스
 *
 * Statement를 래핑하여 SQL 실행 정보를 캡처하고 로깅합니다.
 * 직접 SQL 문자열을 전달받는 execute, executeQuery, executeUpdate 메서드를 오버라이드하여
 * SQL 실행 전후로 로깅을 수행합니다.
 *
 * <p>주요 기능:</p>
 * <ul>
 *   <li>SQL 쿼리 로깅</li>
 *   <li>SQL 실행 시간 측정</li>
 *   <li>SELECT 쿼리의 결과 행 수 추적 (RowCountingResultSet 활용)</li>
 *   <li>UPDATE/INSERT/DELETE 쿼리의 영향받은 행 수 로깅</li>
 * </ul>
 *
 * <p>상속 구조:</p>
 * <ul>
 *   <li>{@link SqlCapturePreparedStatement} - PreparedStatement 지원</li>
 *   <li>{@link SqlCaptureCallableStatement} - CallableStatement 지원</li>
 * </ul>
 *
 * @see SqlCaptureProxy
 * @see SqlLogger
 */
class SqlCaptureStatement implements Statement {
    protected final Statement targetStatement;
    protected final LoggerConfig config;
    protected final SqlLogger sqlLogger;
    protected final boolean sqlLoggingEnabled;

    public SqlCaptureStatement(Statement targetStatement, LoggerConfig config, boolean sqlLoggingEnabled) {
        this.targetStatement = targetStatement;
        this.config = config;
        this.sqlLogger = new Slf4jSqlLogger(config);
        this.sqlLoggingEnabled = sqlLoggingEnabled;
    }

    @Override
    public boolean execute(String sql) throws SQLException {
        if (!sqlLoggingEnabled) {
            return targetStatement.execute(sql);
        }

        String callerInfo = sqlLogger.getCallerInfo();
        sqlLogger.logSql(sql, callerInfo);
        long start = System.currentTimeMillis();
        try {
            return targetStatement.execute(sql);
        } finally {
            sqlLogger.logExecutionTime(sql, System.currentTimeMillis() - start, callerInfo);
        }
    }

    @Override
    public ResultSet executeQuery(String sql) throws SQLException {
        if (!sqlLoggingEnabled) {
            return targetStatement.executeQuery(sql);
        }

        String callerInfo = sqlLogger.getCallerInfo();
        sqlLogger.logSql(sql, callerInfo);
        long start = System.currentTimeMillis();
        try {
            ResultSet rs = targetStatement.executeQuery(sql);

            // ResultSet이 스크롤 가능한 경우 last()와 getRow()로 행 수 계산 시도
            int rowCount = -1;
            try {
                if (rs.getType() != ResultSet.TYPE_FORWARD_ONLY) {
                    rs.last();
                    rowCount = rs.getRow();
                    rs.beforeFirst();
                    if (rowCount >= 0) {
                        sqlLogger.logRowCount(rowCount, callerInfo);
                    }
                }
            } catch (SQLException e) {
                // ResultSet이 스크롤 불가능할 수 있음, 무시
            }

            // forward-only ResultSet의 행 수 카운트를 위해 ResultSet 래핑
            return new RowCountingResultSet(rs, callerInfo, config);
        } finally {
            sqlLogger.logExecutionTime(sql, System.currentTimeMillis() - start, callerInfo);
        }
    }

    @Override
    public int executeUpdate(String sql) throws SQLException {
        if (!sqlLoggingEnabled) {
            return targetStatement.executeUpdate(sql);
        }

        String callerInfo = sqlLogger.getCallerInfo();
        sqlLogger.logSql(sql, callerInfo);
        long start = System.currentTimeMillis();
        try {
            int updateCount = targetStatement.executeUpdate(sql);
            // 업데이트 수 로깅
            if (updateCount != -1) {
                sqlLogger.logUpdateCount(updateCount, callerInfo);
            }
            return updateCount;
        } finally {
            sqlLogger.logExecutionTime(sql, System.currentTimeMillis() - start, callerInfo);
        }
    }

    // 위임 메서드
    @Override public void close() throws SQLException { targetStatement.close(); }
    @Override public int getMaxFieldSize() throws SQLException { return targetStatement.getMaxFieldSize(); }
    @Override public void setMaxFieldSize(int max) throws SQLException { targetStatement.setMaxFieldSize(max); }
    @Override public int getMaxRows() throws SQLException { return targetStatement.getMaxRows(); }
    @Override public void setMaxRows(int max) throws SQLException { targetStatement.setMaxRows(max); }
    @Override public void setEscapeProcessing(boolean enable) throws SQLException { targetStatement.setEscapeProcessing(enable); }
    @Override public int getQueryTimeout() throws SQLException { return targetStatement.getQueryTimeout(); }
    @Override public void setQueryTimeout(int seconds) throws SQLException { targetStatement.setQueryTimeout(seconds); }
    @Override public void cancel() throws SQLException { targetStatement.cancel(); }
    @Override public SQLWarning getWarnings() throws SQLException { return targetStatement.getWarnings(); }
    @Override public void clearWarnings() throws SQLException { targetStatement.clearWarnings(); }
    @Override public void setCursorName(String name) throws SQLException { targetStatement.setCursorName(name); }
    @Override public ResultSet getResultSet() throws SQLException { return targetStatement.getResultSet(); }
    @Override public int getUpdateCount() throws SQLException { return targetStatement.getUpdateCount(); }
    @Override public boolean getMoreResults() throws SQLException { return targetStatement.getMoreResults(); }
    @Override public void setFetchDirection(int direction) throws SQLException { targetStatement.setFetchDirection(direction); }
    @Override public int getFetchDirection() throws SQLException { return targetStatement.getFetchDirection(); }
    @Override public void setFetchSize(int rows) throws SQLException { targetStatement.setFetchSize(rows); }
    @Override public int getFetchSize() throws SQLException { return targetStatement.getFetchSize(); }
    @Override public int getResultSetConcurrency() throws SQLException { return targetStatement.getResultSetConcurrency(); }
    @Override public int getResultSetType() throws SQLException { return targetStatement.getResultSetType(); }
    @Override public void addBatch(String sql) throws SQLException { targetStatement.addBatch(sql); }
    @Override public void clearBatch() throws SQLException { targetStatement.clearBatch(); }
    @Override public int[] executeBatch() throws SQLException { return targetStatement.executeBatch(); }
    @Override public Connection getConnection() throws SQLException { return targetStatement.getConnection(); }
    @Override public boolean getMoreResults(int current) throws SQLException { return targetStatement.getMoreResults(current); }
    @Override public ResultSet getGeneratedKeys() throws SQLException { return targetStatement.getGeneratedKeys(); }
    @Override public int executeUpdate(String sql, int autoGeneratedKeys) throws SQLException {
        String callerInfo = sqlLogger.getCallerInfo();
        sqlLogger.logSql(sql, callerInfo);
        return targetStatement.executeUpdate(sql, autoGeneratedKeys);
    }
    @Override public int executeUpdate(String sql, int[] columnIndexes) throws SQLException {
        String callerInfo = sqlLogger.getCallerInfo();
        sqlLogger.logSql(sql, callerInfo);
        return targetStatement.executeUpdate(sql, columnIndexes);
    }
    @Override public int executeUpdate(String sql, String[] columnNames) throws SQLException {
        String callerInfo = sqlLogger.getCallerInfo();
        sqlLogger.logSql(sql, callerInfo);
        return targetStatement.executeUpdate(sql, columnNames);
    }
    @Override public boolean execute(String sql, int autoGeneratedKeys) throws SQLException {
        String callerInfo = sqlLogger.getCallerInfo();
        sqlLogger.logSql(sql, callerInfo);
        return targetStatement.execute(sql, autoGeneratedKeys);
    }
    @Override public boolean execute(String sql, int[] columnIndexes) throws SQLException {
        String callerInfo = sqlLogger.getCallerInfo();
        sqlLogger.logSql(sql, callerInfo);
        return targetStatement.execute(sql, columnIndexes);
    }
    @Override public boolean execute(String sql, String[] columnNames) throws SQLException {
        String callerInfo = sqlLogger.getCallerInfo();
        sqlLogger.logSql(sql, callerInfo);
        return targetStatement.execute(sql, columnNames);
    }
    @Override public int getResultSetHoldability() throws SQLException { return targetStatement.getResultSetHoldability(); }
    @Override public boolean isClosed() throws SQLException { return targetStatement.isClosed(); }
    @Override public void setPoolable(boolean poolable) throws SQLException { targetStatement.setPoolable(poolable); }
    @Override public boolean isPoolable() throws SQLException { return targetStatement.isPoolable(); }
    @Override public void closeOnCompletion() throws SQLException { targetStatement.closeOnCompletion(); }
    @Override public boolean isCloseOnCompletion() throws SQLException { return targetStatement.isCloseOnCompletion(); }
    @Override public <T> T unwrap(Class<T> iface) throws SQLException { return targetStatement.unwrap(iface); }
    @Override public boolean isWrapperFor(Class<?> iface) throws SQLException { return targetStatement.isWrapperFor(iface); }
}
