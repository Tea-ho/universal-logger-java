package com.universallogger.appenders.database;

import com.universallogger.config.LoggerConfig.DatabaseConfig;

/**
 * H2 데이터베이스 연결 구현체 (테스트용)
 */
public class H2Connection extends AbstractDatabaseConnection {

    public H2Connection(DatabaseConfig config) {
        super(config);
    }

    @Override
    protected String buildJdbcUrl() {
        String database = config.getDatabase();

        if (database == null || database.trim().isEmpty()) {
            throw new IllegalArgumentException("H2 database name is not configured");
        }

        // H2 JDBC URL: jdbc:h2:file:./database 또는 jdbc:h2:mem:database
        String host = config.getHost();
        if ("memory".equalsIgnoreCase(host) || "mem".equalsIgnoreCase(host)) {
            return String.format("jdbc:h2:mem:%s", database);
        } else {
            return String.format("jdbc:h2:file:./%s", database);
        }
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
            "message CLOB, " +
            "exception CLOB); " +
            "CREATE INDEX IF NOT EXISTS idx_%s_timestamp ON %s(log_timestamp); " +
            "CREATE INDEX IF NOT EXISTS idx_%s_level ON %s(log_level);",
            tableName, tableName, tableName, tableName, tableName
        );
    }

    @Override
    public String getDatabaseType() {
        return "h2";
    }
}