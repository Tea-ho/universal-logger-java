package com.universallogger.sql;

import com.universallogger.config.LoggerConfig;

import javax.sql.DataSource;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.logging.Logger;

/**
 * SQL 자동 캡처를 위한 DataSource 프록시
 * 모든 데이터베이스 지원 (MySQL, Oracle, MSSQL, MongoDB JDBC 등)
 *
 * 사용법:
 * - new SqlCaptureProxy(dataSource) - 기본 설정 사용
 * - new SqlCaptureProxy(dataSource, config) - 커스텀 설정 사용
 *
 * 관심사 분리를 위해 리팩토링됨:
 * - SqlFormatter: SQL 포맷팅 로직
 * - ParameterSubstitutor: 파라미터 치환 로직
 * - SqlLogger: SQL 로깅 로직
 * - SqlCaptureConnection, SqlCaptureStatement, SqlCapturePreparedStatement: 별도 래퍼 클래스들
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
