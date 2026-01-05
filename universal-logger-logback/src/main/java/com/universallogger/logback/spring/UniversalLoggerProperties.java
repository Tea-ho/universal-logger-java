package com.universallogger.spring;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Universal Logger 설정 프로퍼티
 *
 * Spring Boot에서 application.yml/properties로 설정 가능
 *
 * 사용 예:
 * <pre>
 * universal-logger:
 *   enabled: true
 *   config-path: "config/universal-logger.yml"
 *   sql-capture:
 *     enabled: true
 * </pre>
 */
@ConfigurationProperties(prefix = "universal-logger")
public class UniversalLoggerProperties {

    /**
     * Universal Logger 활성화 여부
     * 기본값: true
     */
    private boolean enabled = true;

    /**
     * 설정 파일 경로
     * 기본값: config/universal-logger.yml
     */
    private String configPath = "config/universal-logger.yml";

    /**
     * SQL 캡처 설정
     */
    private SqlCaptureConfig sqlCapture = new SqlCaptureConfig();

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getConfigPath() {
        return configPath;
    }

    public void setConfigPath(String configPath) {
        this.configPath = configPath;
    }

    public SqlCaptureConfig getSqlCapture() {
        return sqlCapture;
    }

    public void setSqlCapture(SqlCaptureConfig sqlCapture) {
        this.sqlCapture = sqlCapture;
    }

    /**
     * SQL 캡처 설정
     */
    public static class SqlCaptureConfig {
        /**
         * SQL 캡처 활성화 여부
         * 기본값: true
         */
        private boolean enabled = true;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }
    }
}
