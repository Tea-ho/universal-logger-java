package com.universallogger.appenders;

import ch.qos.logback.core.AppenderBase;
import ch.qos.logback.classic.spi.ILoggingEvent;
import com.universallogger.config.LoggerConfig;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 데이터베이스 어펜더
 */
public class DatabaseAppender extends AppenderBase<ILoggingEvent> {

    private LoggerConfig.DatabaseConfig databaseConfig;
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
        if (insertStatement != null) {
            try {
                insertStatement.close();
            } catch (SQLException e) {
                // ignore
            }
        }
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                // ignore
            }
        }
    }

    private void setupConnection() {
        if (databaseConfig == null) {
            return;
        }

        try {
            String url;
            if ("mysql".equals(databaseConfig.getType())) {
                url = String.format("jdbc:mysql://%s/%s",
                    databaseConfig.getHost(), databaseConfig.getDatabase());
                Class.forName("com.mysql.cj.jdbc.Driver");
            } else {
                addError("Unsupported database type: " + databaseConfig.getType());
                return;
            }

            connection = DriverManager.getConnection(url,
                databaseConfig.getUsername(), databaseConfig.getPassword());

            createTableIfNotExists();

            String sql = String.format(
                "INSERT INTO %s (timestamp, level, logger, message) VALUES (?, ?, ?, ?)",
                databaseConfig.getTable()
            );
            insertStatement = connection.prepareStatement(sql);

        } catch (Exception e) {
            addError("Failed to setup database connection: " + e.getMessage());
        }
    }

    private void createTableIfNotExists() throws SQLException {
        String createTableSql = String.format(
            "CREATE TABLE IF NOT EXISTS %s (" +
                "id INT AUTO_INCREMENT PRIMARY KEY, " +
                "timestamp DATETIME, " +
                "level VARCHAR(10), " +
                "logger VARCHAR(100), " +
                "message TEXT, " +
                "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                ")",
            databaseConfig.getTable()
        );

        try (PreparedStatement stmt = connection.prepareStatement(createTableSql)) {
            stmt.execute();
        }
    }

    @Override
    protected void append(ILoggingEvent event) {
        if (insertStatement == null) {
            return;
        }

        try {
            insertStatement.setString(1,
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            insertStatement.setString(2, event.getLevel().toString());
            insertStatement.setString(3, event.getLoggerName());
            insertStatement.setString(4, event.getFormattedMessage());

            insertStatement.executeUpdate();

        } catch (SQLException e) {
            addError("Failed to insert log into database: " + e.getMessage());
        }
    }

    public void setDatabaseConfig(LoggerConfig.DatabaseConfig databaseConfig) {
        this.databaseConfig = databaseConfig;
    }
}