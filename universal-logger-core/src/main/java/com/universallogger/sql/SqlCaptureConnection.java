package com.universallogger.sql;

import com.universallogger.config.LoggerConfig;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Executor;

/**
 * SQL 캡처를 위한 Connection 래퍼 클래스
 *
 * JDBC Connection을 래핑하여 Statement, PreparedStatement, CallableStatement 생성 시
 * 각각의 SQL 캡처 래퍼 클래스로 감싸서 반환합니다.
 *
 * <p>주요 기능:</p>
 * <ul>
 *   <li>createStatement() → {@link SqlCaptureStatement} 반환</li>
 *   <li>prepareStatement() → {@link SqlCapturePreparedStatement} 반환</li>
 *   <li>prepareCall() → {@link SqlCaptureCallableStatement} 반환</li>
 *   <li>행 수 카운트를 위해 가능한 경우 스크롤 가능한 ResultSet 생성</li>
 * </ul>
 *
 * @see SqlCaptureProxy
 * @see SqlCaptureStatement
 * @see SqlCapturePreparedStatement
 * @see SqlCaptureCallableStatement
 */
class SqlCaptureConnection implements Connection {
    private static final Logger logger = LoggerFactory.getLogger(SqlCaptureConnection.class);

    private final Connection targetConnection;
    private final LoggerConfig config;
    private final boolean sqlLoggingEnabled;

    public SqlCaptureConnection(Connection targetConnection, LoggerConfig config) {
        this.targetConnection = targetConnection;
        this.config = config;
        this.sqlLoggingEnabled = getSqlLoggingEnabled(config);
    }

    private static boolean getSqlLoggingEnabled(LoggerConfig config) {
        try {
            if (config != null && config.getSql() != null) {
                return config.getSql().isParameterSubstitution();
            }
        } catch (Exception e) {
            // 설정을 가져올 수 없는 경우 기본 동작 (OFF)
        }
        return false;
    }

    @Override
    public Statement createStatement() throws SQLException {
        return new SqlCaptureStatement(targetConnection.createStatement(), config, sqlLoggingEnabled);
    }

    @Override
    public PreparedStatement prepareStatement(String sql) throws SQLException {
        // 행 수 카운트를 위해 스크롤 가능한 ResultSet으로 PreparedStatement 생성
        try {
            return new SqlCapturePreparedStatement(
                targetConnection.prepareStatement(sql, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY),
                sql,
                config
            );
        } catch (SQLException e) {
            // 스크롤이 지원되지 않으면 기본값으로 폴백
            return new SqlCapturePreparedStatement(targetConnection.prepareStatement(sql), sql, config);
        }
    }

    @Override
    public CallableStatement prepareCall(String sql) throws SQLException {
        return new SqlCaptureCallableStatement(targetConnection.prepareCall(sql), sql, config);
    }

