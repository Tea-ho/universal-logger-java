package com.universallogger.log4j2.spring;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for UniversalLogger Log4j2
 *
 * application.yml 예시:
 * <pre>
 * universal-logger-log4j2:
 *   enabled: true
 *   config-path: "config/universal-logger.yml"
 *   sql-capture:
 *     enabled: true
 * </pre>
 */
@ConfigurationProperties(prefix = "universal-logger-log4j2")
public class UniversalLoggerProperties {

    /**
     * UniversalLogger 활성화 여부
     */
    private boolean enabled = true;

    /**
     * 설정 파일 경로
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
