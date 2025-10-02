package com.universallogger.appenders.database;

import com.universallogger.config.LoggerConfig.DatabaseConfig;

/**
 * Microsoft SQL Server 데이터베이스 연결 구현체
 */
public class MSSQLConnection extends AbstractDatabaseConnection {

    public MSSQLConnection(DatabaseConfig config) {
        super(config);
    }

    @Override
    protected String buildJdbcUrl() {
        String host = config.getHost();
        String database = config.getDatabase();

        if (host == null || host.trim().isEmpty()) {
            throw new IllegalArgumentException("MSSQL host is not configured");
        }
        if (database == null || database.trim().isEmpty()) {
            throw new IllegalArgumentException("MSSQL database name is not configured");
        }

        // MSSQL JDBC URL: jdbc:sqlserver://host:port;databaseName=database
        int port = config.getPort() > 0 ? config.getPort() : 1433;
        return String.format(
            "jdbc:sqlserver://%s:%d;databaseName=%s;encrypt=false;trustServerCertificate=true",
            host, port, database
        );
    }

    @Override
    protected String getCreateTableSQL() {
        String tableName = getTableName();
        return String.format(
            "IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = '%s') " +
            "BEGIN " +
            "CREATE TABLE %s (" +
            "id BIGINT IDENTITY(1,1) PRIMARY KEY, " +
            "log_timestamp DATETIME2 DEFAULT GETDATE(), " +
            "log_level VARCHAR(10) NOT NULL, " +
            "logger_name VARCHAR(255), " +
            "thread_name VARCHAR(255), " +
            "message NVARCHAR(MAX), " +
            "exception NVARCHAR(MAX)); " +
            "CREATE INDEX idx_timestamp ON %s(log_timestamp); " +
            "CREATE INDEX idx_level ON %s(log_level); " +
            "END",
            tableName, tableName, tableName, tableName
        );
    }

    @Override
    public String getDatabaseType() {
        return "mssql";
    }
}