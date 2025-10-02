package com.universallogger.appenders.database;

import com.universallogger.config.LoggerConfig.DatabaseConfig;

/**
 * PostgreSQL 데이터베이스 연결 구현체
 */
public class PostgreSQLConnection extends AbstractDatabaseConnection {

    public PostgreSQLConnection(DatabaseConfig config) {
        super(config);
    }

    @Override
    protected String buildJdbcUrl() {
        String host = config.getHost();
        String database = config.getDatabase();

        if (host == null || host.trim().isEmpty()) {
            throw new IllegalArgumentException("PostgreSQL host is not configured");
        }
        if (database == null || database.trim().isEmpty()) {
            throw new IllegalArgumentException("PostgreSQL database name is not configured");
        }

        // PostgreSQL JDBC URL: jdbc:postgresql://host:port/database
        int port = config.getPort() > 0 ? config.getPort() : 5432;
        return String.format("jdbc:postgresql://%s:%d/%s", host, port, database);
    }

    @Override
    protected String getCreateTableSQL() {
        String tableName = getTableName();
        return String.format(
            "CREATE TABLE IF NOT EXISTS %s (" +
            "id BIGSERIAL PRIMARY KEY, " +
            "log_timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
            "log_level VARCHAR(10) NOT NULL, " +
            "logger_name VARCHAR(255), " +
            "thread_name VARCHAR(255), " +
            "message TEXT, " +
            "exception TEXT); " +
            "CREATE INDEX IF NOT EXISTS idx_%s_timestamp ON %s(log_timestamp); " +
            "CREATE INDEX IF NOT EXISTS idx_%s_level ON %s(log_level);",
            tableName, tableName, tableName, tableName, tableName
        );
    }

    @Override
    public String getDatabaseType() {
        return "postgresql";
    }
}