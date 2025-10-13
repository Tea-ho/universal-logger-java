package com.universallogger.sql;

import com.universallogger.UniversalLogger;

import javax.sql.DataSource;
import java.io.PrintWriter;
import java.sql.*;
import java.util.logging.Logger;

/**
 * DataSource Proxy for automatic SQL capture
 * Works with any database (MySQL, Oracle, MSSQL, MongoDB JDBC, etc.)
 */
public class SqlCaptureProxy implements DataSource {

    private final DataSource targetDataSource;
    private final boolean enabled;

    public SqlCaptureProxy(DataSource targetDataSource) {
        this(targetDataSource, true);
    }

    public SqlCaptureProxy(DataSource targetDataSource, boolean enabled) {
        this.targetDataSource = targetDataSource;
        this.enabled = enabled;
    }

    @Override
    public Connection getConnection() throws SQLException {
        Connection connection = targetDataSource.getConnection();
        return enabled ? new SqlCaptureConnection(connection) : connection;
    }

    @Override
    public Connection getConnection(String username, String password) throws SQLException {
        Connection connection = targetDataSource.getConnection(username, password);
        return enabled ? new SqlCaptureConnection(connection) : connection;
    }

    // Delegate methods
    @Override
    public PrintWriter getLogWriter() throws SQLException {
        return targetDataSource.getLogWriter();
    }

    @Override
    public void setLogWriter(PrintWriter out) throws SQLException {
        targetDataSource.setLogWriter(out);
    }

    @Override
    public void setLoginTimeout(int seconds) throws SQLException {
        targetDataSource.setLoginTimeout(seconds);
    }

    @Override
    public int getLoginTimeout() throws SQLException {
        return targetDataSource.getLoginTimeout();
    }

    @Override
    public Logger getParentLogger() {
        return Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
    }

    @Override
    public <T> T unwrap(Class<T> iface) throws SQLException {
        if (iface.isInstance(this)) {
            return iface.cast(this);
        }
        return targetDataSource.unwrap(iface);
    }

