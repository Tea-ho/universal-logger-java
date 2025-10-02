package com.universallogger.validation;

import com.universallogger.config.LoggerConfig;
import com.universallogger.config.LoggerConfig.DatabaseConfig;
import com.universallogger.constants.LoggerConstants;

import java.util.ArrayList;
import java.util.List;

/**
 * 설정 검증 클래스
 * 설정 파일의 유효성을 검사하고 오류를 보고
 */
public class ConfigValidator {

    private ConfigValidator() {
        // 유틸리티 클래스 - 인스턴스화 방지
    }

    /**
     * 로거 설정 전체 검증
     *
     * @param config 로거 설정
     * @return 검증 결과
     */
    public static ValidationResult validate(LoggerConfig config) {
        ValidationResult result = new ValidationResult();

        if (config == null) {
            result.addError("LoggerConfig cannot be null");
            return result;
        }

        // 글로벌 설정 검증
        validateGlobalConfig(config.getGlobal(), result);

        // 출력 설정 검증
        if (config.getOutputs() != null && !config.getOutputs().isEmpty()) {
            config.getOutputs().forEach((name, outputConfig) -> {
                validateOutputConfig(name, outputConfig, result);
            });
        }

        return result;
    }

    /**
     * 글로벌 설정 검증
     */
    private static void validateGlobalConfig(LoggerConfig.GlobalConfig global, ValidationResult result) {
        if (global == null) {
            result.addWarning("Global config is null, using defaults");
            return;
        }

        if (global.getAppName() == null || global.getAppName().trim().isEmpty()) {
            result.addWarning("App name is not set, using default: " + LoggerConstants.Defaults.APP_NAME);
        }

        if (global.getEncoding() == null || global.getEncoding().trim().isEmpty()) {
            result.addWarning("Encoding is not set, using default: " + LoggerConstants.Defaults.ENCODING);
        }
    }

    /**
     * 출력 설정 검증
     */
    private static void validateOutputConfig(String name, LoggerConfig.OutputConfig output, ValidationResult result) {
        if (output == null) {
            result.addWarning("Output config '" + name + "' is null");
            return;
        }

        if (!output.isEnabled()) {
            // 비활성화된 출력은 검증하지 않음
            return;
        }

        // 레벨 검증
        String level = output.getLevel();
        if (level != null && !isValidLogLevel(level)) {
            result.addWarning("Invalid log level '" + level + "' for output '" + name + "'");
        }

        // 파일 출력 검증
        if ("file".equals(name) || "errorFile".equals(name)) {
            validateFileOutput(name, output, result);
        }

        // 데이터베이스 출력 검증
        if ("database".equals(name)) {
            validateDatabaseConfig(output.getDatabase(), result);
        }

        // 웹훅 출력 검증
        if ("webhook".equals(name)) {
            validateWebhookConfig(output.getWebhook(), result);
        }

        // 이메일 출력 검증
        if ("email".equals(name)) {
            validateEmailConfig(output.getEmail(), result);
        }
    }

    /**
     * 파일 출력 검증
     */
    private static void validateFileOutput(String name, LoggerConfig.OutputConfig output, ValidationResult result) {
        String path = output.getPath();
        if (path == null || path.trim().isEmpty()) {
            result.addError("File path is not set for output '" + name + "'");
        }

        LoggerConfig.RotationConfig rotation = output.getRotation();
        if (rotation != null) {
            if (rotation.getMaxSize() == null || rotation.getMaxSize().trim().isEmpty()) {
                result.addWarning("Max file size is not set for output '" + name + "', using default");
            }
            if (rotation.getMaxFiles() <= 0) {
                result.addWarning("Max files must be positive for output '" + name + "'");
            }
        }
    }

