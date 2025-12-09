package com.universallogger.sql;

import com.universallogger.LoggerInstance;
import com.universallogger.UniversalLogger;

import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.*;
import java.util.Calendar;
import java.util.Collections;
import java.util.Map;

/**
 * PreparedStatement wrapper for SQL capture with parameters
 */
class SqlCapturePreparedStatement extends SqlCaptureStatement implements PreparedStatement {
    private final PreparedStatement targetPreparedStatement;
    private final String sql;
    private final Map<Integer, Object> parameters = Collections.synchronizedMap(new java.util.HashMap<>());
    private final boolean useParameterSubstitution;

    public SqlCapturePreparedStatement(PreparedStatement targetPreparedStatement, String sql, LoggerInstance loggerInstance) {
        // 생성자에서 한 번만 설정 읽기 (성능 최적화)
        super(targetPreparedStatement, loggerInstance, getSqlLoggingEnabled(loggerInstance));
        this.targetPreparedStatement = targetPreparedStatement;
        this.sql = sql;
        this.useParameterSubstitution = sqlLoggingEnabled; // 부모의 sqlLoggingEnabled 사용
    }

    private static boolean getSqlLoggingEnabled(LoggerInstance loggerInstance) {
        try {
            com.universallogger.config.LoggerConfig config;
            if (loggerInstance != null) {
                config = loggerInstance.getConfig();
            } else {
                // Static API 사용 시 fallback
                config = UniversalLogger.getConfig();
            }
            if (config != null && config.getSql() != null) {
                return config.getSql().isParameterSubstitution();
            }
        } catch (Exception e) {
            // 설정을 가져올 수 없는 경우 기본 동작 (OFF)
        }
        return false;
    }

    @Override
    public ResultSet executeQuery() throws SQLException {
        // parameterSubstitution이 false면 SQL 로깅하지 않음
        if (!useParameterSubstitution) {
            return targetPreparedStatement.executeQuery();
        }

        long start = System.currentTimeMillis();
        String callerInfo = sqlLogger.getCallerInfo();
        int rowCount = -1;
        ResultSet rs = null;

        try {
            rs = targetPreparedStatement.executeQuery();

            // Try to count rows using last() and getRow() if ResultSet is scrollable
            try {
                if (rs.getType() != ResultSet.TYPE_FORWARD_ONLY) {
                    rs.last();
                    rowCount = rs.getRow();
                    rs.beforeFirst();
                }
            } catch (SQLException e) {
                // ResultSet might not be scrollable, ignore
            }

            return rs;
        } finally {
            long executionTime = System.currentTimeMillis() - start;
            sqlLogger.logAllResults(sql, parameters, rowCount, -1, executionTime, useParameterSubstitution, callerInfo);
        }
    }

    @Override
    public int executeUpdate() throws SQLException {
        // parameterSubstitution이 false면 SQL 로깅하지 않음
        if (!useParameterSubstitution) {
            return targetPreparedStatement.executeUpdate();
        }

        long start = System.currentTimeMillis();
        String callerInfo = sqlLogger.getCallerInfo();
        int updateCount = -1;

        try {
            updateCount = targetPreparedStatement.executeUpdate();
            return updateCount;
        } finally {
            long executionTime = System.currentTimeMillis() - start;
            sqlLogger.logAllResults(sql, parameters, -1, updateCount, executionTime, useParameterSubstitution, callerInfo);
        }
    }

    @Override
    public boolean execute() throws SQLException {
        // parameterSubstitution이 false면 SQL 로깅하지 않음
        if (!useParameterSubstitution) {
            return targetPreparedStatement.execute();
        }

        long start = System.currentTimeMillis();
        String callerInfo = sqlLogger.getCallerInfo();
        int rowCount = -1;
        int updateCount = -1;

        try {
            boolean hasResultSet = targetPreparedStatement.execute();

            if (hasResultSet) {
                // Query returned a ResultSet - count rows
                ResultSet rs = targetPreparedStatement.getResultSet();
                if (rs != null) {
                    // Try to count rows using last() and getRow() if ResultSet is scrollable
                    try {
                        if (rs.getType() != ResultSet.TYPE_FORWARD_ONLY) {
                            rs.last();
                            rowCount = rs.getRow();
                            rs.beforeFirst();
                        }
                    } catch (SQLException e) {
                        // ResultSet might not be scrollable, ignore
                    }
                }
            } else {
                // Query returned update count
                updateCount = targetPreparedStatement.getUpdateCount();
            }

            return hasResultSet;
        } finally {
            long executionTime = System.currentTimeMillis() - start;
            sqlLogger.logAllResults(sql, parameters, rowCount, updateCount, executionTime, useParameterSubstitution, callerInfo);
        }
    }