    // 나머지 모든 메서드는 위임
    @Override public void close() throws SQLException { targetConnection.close(); }
    @Override public boolean isClosed() throws SQLException { return targetConnection.isClosed(); }
    @Override public void commit() throws SQLException { targetConnection.commit(); }
    @Override public void rollback() throws SQLException { targetConnection.rollback(); }
    @Override public void setAutoCommit(boolean autoCommit) throws SQLException { targetConnection.setAutoCommit(autoCommit); }
    @Override public boolean getAutoCommit() throws SQLException { return targetConnection.getAutoCommit(); }
    @Override public DatabaseMetaData getMetaData() throws SQLException { return targetConnection.getMetaData(); }
    @Override public void setReadOnly(boolean readOnly) throws SQLException { targetConnection.setReadOnly(readOnly); }
    @Override public boolean isReadOnly() throws SQLException { return targetConnection.isReadOnly(); }
    @Override public void setCatalog(String catalog) throws SQLException { targetConnection.setCatalog(catalog); }
    @Override public String getCatalog() throws SQLException { return targetConnection.getCatalog(); }
    @Override public void setTransactionIsolation(int level) throws SQLException { targetConnection.setTransactionIsolation(level); }
    @Override public int getTransactionIsolation() throws SQLException { return targetConnection.getTransactionIsolation(); }
    @Override public SQLWarning getWarnings() throws SQLException { return targetConnection.getWarnings(); }
    @Override public void clearWarnings() throws SQLException { targetConnection.clearWarnings(); }
    @Override public Statement createStatement(int resultSetType, int resultSetConcurrency) throws SQLException { return new SqlCaptureStatement(targetConnection.createStatement(resultSetType, resultSetConcurrency), config, sqlLoggingEnabled); }
    @Override public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency) throws SQLException {
        // 행 수 카운트를 위해 스크롤 가능한 ResultSet 강제 적용
        return new SqlCapturePreparedStatement(
            targetConnection.prepareStatement(sql, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY),
            sql,
            config
        );
    }
    @Override public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency) throws SQLException { return new SqlCaptureCallableStatement(targetConnection.prepareCall(sql, resultSetType, resultSetConcurrency), sql, config); }
    @Override public Map<String, Class<?>> getTypeMap() throws SQLException { return targetConnection.getTypeMap(); }
    @Override public void setTypeMap(Map<String, Class<?>> map) throws SQLException { targetConnection.setTypeMap(map); }
    @Override public void setHoldability(int holdability) throws SQLException { targetConnection.setHoldability(holdability); }
    @Override public int getHoldability() throws SQLException { return targetConnection.getHoldability(); }
    @Override public Savepoint setSavepoint() throws SQLException { return targetConnection.setSavepoint(); }
    @Override public Savepoint setSavepoint(String name) throws SQLException { return targetConnection.setSavepoint(name); }
    @Override public void rollback(Savepoint savepoint) throws SQLException { targetConnection.rollback(savepoint); }
    @Override public void releaseSavepoint(Savepoint savepoint) throws SQLException { targetConnection.releaseSavepoint(savepoint); }
    @Override public Statement createStatement(int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException { return new SqlCaptureStatement(targetConnection.createStatement(resultSetType, resultSetConcurrency, resultSetHoldability), config, sqlLoggingEnabled); }
    @Override public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException { return new SqlCapturePreparedStatement(targetConnection.prepareStatement(sql, resultSetType, resultSetConcurrency, resultSetHoldability), sql, config); }
    @Override public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException { return new SqlCaptureCallableStatement(targetConnection.prepareCall(sql, resultSetType, resultSetConcurrency, resultSetHoldability), sql, config); }
    @Override public PreparedStatement prepareStatement(String sql, int autoGeneratedKeys) throws SQLException { return new SqlCapturePreparedStatement(targetConnection.prepareStatement(sql, autoGeneratedKeys), sql, config); }
    @Override public PreparedStatement prepareStatement(String sql, int[] columnIndexes) throws SQLException { return new SqlCapturePreparedStatement(targetConnection.prepareStatement(sql, columnIndexes), sql, config); }
    @Override public PreparedStatement prepareStatement(String sql, String[] columnNames) throws SQLException { return new SqlCapturePreparedStatement(targetConnection.prepareStatement(sql, columnNames), sql, config); }
    @Override public Clob createClob() throws SQLException { return targetConnection.createClob(); }
    @Override public Blob createBlob() throws SQLException { return targetConnection.createBlob(); }
    @Override public NClob createNClob() throws SQLException { return targetConnection.createNClob(); }
    @Override public SQLXML createSQLXML() throws SQLException { return targetConnection.createSQLXML(); }
    @Override public boolean isValid(int timeout) throws SQLException { return targetConnection.isValid(timeout); }
    @Override public void setClientInfo(String name, String value) { try { targetConnection.setClientInfo(name, value); } catch (Exception e) { logger.debug("Failed to set client info: {}={}", name, value, e); } }
    @Override public void setClientInfo(Properties properties) { try { targetConnection.setClientInfo(properties); } catch (Exception e) { logger.debug("Failed to set client info properties", e); } }
    @Override public String getClientInfo(String name) throws SQLException { return targetConnection.getClientInfo(name); }
    @Override public Properties getClientInfo() throws SQLException { return targetConnection.getClientInfo(); }
    @Override public Array createArrayOf(String typeName, Object[] elements) throws SQLException { return targetConnection.createArrayOf(typeName, elements); }
    @Override public Struct createStruct(String typeName, Object[] attributes) throws SQLException { return targetConnection.createStruct(typeName, attributes); }
    @Override public void setSchema(String schema) throws SQLException { targetConnection.setSchema(schema); }
    @Override public String getSchema() throws SQLException { return targetConnection.getSchema(); }
    @Override public void abort(Executor executor) throws SQLException { targetConnection.abort(executor); }
    @Override public void setNetworkTimeout(Executor executor, int milliseconds) throws SQLException { targetConnection.setNetworkTimeout(executor, milliseconds); }
    @Override public int getNetworkTimeout() throws SQLException { return targetConnection.getNetworkTimeout(); }
    @Override public String nativeSQL(String sql) throws SQLException { return targetConnection.nativeSQL(sql); }
    @Override public <T> T unwrap(Class<T> iface) throws SQLException { return targetConnection.unwrap(iface); }
    @Override public boolean isWrapperFor(Class<?> iface) throws SQLException { return targetConnection.isWrapperFor(iface); }
}
