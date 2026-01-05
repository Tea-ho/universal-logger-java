package com.universallogger.logback;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.Configurator;
import ch.qos.logback.classic.spi.Configurator.ExecutionStatus;
import ch.qos.logback.core.spi.ContextAwareBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Logback Configurator - Logback 시작 시 자동으로 호출됨
 *
 * ServiceLoader를 통해 자동 발견:
 * META-INF/services/ch.qos.logback.classic.spi.Configurator
 */
public class LogbackConfigurator extends ContextAwareBase implements Configurator {

    private static final Logger logger = LoggerFactory.getLogger(LogbackConfigurator.class);

    @Override
    public ExecutionStatus configure(LoggerContext loggerContext) {
        addInfo("UniversalLogger Logback Configurator starting...");

        try {
            // LoggerInstance를 통해 설정 적용
            // LoggerInstance는 이미 LoggerContext를 초기화하고 설정하는 로직을 가지고 있음
            LoggerInstance.builder().build();

            addInfo("UniversalLogger Logback configuration completed successfully");
            return ExecutionStatus.DO_NOT_INVOKE_NEXT_IF_ANY;

        } catch (Exception e) {
            addError("Failed to configure UniversalLogger", e);
            logger.error("UniversalLogger configuration failed", e);
            return ExecutionStatus.INVOKE_NEXT_IF_ANY;
        }
    }
}