    @Override
    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        return iface.isInstance(this) || targetDataSource.isWrapperFor(iface);
    }

    /**
     * Connection wrapper for SQL capture
     */
    private static class SqlCaptureConnection implements Connection {
        private final Connection targetConnection;

        public SqlCaptureConnection(Connection targetConnection) {
            this.targetConnection = targetConnection;
        }

        @Override
        public Statement createStatement() throws SQLException {
            return new SqlCaptureStatement(targetConnection.createStatement());
        }

        @Override
        public PreparedStatement prepareStatement(String sql) throws SQLException {
            return new SqlCapturePreparedStatement(targetConnection.prepareStatement(sql), sql);
        }

        @Override
        public CallableStatement prepareCall(String sql) throws SQLException {
            return new SqlCaptureCallableStatement(targetConnection.prepareCall(sql), sql);
        }

        // Delegate all other methods
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
        @Override public Statement createStatement(int resultSetType, int resultSetConcurrency) throws SQLException { return new SqlCaptureStatement(targetConnection.createStatement(resultSetType, resultSetConcurrency)); }
        @Override public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency) throws SQLException { return new SqlCapturePreparedStatement(targetConnection.prepareStatement(sql, resultSetType, resultSetConcurrency), sql); }
        @Override public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency) throws SQLException { return new SqlCaptureCallableStatement(targetConnection.prepareCall(sql, resultSetType, resultSetConcurrency), sql); }
        @Override public java.util.Map<String, Class<?>> getTypeMap() throws SQLException { return targetConnection.getTypeMap(); }
        @Override public void setTypeMap(java.util.Map<String, Class<?>> map) throws SQLException { targetConnection.setTypeMap(map); }
        @Override public void setHoldability(int holdability) throws SQLException { targetConnection.setHoldability(holdability); }
        @Override public int getHoldability() throws SQLException { return targetConnection.getHoldability(); }
        @Override public Savepoint setSavepoint() throws SQLException { return targetConnection.setSavepoint(); }
        @Override public Savepoint setSavepoint(String name) throws SQLException { return targetConnection.setSavepoint(name); }
        @Override public void rollback(Savepoint savepoint) throws SQLException { targetConnection.rollback(savepoint); }
        @Override public void releaseSavepoint(Savepoint savepoint) throws SQLException { targetConnection.releaseSavepoint(savepoint); }
        @Override public Statement createStatement(int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException { return new SqlCaptureStatement(targetConnection.createStatement(resultSetType, resultSetConcurrency, resultSetHoldability)); }
        @Override public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException { return new SqlCapturePreparedStatement(targetConnection.prepareStatement(sql, resultSetType, resultSetConcurrency, resultSetHoldability), sql); }
        @Override public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException { return new SqlCaptureCallableStatement(targetConnection.prepareCall(sql, resultSetType, resultSetConcurrency, resultSetHoldability), sql); }
        @Override public PreparedStatement prepareStatement(String sql, int autoGeneratedKeys) throws SQLException { return new SqlCapturePreparedStatement(targetConnection.prepareStatement(sql, autoGeneratedKeys), sql); }
        @Override public PreparedStatement prepareStatement(String sql, int[] columnIndexes) throws SQLException { return new SqlCapturePreparedStatement(targetConnection.prepareStatement(sql, columnIndexes), sql); }
        @Override public PreparedStatement prepareStatement(String sql, String[] columnNames) throws SQLException { return new SqlCapturePreparedStatement(targetConnection.prepareStatement(sql, columnNames), sql); }
        @Override public Clob createClob() throws SQLException { return targetConnection.createClob(); }
        @Override public Blob createBlob() throws SQLException { return targetConnection.createBlob(); }
        @Override public NClob createNClob() throws SQLException { return targetConnection.createNClob(); }
        @Override public SQLXML createSQLXML() throws SQLException { return targetConnection.createSQLXML(); }
        @Override public boolean isValid(int timeout) throws SQLException { return targetConnection.isValid(timeout); }
        @Override public void setClientInfo(String name, String value) { try { targetConnection.setClientInfo(name, value); } catch (Exception e) {} }
        @Override public void setClientInfo(java.util.Properties properties) { try { targetConnection.setClientInfo(properties); } catch (Exception e) {} }
        @Override public String getClientInfo(String name) throws SQLException { return targetConnection.getClientInfo(name); }
        @Override public java.util.Properties getClientInfo() throws SQLException { return targetConnection.getClientInfo(); }
        @Override public Array createArrayOf(String typeName, Object[] elements) throws SQLException { return targetConnection.createArrayOf(typeName, elements); }
        @Override public Struct createStruct(String typeName, Object[] attributes) throws SQLException { return targetConnection.createStruct(typeName, attributes); }
        @Override public void setSchema(String schema) throws SQLException { targetConnection.setSchema(schema); }
        @Override public String getSchema() throws SQLException { return targetConnection.getSchema(); }
        @Override public void abort(java.util.concurrent.Executor executor) throws SQLException { targetConnection.abort(executor); }
        @Override public void setNetworkTimeout(java.util.concurrent.Executor executor, int milliseconds) throws SQLException { targetConnection.setNetworkTimeout(executor, milliseconds); }
        @Override public int getNetworkTimeout() throws SQLException { return targetConnection.getNetworkTimeout(); }
        @Override public String nativeSQL(String sql) throws SQLException { return targetConnection.nativeSQL(sql); }
        @Override public <T> T unwrap(Class<T> iface) throws SQLException { return targetConnection.unwrap(iface); }
        @Override public boolean isWrapperFor(Class<?> iface) throws SQLException { return targetConnection.isWrapperFor(iface); }
    }

    /**
     * Statement wrapper for SQL capture
     */
    private static class SqlCaptureStatement implements Statement {
        private final Statement targetStatement;

        public SqlCaptureStatement(Statement targetStatement) {
            this.targetStatement = targetStatement;
        }

        @Override
        public boolean execute(String sql) throws SQLException {
            logSql(sql);
            long start = System.currentTimeMillis();
            try {
                return targetStatement.execute(sql);
            } finally {
                logExecutionTime(sql, System.currentTimeMillis() - start);
            }
        }

        @Override
        public ResultSet executeQuery(String sql) throws SQLException {
            logSql(sql);
            long start = System.currentTimeMillis();
            try {
                return targetStatement.executeQuery(sql);
            } finally {
                logExecutionTime(sql, System.currentTimeMillis() - start);
            }
        }

        @Override
        public int executeUpdate(String sql) throws SQLException {
            logSql(sql);
            long start = System.currentTimeMillis();
            try {
                return targetStatement.executeUpdate(sql);
            } finally {
                logExecutionTime(sql, System.currentTimeMillis() - start);
            }
        }

        private void logSql(String sql) {
            String callerInfo = getCallerInfo();
            UniversalLogger.getLogger(callerInfo).info("[SQL] " + sql);
        }

        private void logExecutionTime(String sql, long timeMs) {
            String callerInfo = getCallerInfo();
            if (timeMs > 1000) {
                UniversalLogger.getLogger(callerInfo).warn("[SQL-SLOW] Execution time: " + timeMs + "ms - " + sql);
            } else {
                UniversalLogger.getLogger(callerInfo).info("[SQL-TIME] " + timeMs + "ms");
            }
        }

        /**
         * SQL을 실행한 실제 호출자 정보를 추출
         * SQL에 가장 가까운 Mapper/Repository/Service를 찾음
         */
        private String getCallerInfo() {
            StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();

            // 우선순위: Mapper > Repository > Dao > Service > 기타
            StackTraceElement mapperElement = null;
            StackTraceElement repositoryElement = null;
            StackTraceElement daoElement = null;
            StackTraceElement serviceElement = null;
            StackTraceElement lastValidElement = null;

            // 정순으로 탐색 (SQL에 가까운 순서)
            for (StackTraceElement element : stackTrace) {
                String className = element.getClassName();
                String methodName = element.getMethodName();

                // 제외할 프레임워크 클래스들
                if (className.contains("SqlCaptureProxy") ||
                    className.startsWith("java.lang.Thread") ||
                    className.startsWith("com.universallogger") ||
                    className.startsWith("java.") ||
                    className.startsWith("javax.") ||
                    className.startsWith("sun.") ||
                    className.startsWith("com.sun.") ||
                    className.startsWith("jdk.") ||
                    className.startsWith("org.apache.ibatis") ||
                    className.startsWith("org.mybatis") ||
                    className.startsWith("org.hibernate") ||  // Hibernate
                    className.startsWith("org.springframework.aop") ||
                    className.startsWith("org.springframework.cglib") ||
                    className.startsWith("org.springframework.transaction") ||
                    className.startsWith("org.springframework.web") ||
                    className.startsWith("org.springframework.data.jpa") ||  // Spring Data JPA
                    className.contains("$Proxy") ||
                    className.contains("$$") ||  // CGLIB proxy
                    className.contains("EnhancerBy") ||  // CGLIB enhancer
                    className.contains("Aspect") ||  // AOP Aspect
                    className.contains("Interceptor") ||  // Interceptor
                    className.contains("MapperMethod") ||  // MyBatis MapperMethod
                    className.contains("MapperProxy") ||  // MyBatis MapperProxy
                    className.startsWith("com.zaxxer.hikari")) {
                    continue;
                }

                // 유효한 애플리케이션 클래스 저장
                lastValidElement = element;

                // 각 계층별로 마지막으로 발견된 것 저장 (SQL에 가장 가까운 것)
                // 나중에 나오는 것일수록 SQL 실행에 가까움
                if (className.contains("Mapper")) {
                    mapperElement = element;
                } else if (className.contains("Repository")) {
                    repositoryElement = element;
                } else if (className.contains("Dao")) {
                    daoElement = element;
                } else if (className.contains("Service")) {
                    serviceElement = element;
                }
            }

            // 우선순위대로 반환: Mapper > Repository > Dao > Service > 마지막 유효한 클래스
            StackTraceElement targetElement = null;
            if (mapperElement != null) {
                targetElement = mapperElement;
            } else if (repositoryElement != null) {
                targetElement = repositoryElement;
            } else if (daoElement != null) {
                targetElement = daoElement;
            } else if (serviceElement != null) {
                targetElement = serviceElement;
            } else if (lastValidElement != null) {
                targetElement = lastValidElement;
            }

            if (targetElement != null) {
                String className = targetElement.getClassName();
                String methodName = targetElement.getMethodName();
                int lineNumber = targetElement.getLineNumber();
                String simpleClassName = className.substring(className.lastIndexOf('.') + 1);
                if (simpleClassName.contains("$")) {
                    simpleClassName = simpleClassName.substring(0, simpleClassName.indexOf('$'));
                }
                // 라인 번호를 포함하여 정확한 위치 표시
                return simpleClassName + "." + methodName + ":" + lineNumber;
            }

            return "unknown";
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
        @Override public int executeUpdate(String sql, int autoGeneratedKeys) throws SQLException { logSql(sql); return targetStatement.executeUpdate(sql, autoGeneratedKeys); }
        @Override public int executeUpdate(String sql, int[] columnIndexes) throws SQLException { logSql(sql); return targetStatement.executeUpdate(sql, columnIndexes); }
        @Override public int executeUpdate(String sql, String[] columnNames) throws SQLException { logSql(sql); return targetStatement.executeUpdate(sql, columnNames); }
        @Override public boolean execute(String sql, int autoGeneratedKeys) throws SQLException { logSql(sql); return targetStatement.execute(sql, autoGeneratedKeys); }
        @Override public boolean execute(String sql, int[] columnIndexes) throws SQLException { logSql(sql); return targetStatement.execute(sql, columnIndexes); }
        @Override public boolean execute(String sql, String[] columnNames) throws SQLException { logSql(sql); return targetStatement.execute(sql, columnNames); }
        @Override public int getResultSetHoldability() throws SQLException { return targetStatement.getResultSetHoldability(); }
        @Override public boolean isClosed() throws SQLException { return targetStatement.isClosed(); }
        @Override public void setPoolable(boolean poolable) throws SQLException { targetStatement.setPoolable(poolable); }
        @Override public boolean isPoolable() throws SQLException { return targetStatement.isPoolable(); }
        @Override public void closeOnCompletion() throws SQLException { targetStatement.closeOnCompletion(); }
        @Override public boolean isCloseOnCompletion() throws SQLException { return targetStatement.isCloseOnCompletion(); }
        @Override public <T> T unwrap(Class<T> iface) throws SQLException { return targetStatement.unwrap(iface); }
        @Override public boolean isWrapperFor(Class<?> iface) throws SQLException { return targetStatement.isWrapperFor(iface); }
    }

    /**
     * PreparedStatement wrapper for SQL capture with parameters
     */
    private static class SqlCapturePreparedStatement extends SqlCaptureStatement implements PreparedStatement {
        private final PreparedStatement targetPreparedStatement;
        private final String sql;
        private final java.util.Map<Integer, Object> parameters = new java.util.HashMap<>();

        public SqlCapturePreparedStatement(PreparedStatement targetPreparedStatement, String sql) {
            super(targetPreparedStatement);
            this.targetPreparedStatement = targetPreparedStatement;
            this.sql = sql;
        }

        @Override
        public ResultSet executeQuery() throws SQLException {
            logSqlWithParams();
            long start = System.currentTimeMillis();
            try {
                return targetPreparedStatement.executeQuery();
            } finally {
                logExecutionTime(System.currentTimeMillis() - start);
            }
        }

        @Override
        public int executeUpdate() throws SQLException {
            logSqlWithParams();
            long start = System.currentTimeMillis();
            try {
                return targetPreparedStatement.executeUpdate();
            } finally {
                logExecutionTime(System.currentTimeMillis() - start);
            }
        }

        @Override
        public boolean execute() throws SQLException {
            logSqlWithParams();
            long start = System.currentTimeMillis();
            try {
                return targetPreparedStatement.execute();
            } finally {
                logExecutionTime(System.currentTimeMillis() - start);
            }
        }

        private void logSqlWithParams() {
            StringBuilder sb = new StringBuilder();
            sb.append("[SQL] ").append(sql);
            if (!parameters.isEmpty()) {
                sb.append(" | Parameters: ").append(parameters);
            }
            String callerInfo = getCallerInfo();
            UniversalLogger.getLogger(callerInfo).info(sb.toString());
        }

        private void logExecutionTime(long timeMs) {
            String callerInfo = getCallerInfo();
            if (timeMs > 1000) {
                UniversalLogger.getLogger(callerInfo).warn("[SQL-SLOW] Execution time: " + timeMs + "ms - " + sql);
            } else {
                UniversalLogger.getLogger(callerInfo).info("[SQL-TIME] " + timeMs + "ms");
            }
        }

        /**
         * SQL을 실행한 실제 호출자 정보를 추출
         * SQL에 가장 가까운 Mapper/Repository/Service를 찾음
         */
        private String getCallerInfo() {
            StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();

            // 우선순위: Mapper > Repository > Dao > Service > 기타
            StackTraceElement mapperElement = null;
            StackTraceElement repositoryElement = null;
            StackTraceElement daoElement = null;
            StackTraceElement serviceElement = null;
            StackTraceElement lastValidElement = null;

            // 정순으로 탐색 (SQL에 가까운 순서)
            for (StackTraceElement element : stackTrace) {
                String className = element.getClassName();
                String methodName = element.getMethodName();

                // 제외할 프레임워크 클래스들
                if (className.contains("SqlCaptureProxy") ||
                    className.startsWith("java.lang.Thread") ||
                    className.startsWith("com.universallogger") ||
                    className.startsWith("java.") ||
                    className.startsWith("javax.") ||
                    className.startsWith("sun.") ||
                    className.startsWith("com.sun.") ||
                    className.startsWith("jdk.") ||
                    className.startsWith("org.apache.ibatis") ||
                    className.startsWith("org.mybatis") ||
                    className.startsWith("org.hibernate") ||  // Hibernate
                    className.startsWith("org.springframework.aop") ||
                    className.startsWith("org.springframework.cglib") ||
                    className.startsWith("org.springframework.transaction") ||
                    className.startsWith("org.springframework.web") ||
                    className.startsWith("org.springframework.data.jpa") ||  // Spring Data JPA
                    className.contains("$Proxy") ||
                    className.contains("$$") ||  // CGLIB proxy
                    className.contains("EnhancerBy") ||  // CGLIB enhancer
                    className.contains("Aspect") ||  // AOP Aspect
                    className.contains("Interceptor") ||  // Interceptor
                    className.contains("MapperMethod") ||  // MyBatis MapperMethod
                    className.contains("MapperProxy") ||  // MyBatis MapperProxy
                    className.startsWith("com.zaxxer.hikari")) {
                    continue;
                }

                // 유효한 애플리케이션 클래스 저장
                lastValidElement = element;

                // 각 계층별로 마지막으로 발견된 것 저장 (SQL에 가장 가까운 것)
                // 나중에 나오는 것일수록 SQL 실행에 가까움
                if (className.contains("Mapper")) {
                    mapperElement = element;
                } else if (className.contains("Repository")) {
                    repositoryElement = element;
                } else if (className.contains("Dao")) {
                    daoElement = element;
                } else if (className.contains("Service")) {
                    serviceElement = element;
                }
            }

            // 우선순위대로 반환: Mapper > Repository > Dao > Service > 마지막 유효한 클래스
            StackTraceElement targetElement = null;
            if (mapperElement != null) {
                targetElement = mapperElement;
            } else if (repositoryElement != null) {
                targetElement = repositoryElement;
            } else if (daoElement != null) {
                targetElement = daoElement;
            } else if (serviceElement != null) {
                targetElement = serviceElement;
            } else if (lastValidElement != null) {
                targetElement = lastValidElement;
            }

            if (targetElement != null) {
                String className = targetElement.getClassName();
                String methodName = targetElement.getMethodName();
                int lineNumber = targetElement.getLineNumber();
                String simpleClassName = className.substring(className.lastIndexOf('.') + 1);
                if (simpleClassName.contains("$")) {
                    simpleClassName = simpleClassName.substring(0, simpleClassName.indexOf('$'));
                }
                // 라인 번호를 포함하여 정확한 위치 표시
                return simpleClassName + "." + methodName + ":" + lineNumber;
            }

            return "unknown";
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
        @Override public void setBigDecimal(int parameterIndex, java.math.BigDecimal x) throws SQLException { parameters.put(parameterIndex, x); targetPreparedStatement.setBigDecimal(parameterIndex, x); }
        @Override public void setString(int parameterIndex, String x) throws SQLException { parameters.put(parameterIndex, x); targetPreparedStatement.setString(parameterIndex, x); }
        @Override public void setBytes(int parameterIndex, byte[] x) throws SQLException { parameters.put(parameterIndex, x); targetPreparedStatement.setBytes(parameterIndex, x); }
        @Override public void setDate(int parameterIndex, Date x) throws SQLException { parameters.put(parameterIndex, x); targetPreparedStatement.setDate(parameterIndex, x); }
        @Override public void setTime(int parameterIndex, Time x) throws SQLException { parameters.put(parameterIndex, x); targetPreparedStatement.setTime(parameterIndex, x); }
        @Override public void setTimestamp(int parameterIndex, Timestamp x) throws SQLException { parameters.put(parameterIndex, x); targetPreparedStatement.setTimestamp(parameterIndex, x); }
        @Override public void setObject(int parameterIndex, Object x) throws SQLException { parameters.put(parameterIndex, x); targetPreparedStatement.setObject(parameterIndex, x); }

        // Delegate methods
        @Override public void setAsciiStream(int parameterIndex, java.io.InputStream x, int length) throws SQLException { targetPreparedStatement.setAsciiStream(parameterIndex, x, length); }
        @Override public void setUnicodeStream(int parameterIndex, java.io.InputStream x, int length) throws SQLException { targetPreparedStatement.setUnicodeStream(parameterIndex, x, length); }
        @Override public void setBinaryStream(int parameterIndex, java.io.InputStream x, int length) throws SQLException { targetPreparedStatement.setBinaryStream(parameterIndex, x, length); }
        @Override public void clearParameters() throws SQLException { parameters.clear(); targetPreparedStatement.clearParameters(); }
        @Override public void setObject(int parameterIndex, Object x, int targetSqlType) throws SQLException { parameters.put(parameterIndex, x); targetPreparedStatement.setObject(parameterIndex, x, targetSqlType); }
        @Override public void addBatch() throws SQLException { targetPreparedStatement.addBatch(); }
        @Override public void setCharacterStream(int parameterIndex, java.io.Reader reader, int length) throws SQLException { targetPreparedStatement.setCharacterStream(parameterIndex, reader, length); }
        @Override public void setRef(int parameterIndex, Ref x) throws SQLException { targetPreparedStatement.setRef(parameterIndex, x); }
        @Override public void setBlob(int parameterIndex, Blob x) throws SQLException { targetPreparedStatement.setBlob(parameterIndex, x); }
        @Override public void setClob(int parameterIndex, Clob x) throws SQLException { targetPreparedStatement.setClob(parameterIndex, x); }
        @Override public void setArray(int parameterIndex, Array x) throws SQLException { targetPreparedStatement.setArray(parameterIndex, x); }
        @Override public ResultSetMetaData getMetaData() throws SQLException { return targetPreparedStatement.getMetaData(); }
        @Override public void setDate(int parameterIndex, Date x, java.util.Calendar cal) throws SQLException { parameters.put(parameterIndex, x); targetPreparedStatement.setDate(parameterIndex, x, cal); }
        @Override public void setTime(int parameterIndex, Time x, java.util.Calendar cal) throws SQLException { parameters.put(parameterIndex, x); targetPreparedStatement.setTime(parameterIndex, x, cal); }
        @Override public void setTimestamp(int parameterIndex, Timestamp x, java.util.Calendar cal) throws SQLException { parameters.put(parameterIndex, x); targetPreparedStatement.setTimestamp(parameterIndex, x, cal); }
        @Override public void setNull(int parameterIndex, int sqlType, String typeName) throws SQLException { parameters.put(parameterIndex, null); targetPreparedStatement.setNull(parameterIndex, sqlType, typeName); }
        @Override public void setURL(int parameterIndex, java.net.URL x) throws SQLException { parameters.put(parameterIndex, x); targetPreparedStatement.setURL(parameterIndex, x); }
        @Override public ParameterMetaData getParameterMetaData() throws SQLException { return targetPreparedStatement.getParameterMetaData(); }
        @Override public void setRowId(int parameterIndex, RowId x) throws SQLException { targetPreparedStatement.setRowId(parameterIndex, x); }
        @Override public void setNString(int parameterIndex, String value) throws SQLException { parameters.put(parameterIndex, value); targetPreparedStatement.setNString(parameterIndex, value); }
        @Override public void setNCharacterStream(int parameterIndex, java.io.Reader value, long length) throws SQLException { targetPreparedStatement.setNCharacterStream(parameterIndex, value, length); }
        @Override public void setNClob(int parameterIndex, NClob value) throws SQLException { targetPreparedStatement.setNClob(parameterIndex, value); }
        @Override public void setClob(int parameterIndex, java.io.Reader reader, long length) throws SQLException { targetPreparedStatement.setClob(parameterIndex, reader, length); }
        @Override public void setBlob(int parameterIndex, java.io.InputStream inputStream, long length) throws SQLException { targetPreparedStatement.setBlob(parameterIndex, inputStream, length); }
        @Override public void setNClob(int parameterIndex, java.io.Reader reader, long length) throws SQLException { targetPreparedStatement.setNClob(parameterIndex, reader, length); }
        @Override public void setSQLXML(int parameterIndex, SQLXML xmlObject) throws SQLException { targetPreparedStatement.setSQLXML(parameterIndex, xmlObject); }
        @Override public void setObject(int parameterIndex, Object x, int targetSqlType, int scaleOrLength) throws SQLException { parameters.put(parameterIndex, x); targetPreparedStatement.setObject(parameterIndex, x, targetSqlType, scaleOrLength); }
        @Override public void setAsciiStream(int parameterIndex, java.io.InputStream x, long length) throws SQLException { targetPreparedStatement.setAsciiStream(parameterIndex, x, length); }
        @Override public void setBinaryStream(int parameterIndex, java.io.InputStream x, long length) throws SQLException { targetPreparedStatement.setBinaryStream(parameterIndex, x, length); }
        @Override public void setCharacterStream(int parameterIndex, java.io.Reader reader, long length) throws SQLException { targetPreparedStatement.setCharacterStream(parameterIndex, reader, length); }
        @Override public void setAsciiStream(int parameterIndex, java.io.InputStream x) throws SQLException { targetPreparedStatement.setAsciiStream(parameterIndex, x); }
        @Override public void setBinaryStream(int parameterIndex, java.io.InputStream x) throws SQLException { targetPreparedStatement.setBinaryStream(parameterIndex, x); }
        @Override public void setCharacterStream(int parameterIndex, java.io.Reader reader) throws SQLException { targetPreparedStatement.setCharacterStream(parameterIndex, reader); }
        @Override public void setNCharacterStream(int parameterIndex, java.io.Reader value) throws SQLException { targetPreparedStatement.setNCharacterStream(parameterIndex, value); }
        @Override public void setClob(int parameterIndex, java.io.Reader reader) throws SQLException { targetPreparedStatement.setClob(parameterIndex, reader); }
        @Override public void setBlob(int parameterIndex, java.io.InputStream inputStream) throws SQLException { targetPreparedStatement.setBlob(parameterIndex, inputStream); }
        @Override public void setNClob(int parameterIndex, java.io.Reader reader) throws SQLException { targetPreparedStatement.setNClob(parameterIndex, reader); }
    }

    /**
     * CallableStatement wrapper
     */
    private static class SqlCaptureCallableStatement extends SqlCapturePreparedStatement implements CallableStatement {
        private final CallableStatement targetCallableStatement;

        public SqlCaptureCallableStatement(CallableStatement targetCallableStatement, String sql) {
            super(targetCallableStatement, sql);
            this.targetCallableStatement = targetCallableStatement;
        }

        // Implement CallableStatement methods (delegate to target)
        @Override public void registerOutParameter(int parameterIndex, int sqlType) throws SQLException { targetCallableStatement.registerOutParameter(parameterIndex, sqlType); }
        @Override public void registerOutParameter(int parameterIndex, int sqlType, int scale) throws SQLException { targetCallableStatement.registerOutParameter(parameterIndex, sqlType, scale); }
        @Override public boolean wasNull() throws SQLException { return targetCallableStatement.wasNull(); }
        @Override public String getString(int parameterIndex) throws SQLException { return targetCallableStatement.getString(parameterIndex); }
        @Override public boolean getBoolean(int parameterIndex) throws SQLException { return targetCallableStatement.getBoolean(parameterIndex); }
        @Override public byte getByte(int parameterIndex) throws SQLException { return targetCallableStatement.getByte(parameterIndex); }
        @Override public short getShort(int parameterIndex) throws SQLException { return targetCallableStatement.getShort(parameterIndex); }
        @Override public int getInt(int parameterIndex) throws SQLException { return targetCallableStatement.getInt(parameterIndex); }
        @Override public long getLong(int parameterIndex) throws SQLException { return targetCallableStatement.getLong(parameterIndex); }
        @Override public float getFloat(int parameterIndex) throws SQLException { return targetCallableStatement.getFloat(parameterIndex); }
        @Override public double getDouble(int parameterIndex) throws SQLException { return targetCallableStatement.getDouble(parameterIndex); }
        @Override public java.math.BigDecimal getBigDecimal(int parameterIndex, int scale) throws SQLException { return targetCallableStatement.getBigDecimal(parameterIndex, scale); }
        @Override public byte[] getBytes(int parameterIndex) throws SQLException { return targetCallableStatement.getBytes(parameterIndex); }
        @Override public Date getDate(int parameterIndex) throws SQLException { return targetCallableStatement.getDate(parameterIndex); }
        @Override public Time getTime(int parameterIndex) throws SQLException { return targetCallableStatement.getTime(parameterIndex); }
        @Override public Timestamp getTimestamp(int parameterIndex) throws SQLException { return targetCallableStatement.getTimestamp(parameterIndex); }
        @Override public Object getObject(int parameterIndex) throws SQLException { return targetCallableStatement.getObject(parameterIndex); }
        @Override public java.math.BigDecimal getBigDecimal(int parameterIndex) throws SQLException { return targetCallableStatement.getBigDecimal(parameterIndex); }
        @Override public Object getObject(int parameterIndex, java.util.Map<String, Class<?>> map) throws SQLException { return targetCallableStatement.getObject(parameterIndex, map); }
        @Override public Ref getRef(int parameterIndex) throws SQLException { return targetCallableStatement.getRef(parameterIndex); }
        @Override public Blob getBlob(int parameterIndex) throws SQLException { return targetCallableStatement.getBlob(parameterIndex); }
        @Override public Clob getClob(int parameterIndex) throws SQLException { return targetCallableStatement.getClob(parameterIndex); }
        @Override public Array getArray(int parameterIndex) throws SQLException { return targetCallableStatement.getArray(parameterIndex); }
        @Override public Date getDate(int parameterIndex, java.util.Calendar cal) throws SQLException { return targetCallableStatement.getDate(parameterIndex, cal); }
        @Override public Time getTime(int parameterIndex, java.util.Calendar cal) throws SQLException { return targetCallableStatement.getTime(parameterIndex, cal); }
        @Override public Timestamp getTimestamp(int parameterIndex, java.util.Calendar cal) throws SQLException { return targetCallableStatement.getTimestamp(parameterIndex, cal); }
        @Override public void registerOutParameter(int parameterIndex, int sqlType, String typeName) throws SQLException { targetCallableStatement.registerOutParameter(parameterIndex, sqlType, typeName); }
        @Override public void registerOutParameter(String parameterName, int sqlType) throws SQLException { targetCallableStatement.registerOutParameter(parameterName, sqlType); }
        @Override public void registerOutParameter(String parameterName, int sqlType, int scale) throws SQLException { targetCallableStatement.registerOutParameter(parameterName, sqlType, scale); }
        @Override public void registerOutParameter(String parameterName, int sqlType, String typeName) throws SQLException { targetCallableStatement.registerOutParameter(parameterName, sqlType, typeName); }
        @Override public java.net.URL getURL(int parameterIndex) throws SQLException { return targetCallableStatement.getURL(parameterIndex); }
        @Override public void setURL(String parameterName, java.net.URL val) throws SQLException { targetCallableStatement.setURL(parameterName, val); }
        @Override public void setNull(String parameterName, int sqlType) throws SQLException { targetCallableStatement.setNull(parameterName, sqlType); }
        @Override public void setBoolean(String parameterName, boolean x) throws SQLException { targetCallableStatement.setBoolean(parameterName, x); }
        @Override public void setByte(String parameterName, byte x) throws SQLException { targetCallableStatement.setByte(parameterName, x); }
        @Override public void setShort(String parameterName, short x) throws SQLException { targetCallableStatement.setShort(parameterName, x); }
        @Override public void setInt(String parameterName, int x) throws SQLException { targetCallableStatement.setInt(parameterName, x); }
        @Override public void setLong(String parameterName, long x) throws SQLException { targetCallableStatement.setLong(parameterName, x); }
        @Override public void setFloat(String parameterName, float x) throws SQLException { targetCallableStatement.setFloat(parameterName, x); }
        @Override public void setDouble(String parameterName, double x) throws SQLException { targetCallableStatement.setDouble(parameterName, x); }
        @Override public void setBigDecimal(String parameterName, java.math.BigDecimal x) throws SQLException { targetCallableStatement.setBigDecimal(parameterName, x); }
        @Override public void setString(String parameterName, String x) throws SQLException { targetCallableStatement.setString(parameterName, x); }
        @Override public void setBytes(String parameterName, byte[] x) throws SQLException { targetCallableStatement.setBytes(parameterName, x); }
        @Override public void setDate(String parameterName, Date x) throws SQLException { targetCallableStatement.setDate(parameterName, x); }
        @Override public void setTime(String parameterName, Time x) throws SQLException { targetCallableStatement.setTime(parameterName, x); }
        @Override public void setTimestamp(String parameterName, Timestamp x) throws SQLException { targetCallableStatement.setTimestamp(parameterName, x); }
        @Override public void setAsciiStream(String parameterName, java.io.InputStream x, int length) throws SQLException { targetCallableStatement.setAsciiStream(parameterName, x, length); }
        @Override public void setBinaryStream(String parameterName, java.io.InputStream x, int length) throws SQLException { targetCallableStatement.setBinaryStream(parameterName, x, length); }
        @Override public void setObject(String parameterName, Object x, int targetSqlType, int scale) throws SQLException { targetCallableStatement.setObject(parameterName, x, targetSqlType, scale); }
        @Override public void setObject(String parameterName, Object x, int targetSqlType) throws SQLException { targetCallableStatement.setObject(parameterName, x, targetSqlType); }
        @Override public void setObject(String parameterName, Object x) throws SQLException { targetCallableStatement.setObject(parameterName, x); }
        @Override public void setCharacterStream(String parameterName, java.io.Reader reader, int length) throws SQLException { targetCallableStatement.setCharacterStream(parameterName, reader, length); }
        @Override public void setDate(String parameterName, Date x, java.util.Calendar cal) throws SQLException { targetCallableStatement.setDate(parameterName, x, cal); }
        @Override public void setTime(String parameterName, Time x, java.util.Calendar cal) throws SQLException { targetCallableStatement.setTime(parameterName, x, cal); }
        @Override public void setTimestamp(String parameterName, Timestamp x, java.util.Calendar cal) throws SQLException { targetCallableStatement.setTimestamp(parameterName, x, cal); }
        @Override public void setNull(String parameterName, int sqlType, String typeName) throws SQLException { targetCallableStatement.setNull(parameterName, sqlType, typeName); }
        @Override public String getString(String parameterName) throws SQLException { return targetCallableStatement.getString(parameterName); }
        @Override public boolean getBoolean(String parameterName) throws SQLException { return targetCallableStatement.getBoolean(parameterName); }
        @Override public byte getByte(String parameterName) throws SQLException { return targetCallableStatement.getByte(parameterName); }
        @Override public short getShort(String parameterName) throws SQLException { return targetCallableStatement.getShort(parameterName); }
        @Override public int getInt(String parameterName) throws SQLException { return targetCallableStatement.getInt(parameterName); }
        @Override public long getLong(String parameterName) throws SQLException { return targetCallableStatement.getLong(parameterName); }
        @Override public float getFloat(String parameterName) throws SQLException { return targetCallableStatement.getFloat(parameterName); }
        @Override public double getDouble(String parameterName) throws SQLException { return targetCallableStatement.getDouble(parameterName); }
        @Override public byte[] getBytes(String parameterName) throws SQLException { return targetCallableStatement.getBytes(parameterName); }
        @Override public Date getDate(String parameterName) throws SQLException { return targetCallableStatement.getDate(parameterName); }
        @Override public Time getTime(String parameterName) throws SQLException { return targetCallableStatement.getTime(parameterName); }
        @Override public Timestamp getTimestamp(String parameterName) throws SQLException { return targetCallableStatement.getTimestamp(parameterName); }
        @Override public Object getObject(String parameterName) throws SQLException { return targetCallableStatement.getObject(parameterName); }
        @Override public java.math.BigDecimal getBigDecimal(String parameterName) throws SQLException { return targetCallableStatement.getBigDecimal(parameterName); }
        @Override public Object getObject(String parameterName, java.util.Map<String, Class<?>> map) throws SQLException { return targetCallableStatement.getObject(parameterName, map); }
        @Override public Ref getRef(String parameterName) throws SQLException { return targetCallableStatement.getRef(parameterName); }
        @Override public Blob getBlob(String parameterName) throws SQLException { return targetCallableStatement.getBlob(parameterName); }
        @Override public Clob getClob(String parameterName) throws SQLException { return targetCallableStatement.getClob(parameterName); }
        @Override public Array getArray(String parameterName) throws SQLException { return targetCallableStatement.getArray(parameterName); }
        @Override public Date getDate(String parameterName, java.util.Calendar cal) throws SQLException { return targetCallableStatement.getDate(parameterName, cal); }
        @Override public Time getTime(String parameterName, java.util.Calendar cal) throws SQLException { return targetCallableStatement.getTime(parameterName, cal); }
        @Override public Timestamp getTimestamp(String parameterName, java.util.Calendar cal) throws SQLException { return targetCallableStatement.getTimestamp(parameterName, cal); }
        @Override public java.net.URL getURL(String parameterName) throws SQLException { return targetCallableStatement.getURL(parameterName); }
        @Override public RowId getRowId(int parameterIndex) throws SQLException { return targetCallableStatement.getRowId(parameterIndex); }
        @Override public RowId getRowId(String parameterName) throws SQLException { return targetCallableStatement.getRowId(parameterName); }
        @Override public void setRowId(String parameterName, RowId x) throws SQLException { targetCallableStatement.setRowId(parameterName, x); }
        @Override public void setNString(String parameterName, String value) throws SQLException { targetCallableStatement.setNString(parameterName, value); }
        @Override public void setNCharacterStream(String parameterName, java.io.Reader value, long length) throws SQLException { targetCallableStatement.setNCharacterStream(parameterName, value, length); }
        @Override public void setNClob(String parameterName, NClob value) throws SQLException { targetCallableStatement.setNClob(parameterName, value); }
        @Override public void setClob(String parameterName, java.io.Reader reader, long length) throws SQLException { targetCallableStatement.setClob(parameterName, reader, length); }
        @Override public void setBlob(String parameterName, java.io.InputStream inputStream, long length) throws SQLException { targetCallableStatement.setBlob(parameterName, inputStream, length); }
        @Override public void setNClob(String parameterName, java.io.Reader reader, long length) throws SQLException { targetCallableStatement.setNClob(parameterName, reader, length); }
        @Override public NClob getNClob(int parameterIndex) throws SQLException { return targetCallableStatement.getNClob(parameterIndex); }
        @Override public NClob getNClob(String parameterName) throws SQLException { return targetCallableStatement.getNClob(parameterName); }
        @Override public void setSQLXML(String parameterName, SQLXML xmlObject) throws SQLException { targetCallableStatement.setSQLXML(parameterName, xmlObject); }
        @Override public SQLXML getSQLXML(int parameterIndex) throws SQLException { return targetCallableStatement.getSQLXML(parameterIndex); }
        @Override public SQLXML getSQLXML(String parameterName) throws SQLException { return targetCallableStatement.getSQLXML(parameterName); }
        @Override public String getNString(int parameterIndex) throws SQLException { return targetCallableStatement.getNString(parameterIndex); }
        @Override public String getNString(String parameterName) throws SQLException { return targetCallableStatement.getNString(parameterName); }
        @Override public java.io.Reader getNCharacterStream(int parameterIndex) throws SQLException { return targetCallableStatement.getNCharacterStream(parameterIndex); }
        @Override public java.io.Reader getNCharacterStream(String parameterName) throws SQLException { return targetCallableStatement.getNCharacterStream(parameterName); }
        @Override public java.io.Reader getCharacterStream(int parameterIndex) throws SQLException { return targetCallableStatement.getCharacterStream(parameterIndex); }
        @Override public java.io.Reader getCharacterStream(String parameterName) throws SQLException { return targetCallableStatement.getCharacterStream(parameterName); }
        @Override public void setBlob(String parameterName, Blob x) throws SQLException { targetCallableStatement.setBlob(parameterName, x); }
        @Override public void setClob(String parameterName, Clob x) throws SQLException { targetCallableStatement.setClob(parameterName, x); }
        @Override public void setAsciiStream(String parameterName, java.io.InputStream x, long length) throws SQLException { targetCallableStatement.setAsciiStream(parameterName, x, length); }
        @Override public void setBinaryStream(String parameterName, java.io.InputStream x, long length) throws SQLException { targetCallableStatement.setBinaryStream(parameterName, x, length); }
        @Override public void setCharacterStream(String parameterName, java.io.Reader reader, long length) throws SQLException { targetCallableStatement.setCharacterStream(parameterName, reader, length); }
        @Override public void setAsciiStream(String parameterName, java.io.InputStream x) throws SQLException { targetCallableStatement.setAsciiStream(parameterName, x); }
        @Override public void setBinaryStream(String parameterName, java.io.InputStream x) throws SQLException { targetCallableStatement.setBinaryStream(parameterName, x); }
        @Override public void setCharacterStream(String parameterName, java.io.Reader reader) throws SQLException { targetCallableStatement.setCharacterStream(parameterName, reader); }
        @Override public void setNCharacterStream(String parameterName, java.io.Reader value) throws SQLException { targetCallableStatement.setNCharacterStream(parameterName, value); }
        @Override public void setClob(String parameterName, java.io.Reader reader) throws SQLException { targetCallableStatement.setClob(parameterName, reader); }
        @Override public void setBlob(String parameterName, java.io.InputStream inputStream) throws SQLException { targetCallableStatement.setBlob(parameterName, inputStream); }
        @Override public void setNClob(String parameterName, java.io.Reader reader) throws SQLException { targetCallableStatement.setNClob(parameterName, reader); }
        @Override public <T> T getObject(int parameterIndex, Class<T> type) throws SQLException { return targetCallableStatement.getObject(parameterIndex, type); }
        @Override public <T> T getObject(String parameterName, Class<T> type) throws SQLException { return targetCallableStatement.getObject(parameterName, type); }
    }
}
