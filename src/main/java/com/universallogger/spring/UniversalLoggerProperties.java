package com.universallogger.spring;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for UniversalLogger
 */
@ConfigurationProperties(prefix = "universal-logger")
public class UniversalLoggerProperties {

    /**
     * Enable/disable SQL logging
     */
    private boolean sqlLoggingEnabled = true;

    /**
     * Path to custom configuration file
     */
    private String configFile;

    /**
     * Enable/disable automatic initialization
     */
    private boolean autoInit = true;

    public boolean isSqlLoggingEnabled() {
        return sqlLoggingEnabled;
    }

    public void setSqlLoggingEnabled(boolean sqlLoggingEnabled) {
        this.sqlLoggingEnabled = sqlLoggingEnabled;
    }

    public String getConfigFile() {
        return configFile;
    }

    public void setConfigFile(String configFile) {
        this.configFile = configFile;
    }

    public boolean isAutoInit() {
        return autoInit;
    }

    public void setAutoInit(boolean autoInit) {
        this.autoInit = autoInit;
    }
}
