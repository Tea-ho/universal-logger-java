package com.universallogger.sql;

import com.universallogger.LoggerInstance;

import javax.sql.DataSource;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.logging.Logger;

/**
 * DataSource Proxy for automatic SQL capture
 * Works with any database (MySQL, Oracle, MSSQL, MongoDB JDBC, etc.)
 *
 * Supports both static API and instance-based approach:
 * - Static API: new SqlCaptureProxy(dataSource) - uses UniversalLogger singleton
 * - Instance-based: new SqlCaptureProxy(dataSource, loggerInstance) - uses injected instance
 *
 * This class has been refactored for better separation of concerns:
 * - SqlFormatter: SQL formatting logic
 * - ParameterSubstitutor: Parameter substitution logic
 * - SqlLogger: SQL logging logic
 * - SqlCaptureConnection, SqlCaptureStatement, SqlCapturePreparedStatement: Separate wrapper classes
 */
public class SqlCaptureProxy implements DataSource {

    private final DataSource targetDataSource;
    private final boolean enabled;
    private final LoggerInstance loggerInstance;

    /**
     * Constructor with static API (backward compatibility)
     */
    public SqlCaptureProxy(DataSource targetDataSource) {
        this(targetDataSource, true, null);
    }

    /**
     * Constructor with static API and enabled flag (backward compatibility)
     */
    public SqlCaptureProxy(DataSource targetDataSource, boolean enabled) {
        this(targetDataSource, enabled, null);
    }

    /**
     * Constructor with instance-based LoggerInstance (recommended)
     *
     * @param targetDataSource The actual DataSource to wrap
     * @param loggerInstance The LoggerInstance to use for logging
     */
    public SqlCaptureProxy(DataSource targetDataSource, LoggerInstance loggerInstance) {
        this(targetDataSource, true, loggerInstance);
    }

    /**
     * Full constructor (internal use)
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
}