    /**
     * 데이터베이스 설정 검증
     */
    private static void validateDatabaseConfig(DatabaseConfig config, ValidationResult result) {
        if (config == null) {
            result.addError("Database config is required when database output is enabled");
            return;
        }

        if (config.getType() == null || config.getType().trim().isEmpty()) {
            result.addError("Database type is not set");
        } else if (!isValidDatabaseType(config.getType())) {
            result.addError("Unsupported database type: " + config.getType());
        }

        if (config.getHost() == null || config.getHost().trim().isEmpty()) {
            result.addError("Database host is not set");
        }

        if (config.getDatabase() == null || config.getDatabase().trim().isEmpty()) {
            result.addError("Database name is not set");
        }

        if (config.getUsername() == null || config.getUsername().trim().isEmpty()) {
            result.addError("Database username is not set");
        }

        if (config.getPassword() == null || config.getPassword().trim().isEmpty()) {
            result.addWarning("Database password is not set");
        }
    }

    /**
     * 웹훅 설정 검증
     */
    private static void validateWebhookConfig(LoggerConfig.WebhookConfig config, ValidationResult result) {
        if (config == null) {
            result.addError("Webhook config is required when webhook output is enabled");
            return;
        }

        if (config.getUrl() == null || config.getUrl().trim().isEmpty()) {
            result.addError("Webhook URL is not set");
        } else if (!config.getUrl().startsWith("http://") && !config.getUrl().startsWith("https://")) {
            result.addWarning("Webhook URL should start with http:// or https://");
        }
    }

    /**
     * 이메일 설정 검증
     */
    private static void validateEmailConfig(LoggerConfig.EmailConfig config, ValidationResult result) {
        if (config == null) {
            result.addError("Email config is required when email output is enabled");
            return;
        }

        LoggerConfig.EmailConfig.SmtpConfig smtp = config.getSmtp();
        if (smtp == null) {
            result.addError("SMTP config is not set");
        } else {
            if (smtp.getHost() == null || smtp.getHost().trim().isEmpty()) {
                result.addError("SMTP host is not set");
            }
            if (smtp.getPort() <= 0) {
                result.addError("SMTP port must be positive");
            }
        }

        if (config.getTo() == null || config.getTo().length == 0) {
            result.addError("Email recipient(s) not set");
        }
    }

    /**
     * 로그 레벨 유효성 확인
     */
    private static boolean isValidLogLevel(String level) {
        if (level == null) {
            return false;
        }
        String upperLevel = level.toUpperCase();
        return LoggerConstants.LogLevel.TRACE.equals(upperLevel) ||
               LoggerConstants.LogLevel.DEBUG.equals(upperLevel) ||
               LoggerConstants.LogLevel.INFO.equals(upperLevel) ||
               LoggerConstants.LogLevel.WARN.equals(upperLevel) ||
               LoggerConstants.LogLevel.ERROR.equals(upperLevel);
    }

    /**
     * 데이터베이스 타입 유효성 확인
     */
    private static boolean isValidDatabaseType(String type) {
        if (type == null) {
            return false;
        }
        String lowerType = type.toLowerCase();
        return LoggerConstants.DatabaseType.MYSQL.equals(lowerType) ||
               LoggerConstants.DatabaseType.ORACLE.equals(lowerType) ||
               LoggerConstants.DatabaseType.MSSQL.equals(lowerType) ||
               "sqlserver".equals(lowerType) ||
               LoggerConstants.DatabaseType.POSTGRESQL.equals(lowerType) ||
               "postgres".equals(lowerType) ||
               LoggerConstants.DatabaseType.H2.equals(lowerType);
    }

    /**
     * 검증 결과 클래스
     */
    public static class ValidationResult {
        private final List<String> errors = new ArrayList<>();
        private final List<String> warnings = new ArrayList<>();

        public void addError(String error) {
            errors.add(error);
        }

        public void addWarning(String warning) {
            warnings.add(warning);
        }

        public boolean isValid() {
            return errors.isEmpty();
        }

        public List<String> getErrors() {
            return errors;
        }

        public List<String> getWarnings() {
            return warnings;
        }

        public boolean hasWarnings() {
            return !warnings.isEmpty();
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            if (!errors.isEmpty()) {
                sb.append("Errors:\n");
                errors.forEach(error -> sb.append("  - ").append(error).append("\n"));
            }
            if (!warnings.isEmpty()) {
                sb.append("Warnings:\n");
                warnings.forEach(warning -> sb.append("  - ").append(warning).append("\n"));
            }
            return sb.toString();
        }
    }
}