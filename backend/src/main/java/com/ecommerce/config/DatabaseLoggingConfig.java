package com.ecommerce.config;

import lombok.extern.slf4j.Slf4j;
import org.hibernate.engine.jdbc.spi.SqlStatementLogger;
import org.slf4j.MDC;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

@Configuration
@Slf4j
public class DatabaseLoggingConfig {

    @Bean
    public DatabaseQueryLogger databaseQueryLogger() {
        return new DatabaseQueryLogger();
    }

    public static class DatabaseQueryLogger {
        
        public void logQuery(String sql, Object[] parameters) {
            String traceId = MDC.get("traceId");
            log.debug("[{}] SQL_QUERY: {}", traceId, sql);
            
            if (parameters != null && parameters.length > 0) {
                log.trace("[{}] SQL_PARAMETERS: {}", traceId, java.util.Arrays.toString(parameters));
            }
        }
        
        public void logQueryExecution(String sql, long executionTime) {
            String traceId = MDC.get("traceId");
            log.debug("[{}] SQL_EXECUTION_TIME: {}ms for query: {}", traceId, executionTime, sql);
            
            if (executionTime > 1000) {
                log.warn("[{}] SLOW_SQL_QUERY: Query took {}ms - {}", traceId, executionTime, sql);
            }
        }
        
        public void logConnectionInfo(String operation, String details) {
            String traceId = MDC.get("traceId");
            log.trace("[{}] DB_CONNECTION: {} - {}", traceId, operation, details);
        }
        
        public void logTransactionInfo(String operation, String details) {
            String traceId = MDC.get("traceId");
            log.debug("[{}] DB_TRANSACTION: {} - {}", traceId, operation, details);
        }
    }
}