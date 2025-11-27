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
            // Create PreparedStatement with SCROLLABLE result set for row counting
            try {
                return new SqlCapturePreparedStatement(
                    targetConnection.prepareStatement(sql, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY),
                    sql
                );
            } catch (SQLException e) {
                // Fallback to default if scrollable not supported
                return new SqlCapturePreparedStatement(targetConnection.prepareStatement(sql), sql);
            }
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
        @Override public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency) throws SQLException {
            // Force SCROLLABLE result set for row counting
            return new SqlCapturePreparedStatement(
                targetConnection.prepareStatement(sql, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY),
                sql
            );
        }
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
            String callerInfo = getCallerInfo();
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
                            UniversalLogger.getLogger(callerInfo).info("[SQL-ROWS] Total: " + rowCount);
                        }
                    }
                } catch (SQLException e) {
                    // ResultSet might not be scrollable, ignore
                }

                // Wrap ResultSet to count rows for forward-only ResultSets
                return new RowCountingResultSet(rs, callerInfo);
            } finally {
                logExecutionTime(sql, System.currentTimeMillis() - start);
            }
        }

        @Override
        public int executeUpdate(String sql) throws SQLException {
            logSql(sql);
            long start = System.currentTimeMillis();
            try {
                int updateCount = targetStatement.executeUpdate(sql);
                // Log update count
                if (updateCount != -1) {
                    String callerInfo = getCallerInfo();
                    UniversalLogger.getLogger(callerInfo).info("[SQL-ROWS] Updates: " + updateCount);
                }
                return updateCount;
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
        protected String getCallerInfo() {
            StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();

            // 우선순위: Mapper > Repository > Dao > Service > Controller > 기타
            StackTraceElement mapperElement = null;
            StackTraceElement repositoryElement = null;
            StackTraceElement daoElement = null;
            StackTraceElement serviceElement = null;
            StackTraceElement controllerElement = null;
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
                    className.startsWith("jakarta.") ||
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
                    className.startsWith("org.springframework.data") ||  // Spring Data (JPA, Commons 등)
                    className.startsWith("org.springframework.orm") ||  // Spring ORM
                    className.contains("RepositoryComposition") ||  // Spring Data JPA internal
                    className.contains("TaskThread") ||  // Spring async internal
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
                } else if (className.contains("Controller")) {
                    controllerElement = element;
                }
            }

            // 우선순위대로 반환: Mapper > Repository > Dao > Service > Controller > 마지막 유효한 클래스
            StackTraceElement targetElement = null;
            if (mapperElement != null) {
                targetElement = mapperElement;
            } else if (repositoryElement != null) {
                targetElement = repositoryElement;
            } else if (daoElement != null) {
                targetElement = daoElement;
            } else if (serviceElement != null) {
                targetElement = serviceElement;
            } else if (controllerElement != null) {
                targetElement = controllerElement;
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
            long start = System.currentTimeMillis();
            String callerInfo = getCallerInfo();
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
                logAllResults(callerInfo, rowCount, -1, executionTime);
            }
        }

        @Override
        public int executeUpdate() throws SQLException {
            long start = System.currentTimeMillis();
            String callerInfo = getCallerInfo();
            int updateCount = -1;

            try {
                updateCount = targetPreparedStatement.executeUpdate();
                return updateCount;
            } finally {
                long executionTime = System.currentTimeMillis() - start;
                logAllResults(callerInfo, -1, updateCount, executionTime);
            }
        }

        @Override
        public boolean execute() throws SQLException {
            long start = System.currentTimeMillis();
            String callerInfo = getCallerInfo();
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
                logAllResults(callerInfo, rowCount, updateCount, executionTime);
            }
        }

        /**
         * 파라미터 값을 SQL 문자열로 포맷팅
         * 타입에 따라 적절한 형식으로 변환
         */
        private String formatParameter(Object value) {
            if (value == null) {
                return "NULL";
            }

            // String - 작은따옴표로 감싸고 내부 작은따옴표는 이스케이프
            if (value instanceof String) {
                return "'" + ((String) value).replace("'", "''") + "'";
            }

            // Date/Time - 작은따옴표로 감싸기
            if (value instanceof java.sql.Date || value instanceof java.sql.Time ||
                value instanceof java.sql.Timestamp || value instanceof java.util.Date) {
                return "'" + value.toString() + "'";
            }

            // byte[] - BLOB 표시
            if (value instanceof byte[]) {
                return "[BLOB: " + ((byte[]) value).length + " bytes]";
            }

            // Number, Boolean - 그대로 출력
            return value.toString();
        }

        /**
         * SQL의 ? 플레이스홀더를 실제 파라미터 값으로 치환
         */
        private String substituteParameters(String sql, java.util.Map<Integer, Object> params) {
            if (params == null || params.isEmpty()) {
                return sql;
            }

            StringBuilder result = new StringBuilder();
            int currentPos = 0;
            int paramIndex = 1;

            for (int i = 0; i < sql.length(); i++) {
                if (sql.charAt(i) == '?') {
                    // ? 이전까지의 문자열 추가
                    result.append(sql, currentPos, i);

                    // 파라미터 값 치환
                    Object paramValue = params.get(paramIndex);
                    result.append(formatParameter(paramValue));

                    paramIndex++;
                    currentPos = i + 1;
                }
            }

            // 남은 문자열 추가
            if (currentPos < sql.length()) {
                result.append(sql, currentPos, sql.length());
            }

            return result.toString();
        }

        /**
         * SQL 실행 결과를 한 번에 로깅 (멀티스레드 환경에서 로그가 섞이지 않도록)
         */
        private void logAllResults(String callerInfo, int rowCount, int updateCount, long executionTime) {
            StringBuilder sb = new StringBuilder();

            // 설정 확인
            boolean useSubstitution = false;
            try {
                com.universallogger.config.LoggerConfig config = UniversalLogger.getConfig();
                if (config != null && config.getSql() != null) {
                    useSubstitution = config.getSql().isParameterSubstitution();
                }
            } catch (Exception e) {
                // 설정을 가져올 수 없는 경우 기본 동작 (OFF)
            }

            // 1. SQL + Parameters
            String formattedSql = formatSql(sql);
            if (useSubstitution && !parameters.isEmpty()) {
                // ON: 파라미터 치환된 SQL 출력
                String substitutedSql = substituteParameters(formattedSql, parameters);
                sb.append("[SQL] ").append(substitutedSql).append("\n");
                sb.append("[SQL-PARAMS] Original: ").append(parameters).append("\n");
            } else {
                // OFF: 기존 형식 (? + Parameters)
                sb.append("[SQL] ").append(formattedSql);
                if (!parameters.isEmpty()) {
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

            UniversalLogger.getLogger(callerInfo).info(sb.toString());
        }

        /**
         * SQL을 읽기 쉽게 포맷팅
         * - 주요 키워드 앞에 줄바꿈 추가
         * - MyBatis 스타일과 동일하게 출력
         */
        private String formatSql(String sql) {
            if (sql == null || sql.trim().isEmpty()) {
                return sql;
            }

            // 이미 포맷팅된 SQL인지 확인 (줄바꿈이 많으면 이미 포맷팅됨)
            if (sql.split("\n").length > 3) {
                return sql;  // 이미 포맷팅되어 있으면 그대로 반환
            }

            String formatted = sql
                // SELECT 구문
                .replaceAll("(?i)\\s+(SELECT)\\s+", "\n$1 ")
                .replaceAll("(?i)\\s+(FROM)\\s+", "\n       $1 ")
                .replaceAll("(?i)\\s+(WHERE)\\s+", "\n       $1 ")
                .replaceAll("(?i)\\s+(AND)\\s+", "\n       $1 ")
                .replaceAll("(?i)\\s+(OR)\\s+", "\n       $1 ")
                .replaceAll("(?i)\\s+(ORDER BY)\\s+", "\n       $1 ")
                .replaceAll("(?i)\\s+(GROUP BY)\\s+", "\n       $1 ")
                .replaceAll("(?i)\\s+(HAVING)\\s+", "\n       $1 ")
                .replaceAll("(?i)\\s+(LIMIT)\\s+", "\n       $1 ")
                .replaceAll("(?i)\\s+(OFFSET)\\s+", "\n       $1 ")
                // JOIN 구문
                .replaceAll("(?i)\\s+(LEFT JOIN|RIGHT JOIN|INNER JOIN|OUTER JOIN|CROSS JOIN|JOIN)\\s+", "\n       $1 ")
                .replaceAll("(?i)\\s+(ON)\\s+", " $1 ")
                // INSERT 구문
                .replaceAll("(?i)\\s+(INSERT INTO)\\s+", "\n$1 ")
                .replaceAll("(?i)\\s+(VALUES)\\s+", "\n       $1 ")
                // UPDATE 구문
                .replaceAll("(?i)\\s+(UPDATE)\\s+", "\n$1 ")
                .replaceAll("(?i)\\s+(SET)\\s+", "\n       $1 ")
                // DELETE 구문
                .replaceAll("(?i)\\s+(DELETE FROM)\\s+", "\n$1 ")
                .replaceAll("(?i)\\s+(DELETE)\\s+", "\n$1 ")
                .trim();

            return formatted;
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

    /**
     * ResultSet wrapper for row counting (like MyBatis ResultSetLogger)
     * Counts rows as they are consumed and logs the total when finished
     */
    private static class RowCountingResultSet implements ResultSet {
        private final ResultSet targetResultSet;
        private final String callerInfo;
        private int rows = 0;
        private int currentRow = 0;
        private boolean countLogged = false;

        public RowCountingResultSet(ResultSet targetResultSet, String callerInfo) {
            this.targetResultSet = targetResultSet;
            this.callerInfo = callerInfo;
        }

        @Override
        public boolean next() throws SQLException {
            boolean hasNext = targetResultSet.next();
            if (hasNext) {
                currentRow++;
                if (currentRow > rows) {
                    rows = currentRow;
                }
            } else {
                // Log total when no more rows (like MyBatis ResultSetLogger)
                logRowCount();
            }
            return hasNext;
        }

        // Track row access through getter methods (MyBatis calls these)
        private void trackRowAccess() {
            if (currentRow > rows) {
                rows = currentRow;
            }
        }

        @Override
        public <T> T unwrap(Class<T> iface) throws SQLException {
            if (iface.isInstance(this)) {
                return iface.cast(this);
            }
            // Log row count before unwrapping (MyBatis might unwrap our ResultSet)
            logRowCount();
            return targetResultSet.unwrap(iface);
        }

        @Override
        public boolean isWrapperFor(Class<?> iface) throws SQLException {
            return iface.isInstance(this) || targetResultSet.isWrapperFor(iface);
        }

        @Override
        public void close() throws SQLException {
            logRowCount();
            targetResultSet.close();
        }

        private void logRowCount() {
            if (!countLogged && rows > 0) {
                countLogged = true;
                // Log in Universal Logger style
                UniversalLogger.getLogger(callerInfo).info("[SQL-ROWS] Total: " + rows);
            }
        }

        // Delegate all other methods to targetResultSet
        @Override public boolean wasNull() throws SQLException { return targetResultSet.wasNull(); }
        @Override public String getString(int columnIndex) throws SQLException { trackRowAccess(); return targetResultSet.getString(columnIndex); }
        @Override public boolean getBoolean(int columnIndex) throws SQLException { trackRowAccess(); return targetResultSet.getBoolean(columnIndex); }
        @Override public byte getByte(int columnIndex) throws SQLException { trackRowAccess(); return targetResultSet.getByte(columnIndex); }
        @Override public short getShort(int columnIndex) throws SQLException { trackRowAccess(); return targetResultSet.getShort(columnIndex); }
        @Override public int getInt(int columnIndex) throws SQLException { trackRowAccess(); return targetResultSet.getInt(columnIndex); }
        @Override public long getLong(int columnIndex) throws SQLException { trackRowAccess(); return targetResultSet.getLong(columnIndex); }
        @Override public float getFloat(int columnIndex) throws SQLException { trackRowAccess(); return targetResultSet.getFloat(columnIndex); }
        @Override public double getDouble(int columnIndex) throws SQLException { trackRowAccess(); return targetResultSet.getDouble(columnIndex); }
        @Override public java.math.BigDecimal getBigDecimal(int columnIndex, int scale) throws SQLException { trackRowAccess(); return targetResultSet.getBigDecimal(columnIndex, scale); }
        @Override public byte[] getBytes(int columnIndex) throws SQLException { trackRowAccess(); return targetResultSet.getBytes(columnIndex); }
        @Override public Date getDate(int columnIndex) throws SQLException { trackRowAccess(); return targetResultSet.getDate(columnIndex); }
        @Override public Time getTime(int columnIndex) throws SQLException { trackRowAccess(); return targetResultSet.getTime(columnIndex); }
        @Override public Timestamp getTimestamp(int columnIndex) throws SQLException { trackRowAccess(); return targetResultSet.getTimestamp(columnIndex); }
        @Override public java.io.InputStream getAsciiStream(int columnIndex) throws SQLException { return targetResultSet.getAsciiStream(columnIndex); }
        @Override public java.io.InputStream getUnicodeStream(int columnIndex) throws SQLException { return targetResultSet.getUnicodeStream(columnIndex); }
        @Override public java.io.InputStream getBinaryStream(int columnIndex) throws SQLException { return targetResultSet.getBinaryStream(columnIndex); }
        @Override public String getString(String columnLabel) throws SQLException { trackRowAccess(); return targetResultSet.getString(columnLabel); }
        @Override public boolean getBoolean(String columnLabel) throws SQLException { trackRowAccess(); return targetResultSet.getBoolean(columnLabel); }
        @Override public byte getByte(String columnLabel) throws SQLException { trackRowAccess(); return targetResultSet.getByte(columnLabel); }
        @Override public short getShort(String columnLabel) throws SQLException { trackRowAccess(); return targetResultSet.getShort(columnLabel); }
        @Override public int getInt(String columnLabel) throws SQLException { trackRowAccess(); return targetResultSet.getInt(columnLabel); }
        @Override public long getLong(String columnLabel) throws SQLException { trackRowAccess(); return targetResultSet.getLong(columnLabel); }
        @Override public float getFloat(String columnLabel) throws SQLException { trackRowAccess(); return targetResultSet.getFloat(columnLabel); }
        @Override public double getDouble(String columnLabel) throws SQLException { trackRowAccess(); return targetResultSet.getDouble(columnLabel); }
        @Override public java.math.BigDecimal getBigDecimal(String columnLabel, int scale) throws SQLException { trackRowAccess(); return targetResultSet.getBigDecimal(columnLabel, scale); }
        @Override public byte[] getBytes(String columnLabel) throws SQLException { trackRowAccess(); return targetResultSet.getBytes(columnLabel); }
        @Override public Date getDate(String columnLabel) throws SQLException { trackRowAccess(); return targetResultSet.getDate(columnLabel); }
        @Override public Time getTime(String columnLabel) throws SQLException { trackRowAccess(); return targetResultSet.getTime(columnLabel); }
        @Override public Timestamp getTimestamp(String columnLabel) throws SQLException { trackRowAccess(); return targetResultSet.getTimestamp(columnLabel); }
        @Override public java.io.InputStream getAsciiStream(String columnLabel) throws SQLException { return targetResultSet.getAsciiStream(columnLabel); }
        @Override public java.io.InputStream getUnicodeStream(String columnLabel) throws SQLException { return targetResultSet.getUnicodeStream(columnLabel); }
        @Override public java.io.InputStream getBinaryStream(String columnLabel) throws SQLException { return targetResultSet.getBinaryStream(columnLabel); }
        @Override public SQLWarning getWarnings() throws SQLException { return targetResultSet.getWarnings(); }
        @Override public void clearWarnings() throws SQLException { targetResultSet.clearWarnings(); }
        @Override public String getCursorName() throws SQLException { return targetResultSet.getCursorName(); }
        @Override public ResultSetMetaData getMetaData() throws SQLException { return targetResultSet.getMetaData(); }
        @Override public Object getObject(int columnIndex) throws SQLException { trackRowAccess(); return targetResultSet.getObject(columnIndex); }
        @Override public Object getObject(String columnLabel) throws SQLException { trackRowAccess(); return targetResultSet.getObject(columnLabel); }
        @Override public int findColumn(String columnLabel) throws SQLException { return targetResultSet.findColumn(columnLabel); }
        @Override public java.io.Reader getCharacterStream(int columnIndex) throws SQLException { return targetResultSet.getCharacterStream(columnIndex); }
        @Override public java.io.Reader getCharacterStream(String columnLabel) throws SQLException { return targetResultSet.getCharacterStream(columnLabel); }
        @Override public java.math.BigDecimal getBigDecimal(int columnIndex) throws SQLException { return targetResultSet.getBigDecimal(columnIndex); }
        @Override public java.math.BigDecimal getBigDecimal(String columnLabel) throws SQLException { return targetResultSet.getBigDecimal(columnLabel); }
        @Override public boolean isBeforeFirst() throws SQLException { return targetResultSet.isBeforeFirst(); }
        @Override public boolean isAfterLast() throws SQLException { return targetResultSet.isAfterLast(); }
        @Override public boolean isFirst() throws SQLException { return targetResultSet.isFirst(); }
        @Override public boolean isLast() throws SQLException { return targetResultSet.isLast(); }
        @Override public void beforeFirst() throws SQLException { targetResultSet.beforeFirst(); }
        @Override public void afterLast() throws SQLException { targetResultSet.afterLast(); }
        @Override public boolean first() throws SQLException { return targetResultSet.first(); }
        @Override public boolean last() throws SQLException { return targetResultSet.last(); }
        @Override public int getRow() throws SQLException { return targetResultSet.getRow(); }
        @Override public boolean absolute(int row) throws SQLException { return targetResultSet.absolute(row); }
        @Override public boolean relative(int rows) throws SQLException { return targetResultSet.relative(rows); }
        @Override public boolean previous() throws SQLException { return targetResultSet.previous(); }
        @Override public void setFetchDirection(int direction) throws SQLException { targetResultSet.setFetchDirection(direction); }
        @Override public int getFetchDirection() throws SQLException { return targetResultSet.getFetchDirection(); }
        @Override public void setFetchSize(int rows) throws SQLException { targetResultSet.setFetchSize(rows); }
        @Override public int getFetchSize() throws SQLException { return targetResultSet.getFetchSize(); }
        @Override public int getType() throws SQLException { return targetResultSet.getType(); }
        @Override public int getConcurrency() throws SQLException { return targetResultSet.getConcurrency(); }
        @Override public boolean rowUpdated() throws SQLException { return targetResultSet.rowUpdated(); }
        @Override public boolean rowInserted() throws SQLException { return targetResultSet.rowInserted(); }
        @Override public boolean rowDeleted() throws SQLException { return targetResultSet.rowDeleted(); }
        @Override public void updateNull(int columnIndex) throws SQLException { targetResultSet.updateNull(columnIndex); }
        @Override public void updateBoolean(int columnIndex, boolean x) throws SQLException { targetResultSet.updateBoolean(columnIndex, x); }
        @Override public void updateByte(int columnIndex, byte x) throws SQLException { targetResultSet.updateByte(columnIndex, x); }
        @Override public void updateShort(int columnIndex, short x) throws SQLException { targetResultSet.updateShort(columnIndex, x); }
        @Override public void updateInt(int columnIndex, int x) throws SQLException { targetResultSet.updateInt(columnIndex, x); }
        @Override public void updateLong(int columnIndex, long x) throws SQLException { targetResultSet.updateLong(columnIndex, x); }
        @Override public void updateFloat(int columnIndex, float x) throws SQLException { targetResultSet.updateFloat(columnIndex, x); }
        @Override public void updateDouble(int columnIndex, double x) throws SQLException { targetResultSet.updateDouble(columnIndex, x); }
        @Override public void updateBigDecimal(int columnIndex, java.math.BigDecimal x) throws SQLException { targetResultSet.updateBigDecimal(columnIndex, x); }
        @Override public void updateString(int columnIndex, String x) throws SQLException { targetResultSet.updateString(columnIndex, x); }
        @Override public void updateBytes(int columnIndex, byte[] x) throws SQLException { targetResultSet.updateBytes(columnIndex, x); }
        @Override public void updateDate(int columnIndex, Date x) throws SQLException { targetResultSet.updateDate(columnIndex, x); }
        @Override public void updateTime(int columnIndex, Time x) throws SQLException { targetResultSet.updateTime(columnIndex, x); }
        @Override public void updateTimestamp(int columnIndex, Timestamp x) throws SQLException { targetResultSet.updateTimestamp(columnIndex, x); }
        @Override public void updateAsciiStream(int columnIndex, java.io.InputStream x, int length) throws SQLException { targetResultSet.updateAsciiStream(columnIndex, x, length); }
        @Override public void updateBinaryStream(int columnIndex, java.io.InputStream x, int length) throws SQLException { targetResultSet.updateBinaryStream(columnIndex, x, length); }
        @Override public void updateCharacterStream(int columnIndex, java.io.Reader x, int length) throws SQLException { targetResultSet.updateCharacterStream(columnIndex, x, length); }
        @Override public void updateObject(int columnIndex, Object x, int scaleOrLength) throws SQLException { targetResultSet.updateObject(columnIndex, x, scaleOrLength); }
        @Override public void updateObject(int columnIndex, Object x) throws SQLException { targetResultSet.updateObject(columnIndex, x); }
        @Override public void updateNull(String columnLabel) throws SQLException { targetResultSet.updateNull(columnLabel); }
        @Override public void updateBoolean(String columnLabel, boolean x) throws SQLException { targetResultSet.updateBoolean(columnLabel, x); }
        @Override public void updateByte(String columnLabel, byte x) throws SQLException { targetResultSet.updateByte(columnLabel, x); }
        @Override public void updateShort(String columnLabel, short x) throws SQLException { targetResultSet.updateShort(columnLabel, x); }
        @Override public void updateInt(String columnLabel, int x) throws SQLException { targetResultSet.updateInt(columnLabel, x); }
        @Override public void updateLong(String columnLabel, long x) throws SQLException { targetResultSet.updateLong(columnLabel, x); }
        @Override public void updateFloat(String columnLabel, float x) throws SQLException { targetResultSet.updateFloat(columnLabel, x); }
        @Override public void updateDouble(String columnLabel, double x) throws SQLException { targetResultSet.updateDouble(columnLabel, x); }
        @Override public void updateBigDecimal(String columnLabel, java.math.BigDecimal x) throws SQLException { targetResultSet.updateBigDecimal(columnLabel, x); }
        @Override public void updateString(String columnLabel, String x) throws SQLException { targetResultSet.updateString(columnLabel, x); }
        @Override public void updateBytes(String columnLabel, byte[] x) throws SQLException { targetResultSet.updateBytes(columnLabel, x); }
        @Override public void updateDate(String columnLabel, Date x) throws SQLException { targetResultSet.updateDate(columnLabel, x); }
        @Override public void updateTime(String columnLabel, Time x) throws SQLException { targetResultSet.updateTime(columnLabel, x); }
        @Override public void updateTimestamp(String columnLabel, Timestamp x) throws SQLException { targetResultSet.updateTimestamp(columnLabel, x); }
        @Override public void updateAsciiStream(String columnLabel, java.io.InputStream x, int length) throws SQLException { targetResultSet.updateAsciiStream(columnLabel, x, length); }
        @Override public void updateBinaryStream(String columnLabel, java.io.InputStream x, int length) throws SQLException { targetResultSet.updateBinaryStream(columnLabel, x, length); }
        @Override public void updateCharacterStream(String columnLabel, java.io.Reader reader, int length) throws SQLException { targetResultSet.updateCharacterStream(columnLabel, reader, length); }
        @Override public void updateObject(String columnLabel, Object x, int scaleOrLength) throws SQLException { targetResultSet.updateObject(columnLabel, x, scaleOrLength); }
        @Override public void updateObject(String columnLabel, Object x) throws SQLException { targetResultSet.updateObject(columnLabel, x); }
        @Override public void insertRow() throws SQLException { targetResultSet.insertRow(); }
        @Override public void updateRow() throws SQLException { targetResultSet.updateRow(); }
        @Override public void deleteRow() throws SQLException { targetResultSet.deleteRow(); }
        @Override public void refreshRow() throws SQLException { targetResultSet.refreshRow(); }
        @Override public void cancelRowUpdates() throws SQLException { targetResultSet.cancelRowUpdates(); }
        @Override public void moveToInsertRow() throws SQLException { targetResultSet.moveToInsertRow(); }
        @Override public void moveToCurrentRow() throws SQLException { targetResultSet.moveToCurrentRow(); }
        @Override public Statement getStatement() throws SQLException { return targetResultSet.getStatement(); }
        @Override public Object getObject(int columnIndex, java.util.Map<String, Class<?>> map) throws SQLException { return targetResultSet.getObject(columnIndex, map); }
        @Override public Ref getRef(int columnIndex) throws SQLException { return targetResultSet.getRef(columnIndex); }
        @Override public Blob getBlob(int columnIndex) throws SQLException { return targetResultSet.getBlob(columnIndex); }
        @Override public Clob getClob(int columnIndex) throws SQLException { return targetResultSet.getClob(columnIndex); }
        @Override public Array getArray(int columnIndex) throws SQLException { return targetResultSet.getArray(columnIndex); }
        @Override public Object getObject(String columnLabel, java.util.Map<String, Class<?>> map) throws SQLException { return targetResultSet.getObject(columnLabel, map); }
        @Override public Ref getRef(String columnLabel) throws SQLException { return targetResultSet.getRef(columnLabel); }
        @Override public Blob getBlob(String columnLabel) throws SQLException { return targetResultSet.getBlob(columnLabel); }
        @Override public Clob getClob(String columnLabel) throws SQLException { return targetResultSet.getClob(columnLabel); }
        @Override public Array getArray(String columnLabel) throws SQLException { return targetResultSet.getArray(columnLabel); }
        @Override public Date getDate(int columnIndex, java.util.Calendar cal) throws SQLException { return targetResultSet.getDate(columnIndex, cal); }
        @Override public Date getDate(String columnLabel, java.util.Calendar cal) throws SQLException { return targetResultSet.getDate(columnLabel, cal); }
        @Override public Time getTime(int columnIndex, java.util.Calendar cal) throws SQLException { return targetResultSet.getTime(columnIndex, cal); }
        @Override public Time getTime(String columnLabel, java.util.Calendar cal) throws SQLException { return targetResultSet.getTime(columnLabel, cal); }
        @Override public Timestamp getTimestamp(int columnIndex, java.util.Calendar cal) throws SQLException { return targetResultSet.getTimestamp(columnIndex, cal); }
        @Override public Timestamp getTimestamp(String columnLabel, java.util.Calendar cal) throws SQLException { return targetResultSet.getTimestamp(columnLabel, cal); }
        @Override public java.net.URL getURL(int columnIndex) throws SQLException { return targetResultSet.getURL(columnIndex); }
        @Override public java.net.URL getURL(String columnLabel) throws SQLException { return targetResultSet.getURL(columnLabel); }
        @Override public void updateRef(int columnIndex, Ref x) throws SQLException { targetResultSet.updateRef(columnIndex, x); }
        @Override public void updateRef(String columnLabel, Ref x) throws SQLException { targetResultSet.updateRef(columnLabel, x); }
        @Override public void updateBlob(int columnIndex, Blob x) throws SQLException { targetResultSet.updateBlob(columnIndex, x); }
        @Override public void updateBlob(String columnLabel, Blob x) throws SQLException { targetResultSet.updateBlob(columnLabel, x); }
        @Override public void updateClob(int columnIndex, Clob x) throws SQLException { targetResultSet.updateClob(columnIndex, x); }
        @Override public void updateClob(String columnLabel, Clob x) throws SQLException { targetResultSet.updateClob(columnLabel, x); }
        @Override public void updateArray(int columnIndex, Array x) throws SQLException { targetResultSet.updateArray(columnIndex, x); }
        @Override public void updateArray(String columnLabel, Array x) throws SQLException { targetResultSet.updateArray(columnLabel, x); }
        @Override public RowId getRowId(int columnIndex) throws SQLException { return targetResultSet.getRowId(columnIndex); }
        @Override public RowId getRowId(String columnLabel) throws SQLException { return targetResultSet.getRowId(columnLabel); }
        @Override public void updateRowId(int columnIndex, RowId x) throws SQLException { targetResultSet.updateRowId(columnIndex, x); }
        @Override public void updateRowId(String columnLabel, RowId x) throws SQLException { targetResultSet.updateRowId(columnLabel, x); }
        @Override public int getHoldability() throws SQLException { return targetResultSet.getHoldability(); }
        @Override public boolean isClosed() throws SQLException { return targetResultSet.isClosed(); }
        @Override public void updateNString(int columnIndex, String nString) throws SQLException { targetResultSet.updateNString(columnIndex, nString); }
        @Override public void updateNString(String columnLabel, String nString) throws SQLException { targetResultSet.updateNString(columnLabel, nString); }
        @Override public void updateNClob(int columnIndex, NClob nClob) throws SQLException { targetResultSet.updateNClob(columnIndex, nClob); }
        @Override public void updateNClob(String columnLabel, NClob nClob) throws SQLException { targetResultSet.updateNClob(columnLabel, nClob); }
        @Override public NClob getNClob(int columnIndex) throws SQLException { return targetResultSet.getNClob(columnIndex); }
        @Override public NClob getNClob(String columnLabel) throws SQLException { return targetResultSet.getNClob(columnLabel); }
        @Override public SQLXML getSQLXML(int columnIndex) throws SQLException { return targetResultSet.getSQLXML(columnIndex); }
        @Override public SQLXML getSQLXML(String columnLabel) throws SQLException { return targetResultSet.getSQLXML(columnLabel); }
        @Override public void updateSQLXML(int columnIndex, SQLXML xmlObject) throws SQLException { targetResultSet.updateSQLXML(columnIndex, xmlObject); }
        @Override public void updateSQLXML(String columnLabel, SQLXML xmlObject) throws SQLException { targetResultSet.updateSQLXML(columnLabel, xmlObject); }
        @Override public String getNString(int columnIndex) throws SQLException { return targetResultSet.getNString(columnIndex); }
        @Override public String getNString(String columnLabel) throws SQLException { return targetResultSet.getNString(columnLabel); }
        @Override public java.io.Reader getNCharacterStream(int columnIndex) throws SQLException { return targetResultSet.getNCharacterStream(columnIndex); }
        @Override public java.io.Reader getNCharacterStream(String columnLabel) throws SQLException { return targetResultSet.getNCharacterStream(columnLabel); }
        @Override public void updateNCharacterStream(int columnIndex, java.io.Reader x, long length) throws SQLException { targetResultSet.updateNCharacterStream(columnIndex, x, length); }
        @Override public void updateNCharacterStream(String columnLabel, java.io.Reader reader, long length) throws SQLException { targetResultSet.updateNCharacterStream(columnLabel, reader, length); }
        @Override public void updateAsciiStream(int columnIndex, java.io.InputStream x, long length) throws SQLException { targetResultSet.updateAsciiStream(columnIndex, x, length); }
        @Override public void updateBinaryStream(int columnIndex, java.io.InputStream x, long length) throws SQLException { targetResultSet.updateBinaryStream(columnIndex, x, length); }
        @Override public void updateCharacterStream(int columnIndex, java.io.Reader x, long length) throws SQLException { targetResultSet.updateCharacterStream(columnIndex, x, length); }
        @Override public void updateAsciiStream(String columnLabel, java.io.InputStream x, long length) throws SQLException { targetResultSet.updateAsciiStream(columnLabel, x, length); }
        @Override public void updateBinaryStream(String columnLabel, java.io.InputStream x, long length) throws SQLException { targetResultSet.updateBinaryStream(columnLabel, x, length); }
        @Override public void updateCharacterStream(String columnLabel, java.io.Reader reader, long length) throws SQLException { targetResultSet.updateCharacterStream(columnLabel, reader, length); }
        @Override public void updateBlob(int columnIndex, java.io.InputStream inputStream, long length) throws SQLException { targetResultSet.updateBlob(columnIndex, inputStream, length); }
        @Override public void updateBlob(String columnLabel, java.io.InputStream inputStream, long length) throws SQLException { targetResultSet.updateBlob(columnLabel, inputStream, length); }
        @Override public void updateClob(int columnIndex, java.io.Reader reader, long length) throws SQLException { targetResultSet.updateClob(columnIndex, reader, length); }
        @Override public void updateClob(String columnLabel, java.io.Reader reader, long length) throws SQLException { targetResultSet.updateClob(columnLabel, reader, length); }
        @Override public void updateNClob(int columnIndex, java.io.Reader reader, long length) throws SQLException { targetResultSet.updateNClob(columnIndex, reader, length); }
        @Override public void updateNClob(String columnLabel, java.io.Reader reader, long length) throws SQLException { targetResultSet.updateNClob(columnLabel, reader, length); }
        @Override public void updateNCharacterStream(int columnIndex, java.io.Reader x) throws SQLException { targetResultSet.updateNCharacterStream(columnIndex, x); }
        @Override public void updateNCharacterStream(String columnLabel, java.io.Reader reader) throws SQLException { targetResultSet.updateNCharacterStream(columnLabel, reader); }
        @Override public void updateAsciiStream(int columnIndex, java.io.InputStream x) throws SQLException { targetResultSet.updateAsciiStream(columnIndex, x); }
        @Override public void updateBinaryStream(int columnIndex, java.io.InputStream x) throws SQLException { targetResultSet.updateBinaryStream(columnIndex, x); }
        @Override public void updateCharacterStream(int columnIndex, java.io.Reader x) throws SQLException { targetResultSet.updateCharacterStream(columnIndex, x); }
        @Override public void updateAsciiStream(String columnLabel, java.io.InputStream x) throws SQLException { targetResultSet.updateAsciiStream(columnLabel, x); }
        @Override public void updateBinaryStream(String columnLabel, java.io.InputStream x) throws SQLException { targetResultSet.updateBinaryStream(columnLabel, x); }
        @Override public void updateCharacterStream(String columnLabel, java.io.Reader reader) throws SQLException { targetResultSet.updateCharacterStream(columnLabel, reader); }
        @Override public void updateBlob(int columnIndex, java.io.InputStream inputStream) throws SQLException { targetResultSet.updateBlob(columnIndex, inputStream); }
        @Override public void updateBlob(String columnLabel, java.io.InputStream inputStream) throws SQLException { targetResultSet.updateBlob(columnLabel, inputStream); }
        @Override public void updateClob(int columnIndex, java.io.Reader reader) throws SQLException { targetResultSet.updateClob(columnIndex, reader); }
        @Override public void updateClob(String columnLabel, java.io.Reader reader) throws SQLException { targetResultSet.updateClob(columnLabel, reader); }
        @Override public void updateNClob(int columnIndex, java.io.Reader reader) throws SQLException { targetResultSet.updateNClob(columnIndex, reader); }
        @Override public void updateNClob(String columnLabel, java.io.Reader reader) throws SQLException { targetResultSet.updateNClob(columnLabel, reader); }
        @Override public <T> T getObject(int columnIndex, Class<T> type) throws SQLException { return targetResultSet.getObject(columnIndex, type); }
        @Override public <T> T getObject(String columnLabel, Class<T> type) throws SQLException { return targetResultSet.getObject(columnLabel, type); }
    }
}
