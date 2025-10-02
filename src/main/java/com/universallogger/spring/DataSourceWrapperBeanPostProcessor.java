package com.universallogger.spring;

import com.universallogger.UniversalLogger;
import com.universallogger.sql.SqlCaptureProxy;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;

/**
 * BeanPostProcessor to wrap DataSource with SQL logging proxy
 */
@Component
public class DataSourceWrapperBeanPostProcessor implements BeanPostProcessor {

    private final UniversalLoggerProperties properties;

    public DataSourceWrapperBeanPostProcessor(UniversalLoggerProperties properties) {
        this.properties = properties;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        if (bean instanceof DataSource && properties.isSqlLoggingEnabled()) {
            // Avoid wrapping if already wrapped
            if (bean instanceof SqlCaptureProxy) {
                return bean;
            }

            UniversalLogger.info("[UniversalLogger] Wrapping DataSource bean: " + beanName);
            return new SqlCaptureProxy((DataSource) bean);
        }
        return bean;
    }
}
