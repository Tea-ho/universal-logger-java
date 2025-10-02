package com.universallogger.appenders.database;

import com.universallogger.config.LoggerConfig.DatabaseConfig;

/**
 * MySQL 데이터베이스 연결 구현체
 */
public class MySQLConnection extends AbstractDatabaseConnection {

    public MySQLConnection(DatabaseConfig config) {
        super(config);
    }

    @Override
    protected String buildJdbcUrl() {
        String host = config.getHost();
        String database = config.getDatabase();

        if (host == null || host.trim().isEmpty()) {
            throw new IllegalArgumentException("MySQL host is not configured");
        }
        if (database == null || database.trim().isEmpty()) {
            throw new IllegalArgumentException("MySQL database name is not configured");
        }

        // MySQL JDBC URL: jdbc:mysql://host:port/database?useSSL=false&serverTimezone=UTC
        int port = config.getPort() > 0 ? config.getPort() : 3306;
        return String.format(
            "jdbc:mysql://%s:%d/%s?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true",
            host, port, database
        );
    }

    @Override
    protected String getCreateTableSQL() {
        String tableName = getTableName();
        return String.format(
            "CREATE TABLE IF NOT EXISTS %s (" +
            "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
            "log_timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
            "log_level VARCHAR(10) NOT NULL, " +
            "logger_name VARCHAR(255), " +
            "thread_name VARCHAR(255), " +
            "message TEXT, " +
            "exception TEXT, " +
            "INDEX idx_timestamp (log_timestamp), " +
            "INDEX idx_level (log_level)" +
            ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4",
            tableName
        );
    }

    @Override
    public String getDatabaseType() {
        return "mysql";
    }
}