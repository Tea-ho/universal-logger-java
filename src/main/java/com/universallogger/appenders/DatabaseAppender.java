package com.universallogger.appenders;

import ch.qos.logback.core.AppenderBase;
import ch.qos.logback.classic.spi.ILoggingEvent;
import com.universallogger.appenders.database.DatabaseConnection;
import com.universallogger.appenders.database.DatabaseConnectionFactory;
import com.universallogger.config.LoggerConfig;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 데이터베이스 어펜더 - DatabaseConnectionFactory를 활용하여 다중 DB 지원
 *
 * 지원 데이터베이스:
 * - MySQL
 * - Oracle
 * - PostgreSQL
 * - MSSQL (SQL Server)
 * - H2
 *
 * OOP 개선사항:
 * - Factory 패턴 활용으로 단일 책임 원칙(SRP) 준수
 * - 개방-폐쇄 원칙(OCP) 준수 - 새 DB 추가 시 Factory만 수정
 * - 의존성 역전 원칙(DIP) 준수 - DatabaseConnection 인터페이스에 의존
 */
public class DatabaseAppender extends AppenderBase<ILoggingEvent> {

    private LoggerConfig.DatabaseConfig databaseConfig;
    private DatabaseConnection databaseConnection;
    private Connection connection;
    private PreparedStatement insertStatement;

    @Override
    public void start() {
        super.start();
        setupConnection();
    }

    @Override
    public void stop() {
        super.stop();
        closeResources();
    }

    /**
     * 데이터베이스 연결 설정 - Factory 패턴 활용
     */
    private void setupConnection() {
        if (databaseConfig == null) {
            addError("Database config is not set");
            return;
        }

        try {
            // Factory를 통해 적절한 DatabaseConnection 구현체 생성
            databaseConnection = DatabaseConnectionFactory.createConnection(databaseConfig);

            // 연결 테스트
            if (!databaseConnection.testConnection()) {
                addError("Failed to connect to database: " + databaseConfig.getType());
                return;
            }

            // 테이블 생성 (없으면)
            databaseConnection.createTableIfNotExists();

            // 연결 획득
            connection = databaseConnection.getConnection();

            // PreparedStatement 준비
            String sql = buildInsertSql();
            insertStatement = connection.prepareStatement(sql);

            addInfo("DatabaseAppender started successfully. Type: " + databaseConnection.getDatabaseType());

        } catch (IllegalArgumentException e) {
            addError("Unsupported database configuration: " + e.getMessage());
        } catch (SQLException e) {
            addError("Failed to setup database connection: " + e.getMessage());
        } catch (Exception e) {
            addError("Unexpected error during database setup: " + e.getMessage());
        }
    }

    /**
     * INSERT SQL 빌드
     */
    private String buildInsertSql() {
        String tableName = databaseConfig.getTable();
        if (tableName == null || tableName.isEmpty()) {
            tableName = "logs";
        }
        return String.format(
            "INSERT INTO %s (timestamp, level, logger, message) VALUES (?, ?, ?, ?)",
            tableName
        );
    }

    /**
     * 리소스 정리
     */
    private void closeResources() {
        // PreparedStatement 닫기
        if (insertStatement != null) {
            try {
                insertStatement.close();
            } catch (SQLException e) {
                addWarn("Failed to close PreparedStatement: " + e.getMessage());
            }
            insertStatement = null;
        }

        // Connection 닫기
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                addWarn("Failed to close Connection: " + e.getMessage());
            }
            connection = null;
        }

        // DatabaseConnection 닫기
        if (databaseConnection != null) {
            databaseConnection.close();
            databaseConnection = null;
        }
    }

    @Override
    protected synchronized void append(ILoggingEvent event) {
        if (insertStatement == null) {
            return;
        }

        try {
            // 타임스탬프
            insertStatement.setString(1,
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            // 로그 레벨
            insertStatement.setString(2, event.getLevel().toString());
            // 로거 이름
            insertStatement.setString(3, event.getLoggerName());
            // 메시지
            insertStatement.setString(4, event.getFormattedMessage());

            insertStatement.executeUpdate();

        } catch (SQLException e) {
            addError("Failed to insert log into database: " + e.getMessage());
            // 연결이 끊어진 경우 재연결 시도
            if (isConnectionClosed()) {
                reconnect();
            }
        }
    }

    /**
     * 연결 상태 확인
     */
    private boolean isConnectionClosed() {
        try {
            return connection == null || connection.isClosed();
        } catch (SQLException e) {
            return true;
        }
    }

    /**
     * 재연결 시도
     */
    private void reconnect() {
        addInfo("Attempting to reconnect to database...");
        closeResources();
        setupConnection();
    }

    /**
     * 데이터베이스 설정 지정
     */
    public void setDatabaseConfig(LoggerConfig.DatabaseConfig databaseConfig) {
        this.databaseConfig = databaseConfig;
    }

    /**
     * 현재 데이터베이스 연결 반환 (테스트/디버깅용)
     */
    public DatabaseConnection getDatabaseConnection() {
        return databaseConnection;
    }
}
