package com.universallogger.constants;

/**
 * 로거 상수 정의
 * Hard-code 제거를 위한 중앙 집중식 상수 관리
 */
public final class LoggerConstants {

    private LoggerConstants() {
        // 유틸리티 클래스 - 인스턴스화 방지
    }

    /**
     * 로그 레벨
     */
    public static final class LogLevel {
        public static final String TRACE = "TRACE";
        public static final String DEBUG = "DEBUG";
        public static final String INFO = "INFO";
        public static final String WARN = "WARN";
        public static final String ERROR = "ERROR";

        private LogLevel() {}
    }

    /**
     * 데이터베이스 타입
     */
    public static final class DatabaseType {
        public static final String MYSQL = "mysql";
        public static final String ORACLE = "oracle";
        public static final String MSSQL = "mssql";
        public static final String POSTGRESQL = "postgresql";
        public static final String H2 = "h2";

        private DatabaseType() {}
    }

    /**
     * 데이터베이스 기본 포트
     */
    public static final class DatabasePort {
        public static final int MYSQL = 3306;
        public static final int ORACLE = 1521;
        public static final int MSSQL = 1433;
        public static final int POSTGRESQL = 5432;

        private DatabasePort() {}
    }

    /**
     * 설정 파일 경로 (자동 감지 우선순위)
     */
    public static final class ConfigPath {
        public static final String[] SEARCH_PATHS = {
            "universal-logger.yml",
            "config/universal-logger.yml",
            "universal-logger.xml",
            "config/universal-logger.xml",
            "logback.xml",
            "config/logback.xml",
            "minimal.yml",
            "config/minimal.yml",
            "classpath:universal-logger.yml",
            "classpath:logback.xml"
        };

        private ConfigPath() {}
    }

    /**
     * 기본값
     */
    public static final class Defaults {
        public static final String APP_NAME = "UniversalLogger";
        public static final String LOG_LEVEL = "INFO";
        public static final String TIMEZONE = "Asia/Seoul";
        public static final String ENCODING = "UTF-8";
        public static final String LOG_DIR = "logs";
        public static final String TABLE_NAME = "app_logs";

        // 로테이션 기본값
        public static final String MAX_FILE_SIZE = "10MB";
        public static final int MAX_FILES = 30;
        public static final boolean COMPRESS = true;

        // 성능 기본값
        public static final boolean ASYNC = true;
        public static final int BUFFER_SIZE = 1000;
        public static final int FLUSH_INTERVAL = 5;
        public static final int MAX_QUEUE_SIZE = 10000;

        private Defaults() {}
    }

    /**
     * 로그 포맷 변수
     */
    public static final class FormatVariable {
        public static final String TIMESTAMP = "{timestamp}";
        public static final String LEVEL = "{level}";
        public static final String LOGGER = "{logger}";
        public static final String CLASS = "{class}";
        public static final String METHOD = "{method}";
        public static final String LINE = "{line}";
        public static final String THREAD = "{thread}";
        public static final String MESSAGE = "{message}";
        public static final String APP_NAME = "{appName}";
        public static final String DATE = "{date}";
        public static final String DATETIME = "{datetime}";

        private FormatVariable() {}
    }

    /**
     * 설정 파일 버전
     */
    public static final class Version {
        public static final String CURRENT = "1.0";

        private Version() {}
    }

    /**
     * 예외 메시지
     */
    public static final class ErrorMessage {
        public static final String CONFIG_NOT_FOUND = "Config file not found: ";
        public static final String INVALID_CONFIG = "Invalid configuration: ";
        public static final String UNSUPPORTED_DB_TYPE = "Unsupported database type: ";
        public static final String DB_CONNECTION_FAILED = "Database connection failed: ";
        public static final String INIT_FAILED = "Failed to initialize Universal Logger: ";

        private ErrorMessage() {}
    }
}