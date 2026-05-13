package io.universallogger.sql;

import io.universallogger.config.LoggerConfig;

import javax.sql.DataSource;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.logging.Logger;

/**
 * SQL 자동 캡처를 위한 DataSource 프록시
 *
 * 원본 DataSource를 래핑하여 모든 SQL 실행을 자동으로 캡처하고 로깅합니다.
 * 모든 JDBC 호환 데이터베이스를 지원합니다 (MySQL, Oracle, MSSQL, PostgreSQL, MongoDB JDBC 등).
 *
 * <p>동작 원리:</p>
 * <ol>
 *   <li>getConnection() 호출 시 {@link SqlCaptureConnection}으로 래핑된 Connection 반환</li>
 *   <li>Connection에서 Statement/PreparedStatement/CallableStatement 생성 시 각각의 캡처 래퍼로 래핑</li>
 *   <li>SQL 실행 시 자동으로 쿼리, 파라미터, 실행 시간, 결과 행 수 등을 로깅</li>
 * </ol>
 *
 * <p>사용 예시:</p>
 * <pre>{@code
 * // 기존 DataSource 래핑
 * DataSource originalDataSource = ...;
 * DataSource proxiedDataSource = new SqlCaptureProxy(originalDataSource, loggerConfig);
 *
 * // Spring에서 사용
 * @Bean
 * public DataSource dataSource(DataSource originalDataSource, LoggerConfig config) {
 *     return new SqlCaptureProxy(originalDataSource, config);
 * }
 * }</pre>
 *
 * @see SqlCaptureConnection
 * @see SqlCaptureStatement
 * @see SqlCapturePreparedStatement
 * @see SqlCaptureCallableStatement
 */
public class SqlCaptureProxy implements DataSource {

    private final DataSource targetDataSource;
    private final boolean enabled;
    private final LoggerConfig config;

    /**
     * 기본 생성자 - 기본 설정 사용
     */
    public SqlCaptureProxy(DataSource targetDataSource) {
        this(targetDataSource, true, null);
    }

    /**
     * 활성화 플래그 생성자
     */
    public SqlCaptureProxy(DataSource targetDataSource, boolean enabled) {
        this(targetDataSource, enabled, null);
    }

    /**
     * LoggerConfig 생성자 (권장)
     *
     * @param targetDataSource 래핑할 실제 DataSource
     * @param config 로깅 설정
     */
    public SqlCaptureProxy(DataSource targetDataSource, LoggerConfig config) {
        this(targetDataSource, true, config);
    }

    /**
     * 전체 생성자 (내부 사용)
     */
    private SqlCaptureProxy(DataSource targetDataSource, boolean enabled, LoggerConfig config) {
        this.targetDataSource = targetDataSource;
        this.enabled = enabled;
        this.config = config;
    }

    @Override
    public Connection getConnection() throws SQLException {
        Connection connection = targetDataSource.getConnection();
        return enabled ? new SqlCaptureConnection(connection, config) : connection;
    }

    @Override
    public Connection getConnection(String username, String password) throws SQLException {
        Connection connection = targetDataSource.getConnection(username, password);
        return enabled ? new SqlCaptureConnection(connection, config) : connection;
    }

    // 위임 메서드들
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
}
