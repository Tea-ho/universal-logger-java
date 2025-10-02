package com.universallogger.appenders.database;

import com.universallogger.config.LoggerConfig.DatabaseConfig;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * 데이터베이스 연결 추상 클래스
 * 공통 로직을 구현하고 DB별 차이는 하위 클래스에서 구현
 */
public abstract class AbstractDatabaseConnection implements DatabaseConnection {

    protected final DatabaseConfig config;
    protected Connection connection;

    protected AbstractDatabaseConnection(DatabaseConfig config) {
        if (config == null) {
            throw new IllegalArgumentException("DatabaseConfig cannot be null");
        }
        this.config = config;
    }

    @Override
    public Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            String jdbcUrl = buildJdbcUrl();
            String username = config.getUsername();
            String password = config.getPassword();

            if (username == null || username.trim().isEmpty()) {
                throw new SQLException("Database username is not configured");
            }

            connection = DriverManager.getConnection(jdbcUrl, username, password);
        }
        return connection;
    }

    @Override
    public void close() {
        if (connection != null) {
            try {
                if (!connection.isClosed()) {
                    connection.close();
                }
            } catch (SQLException e) {
                System.err.println("Failed to close database connection: " + e.getMessage());
            }
        }
    }

    @Override
    public void createTableIfNotExists() throws SQLException {
        String createTableSQL = getCreateTableSQL();
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(createTableSQL);
        }
    }

    @Override
    public boolean testConnection() {
        try (Connection conn = getConnection()) {
            return conn != null && !conn.isClosed();
        } catch (SQLException e) {
            System.err.println("Database connection test failed: " + e.getMessage());
            return false;
        }
    }

    /**
     * JDBC URL 생성 (DB별로 다름)
     *
     * @return JDBC URL
     */
    protected abstract String buildJdbcUrl();

    /**
     * 테이블 생성 SQL 반환 (DB별로 다름)
     *
     * @return CREATE TABLE SQL
     */
    protected abstract String getCreateTableSQL();

    /**
     * 테이블명 반환
     *
     * @return 테이블명
     */
    protected String getTableName() {
        String tableName = config.getTable();
        return (tableName != null && !tableName.trim().isEmpty()) ? tableName : "app_logs";
    }
}