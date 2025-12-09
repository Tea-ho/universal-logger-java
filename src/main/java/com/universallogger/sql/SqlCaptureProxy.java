package com.universallogger.sql;

import com.universallogger.LoggerInstance;

import javax.sql.DataSource;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.logging.Logger;

/**
 * SQL 자동 캡처를 위한 DataSource 프록시
 * 모든 데이터베이스 지원 (MySQL, Oracle, MSSQL, MongoDB JDBC 등)
 *
 * Static API와 인스턴스 기반 방식 모두 지원:
 * - Static API: new SqlCaptureProxy(dataSource) - UniversalLogger 싱글톤 사용
 * - Instance-based: new SqlCaptureProxy(dataSource, loggerInstance) - 주입된 인스턴스 사용
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
    private final LoggerInstance loggerInstance;

    /**
     * Static API 생성자 (하위 호환성)
     */
    public SqlCaptureProxy(DataSource targetDataSource) {
        this(targetDataSource, true, null);
    }

    /**
     * Static API + 활성화 플래그 생성자 (하위 호환성)
     */
    public SqlCaptureProxy(DataSource targetDataSource, boolean enabled) {
        this(targetDataSource, enabled, null);
    }

    /**
     * 인스턴스 기반 LoggerInstance 생성자 (권장)
     *
     * @param targetDataSource 래핑할 실제 DataSource
     * @param loggerInstance 로깅에 사용할 LoggerInstance
     */
    public SqlCaptureProxy(DataSource targetDataSource, LoggerInstance loggerInstance) {
        this(targetDataSource, true, loggerInstance);
    }

    /**
     * 전체 생성자 (내부 사용)
     */
    private SqlCaptureProxy(DataSource targetDataSource, boolean enabled, LoggerInstance loggerInstance) {
        this.targetDataSource = targetDataSource;
        this.enabled = enabled;
        this.loggerInstance = loggerInstance;
    }

    @Override
    public Connection getConnection() throws SQLException {
        Connection connection = targetDataSource.getConnection();
        return enabled ? new SqlCaptureConnection(connection, loggerInstance) : connection;
    }

    @Override
    public Connection getConnection(String username, String password) throws SQLException {
        Connection connection = targetDataSource.getConnection(username, password);
        return enabled ? new SqlCaptureConnection(connection, loggerInstance) : connection;
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
