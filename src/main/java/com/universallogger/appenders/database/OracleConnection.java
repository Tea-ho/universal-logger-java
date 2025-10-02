package com.universallogger.appenders.database;

import com.universallogger.config.LoggerConfig.DatabaseConfig;

/**
 * Oracle 데이터베이스 연결 구현체
 */
public class OracleConnection extends AbstractDatabaseConnection {

    public OracleConnection(DatabaseConfig config) {
        super(config);
    }

    @Override
    protected String buildJdbcUrl() {
        String host = config.getHost();
        String database = config.getDatabase();

        if (host == null || host.trim().isEmpty()) {
            throw new IllegalArgumentException("Oracle host is not configured");
        }
        if (database == null || database.trim().isEmpty()) {
            throw new IllegalArgumentException("Oracle SID/Service name is not configured");
        }

        // Oracle JDBC URL: jdbc:oracle:thin:@host:port:sid
        int port = config.getPort() > 0 ? config.getPort() : 1521;
        return String.format("jdbc:oracle:thin:@%s:%d:%s", host, port, database);
    }

    @Override
    protected String getCreateTableSQL() {
        String tableName = getTableName();
        // Oracle은 AUTO_INCREMENT 대신 SEQUENCE 사용
        return String.format(
            "BEGIN " +
            "EXECUTE IMMEDIATE 'CREATE SEQUENCE %s_seq START WITH 1 INCREMENT BY 1'; " +
            "EXECUTE IMMEDIATE 'CREATE TABLE %s (" +
            "id NUMBER PRIMARY KEY, " +
            "log_timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
            "log_level VARCHAR2(10) NOT NULL, " +
            "logger_name VARCHAR2(255), " +
            "thread_name VARCHAR2(255), " +
            "message CLOB, " +
            "exception CLOB)'; " +
            "EXECUTE IMMEDIATE 'CREATE INDEX idx_%s_timestamp ON %s(log_timestamp)'; " +
            "EXECUTE IMMEDIATE 'CREATE INDEX idx_%s_level ON %s(log_level)'; " +
            "EXCEPTION WHEN OTHERS THEN NULL; " +
            "END;",
            tableName, tableName, tableName, tableName, tableName, tableName
        );
    }

    @Override
    public String getDatabaseType() {
        return "oracle";
    }
}