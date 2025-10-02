package com.universallogger.appenders.database;

import com.universallogger.config.LoggerConfig.DatabaseConfig;

/**
 * 데이터베이스 연결 팩토리
 * Factory 패턴을 사용하여 데이터베이스 타입에 따라 적절한 연결 객체 생성
 */
public class DatabaseConnectionFactory {

    private DatabaseConnectionFactory() {
        // 유틸리티 클래스 - 인스턴스화 방지
    }

    /**
     * 데이터베이스 설정에 따라 적절한 DatabaseConnection 구현체 생성
     *
     * @param config 데이터베이스 설정
     * @return DatabaseConnection 구현체
     * @throws IllegalArgumentException 지원하지 않는 데이터베이스 타입인 경우
     */
    public static DatabaseConnection createConnection(DatabaseConfig config) {
        if (config == null) {
            throw new IllegalArgumentException("Database config cannot be null");
        }

        String dbType = config.getType();
        if (dbType == null || dbType.trim().isEmpty()) {
            throw new IllegalArgumentException("Database type cannot be null or empty");
        }

        switch (dbType.toLowerCase()) {
            case "mysql":
                return new MySQLConnection(config);
            case "oracle":
                return new OracleConnection(config);
            case "mssql":
            case "sqlserver":
                return new MSSQLConnection(config);
            case "postgresql":
            case "postgres":
                return new PostgreSQLConnection(config);
            case "h2":
                return new H2Connection(config);
            default:
                throw new IllegalArgumentException(
                    "Unsupported database type: " + dbType +
                    ". Supported types: mysql, oracle, mssql, postgresql, h2"
                );
        }
    }
}