package com.universallogger.sql;

import com.universallogger.LoggerInstance;

import java.sql.*;

/**
 * Statement wrapper for SQL capture
 */
class SqlCaptureStatement implements Statement {
    protected final Statement targetStatement;
    protected final LoggerInstance loggerInstance;
    protected final SqlLogger sqlLogger;

    public SqlCaptureStatement(Statement targetStatement, LoggerInstance loggerInstance) {
        this.targetStatement = targetStatement;
        this.loggerInstance = loggerInstance;
        this.sqlLogger = new SqlLogger(loggerInstance);
    }

    @Override
    public boolean execute(String sql) throws SQLException {
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
        String callerInfo = sqlLogger.getCallerInfo();
        sqlLogger.logSql(sql, callerInfo);
        long start = System.currentTimeMillis();
        try {
            ResultSet rs = targetStatement.executeQuery(sql);

            // Try to count rows using last() and getRow() if ResultSet is scrollable
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
                // ResultSet might not be scrollable, ignore
            }

            // Wrap ResultSet to count rows for forward-only ResultSets
            return new RowCountingResultSet(rs, callerInfo, loggerInstance);
        } finally {
            sqlLogger.logExecutionTime(sql, System.currentTimeMillis() - start, callerInfo);
        }
    }

    @Override
    public int executeUpdate(String sql) throws SQLException {
        String callerInfo = sqlLogger.getCallerInfo();
        sqlLogger.logSql(sql, callerInfo);
        long start = System.currentTimeMillis();
        try {
            int updateCount = targetStatement.executeUpdate(sql);
            // Log update count
            if (updateCount != -1) {
                sqlLogger.logUpdateCount(updateCount, callerInfo);
            }
            return updateCount;
        } finally {
            sqlLogger.logExecutionTime(sql, System.currentTimeMillis() - start, callerInfo);
        }
    }

    // Delegate methods
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