    // Parameter tracking methods
    @Override public void setNull(int parameterIndex, int sqlType) throws SQLException { parameters.put(parameterIndex, null); targetPreparedStatement.setNull(parameterIndex, sqlType); }
    @Override public void setBoolean(int parameterIndex, boolean x) throws SQLException { parameters.put(parameterIndex, x); targetPreparedStatement.setBoolean(parameterIndex, x); }
    @Override public void setByte(int parameterIndex, byte x) throws SQLException { parameters.put(parameterIndex, x); targetPreparedStatement.setByte(parameterIndex, x); }
    @Override public void setShort(int parameterIndex, short x) throws SQLException { parameters.put(parameterIndex, x); targetPreparedStatement.setShort(parameterIndex, x); }
    @Override public void setInt(int parameterIndex, int x) throws SQLException { parameters.put(parameterIndex, x); targetPreparedStatement.setInt(parameterIndex, x); }
    @Override public void setLong(int parameterIndex, long x) throws SQLException { parameters.put(parameterIndex, x); targetPreparedStatement.setLong(parameterIndex, x); }
    @Override public void setFloat(int parameterIndex, float x) throws SQLException { parameters.put(parameterIndex, x); targetPreparedStatement.setFloat(parameterIndex, x); }
    @Override public void setDouble(int parameterIndex, double x) throws SQLException { parameters.put(parameterIndex, x); targetPreparedStatement.setDouble(parameterIndex, x); }
    @Override public void setBigDecimal(int parameterIndex, BigDecimal x) throws SQLException { parameters.put(parameterIndex, x); targetPreparedStatement.setBigDecimal(parameterIndex, x); }
    @Override public void setString(int parameterIndex, String x) throws SQLException { parameters.put(parameterIndex, x); targetPreparedStatement.setString(parameterIndex, x); }
    @Override public void setBytes(int parameterIndex, byte[] x) throws SQLException { parameters.put(parameterIndex, x); targetPreparedStatement.setBytes(parameterIndex, x); }
    @Override public void setDate(int parameterIndex, Date x) throws SQLException { parameters.put(parameterIndex, x); targetPreparedStatement.setDate(parameterIndex, x); }
    @Override public void setTime(int parameterIndex, Time x) throws SQLException { parameters.put(parameterIndex, x); targetPreparedStatement.setTime(parameterIndex, x); }
    @Override public void setTimestamp(int parameterIndex, Timestamp x) throws SQLException { parameters.put(parameterIndex, x); targetPreparedStatement.setTimestamp(parameterIndex, x); }
    @Override public void setObject(int parameterIndex, Object x) throws SQLException { parameters.put(parameterIndex, x); targetPreparedStatement.setObject(parameterIndex, x); }

    // Delegate methods
    @Override public void setAsciiStream(int parameterIndex, InputStream x, int length) throws SQLException { targetPreparedStatement.setAsciiStream(parameterIndex, x, length); }
    @Override public void setUnicodeStream(int parameterIndex, InputStream x, int length) throws SQLException { targetPreparedStatement.setUnicodeStream(parameterIndex, x, length); }
    @Override public void setBinaryStream(int parameterIndex, InputStream x, int length) throws SQLException { targetPreparedStatement.setBinaryStream(parameterIndex, x, length); }
    @Override public void clearParameters() throws SQLException { parameters.clear(); targetPreparedStatement.clearParameters(); }
    @Override public void setObject(int parameterIndex, Object x, int targetSqlType) throws SQLException { parameters.put(parameterIndex, x); targetPreparedStatement.setObject(parameterIndex, x, targetSqlType); }
    @Override public void addBatch() throws SQLException { targetPreparedStatement.addBatch(); }
    @Override public void setCharacterStream(int parameterIndex, Reader reader, int length) throws SQLException { targetPreparedStatement.setCharacterStream(parameterIndex, reader, length); }
    @Override public void setRef(int parameterIndex, Ref x) throws SQLException { targetPreparedStatement.setRef(parameterIndex, x); }
    @Override public void setBlob(int parameterIndex, Blob x) throws SQLException { targetPreparedStatement.setBlob(parameterIndex, x); }
    @Override public void setClob(int parameterIndex, Clob x) throws SQLException { targetPreparedStatement.setClob(parameterIndex, x); }
    @Override public void setArray(int parameterIndex, Array x) throws SQLException { targetPreparedStatement.setArray(parameterIndex, x); }
    @Override public ResultSetMetaData getMetaData() throws SQLException { return targetPreparedStatement.getMetaData(); }
    @Override public void setDate(int parameterIndex, Date x, Calendar cal) throws SQLException { parameters.put(parameterIndex, x); targetPreparedStatement.setDate(parameterIndex, x, cal); }
    @Override public void setTime(int parameterIndex, Time x, Calendar cal) throws SQLException { parameters.put(parameterIndex, x); targetPreparedStatement.setTime(parameterIndex, x, cal); }
    @Override public void setTimestamp(int parameterIndex, Timestamp x, Calendar cal) throws SQLException { parameters.put(parameterIndex, x); targetPreparedStatement.setTimestamp(parameterIndex, x, cal); }
    @Override public void setNull(int parameterIndex, int sqlType, String typeName) throws SQLException { parameters.put(parameterIndex, null); targetPreparedStatement.setNull(parameterIndex, sqlType, typeName); }
    @Override public void setURL(int parameterIndex, URL x) throws SQLException { parameters.put(parameterIndex, x); targetPreparedStatement.setURL(parameterIndex, x); }
    @Override public ParameterMetaData getParameterMetaData() throws SQLException { return targetPreparedStatement.getParameterMetaData(); }
    @Override public void setRowId(int parameterIndex, RowId x) throws SQLException { targetPreparedStatement.setRowId(parameterIndex, x); }
    @Override public void setNString(int parameterIndex, String value) throws SQLException { parameters.put(parameterIndex, value); targetPreparedStatement.setNString(parameterIndex, value); }
    @Override public void setNCharacterStream(int parameterIndex, Reader value, long length) throws SQLException { targetPreparedStatement.setNCharacterStream(parameterIndex, value, length); }
    @Override public void setNClob(int parameterIndex, NClob value) throws SQLException { targetPreparedStatement.setNClob(parameterIndex, value); }
    @Override public void setClob(int parameterIndex, Reader reader, long length) throws SQLException { targetPreparedStatement.setClob(parameterIndex, reader, length); }
    @Override public void setBlob(int parameterIndex, InputStream inputStream, long length) throws SQLException { targetPreparedStatement.setBlob(parameterIndex, inputStream, length); }
    @Override public void setNClob(int parameterIndex, Reader reader, long length) throws SQLException { targetPreparedStatement.setNClob(parameterIndex, reader, length); }
    @Override public void setSQLXML(int parameterIndex, SQLXML xmlObject) throws SQLException { targetPreparedStatement.setSQLXML(parameterIndex, xmlObject); }
    @Override public void setObject(int parameterIndex, Object x, int targetSqlType, int scaleOrLength) throws SQLException { parameters.put(parameterIndex, x); targetPreparedStatement.setObject(parameterIndex, x, targetSqlType, scaleOrLength); }
    @Override public void setAsciiStream(int parameterIndex, InputStream x, long length) throws SQLException { targetPreparedStatement.setAsciiStream(parameterIndex, x, length); }
    @Override public void setBinaryStream(int parameterIndex, InputStream x, long length) throws SQLException { targetPreparedStatement.setBinaryStream(parameterIndex, x, length); }
    @Override public void setCharacterStream(int parameterIndex, Reader reader, long length) throws SQLException { targetPreparedStatement.setCharacterStream(parameterIndex, reader, length); }
    @Override public void setAsciiStream(int parameterIndex, InputStream x) throws SQLException { targetPreparedStatement.setAsciiStream(parameterIndex, x); }
    @Override public void setBinaryStream(int parameterIndex, InputStream x) throws SQLException { targetPreparedStatement.setBinaryStream(parameterIndex, x); }
    @Override public void setCharacterStream(int parameterIndex, Reader reader) throws SQLException { targetPreparedStatement.setCharacterStream(parameterIndex, reader); }
    @Override public void setNCharacterStream(int parameterIndex, Reader value) throws SQLException { targetPreparedStatement.setNCharacterStream(parameterIndex, value); }
    @Override public void setClob(int parameterIndex, Reader reader) throws SQLException { targetPreparedStatement.setClob(parameterIndex, reader); }
    @Override public void setBlob(int parameterIndex, InputStream inputStream) throws SQLException { targetPreparedStatement.setBlob(parameterIndex, inputStream); }
    @Override public void setNClob(int parameterIndex, Reader reader) throws SQLException { targetPreparedStatement.setNClob(parameterIndex, reader); }
}
