package com.ecommerce.config;

import com.ecommerce.service.SecretsManagerService;
import com.zaxxer.hikari.HikariDataSource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class DatabaseConfig {

    private final SecretsManagerService secretsManagerService;

    @Value("${spring.datasource.url}")
    private String databaseUrl;

    @Value("${spring.datasource.driver-class-name}")
    private String driverClassName;

    @Bean
    @Primary
    public DataSource dataSource() {
        log.info("Configuring DataSource with credentials from AWS Secrets Manager");
        
        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setJdbcUrl(databaseUrl);
        dataSource.setDriverClassName(driverClassName);
        dataSource.setUsername(secretsManagerService.getDatabaseUsername());
        dataSource.setPassword(secretsManagerService.getDatabasePassword());
        
        // HikariCP settings
        dataSource.setMaximumPoolSize(20);
        dataSource.setMinimumIdle(5);
        dataSource.setIdleTimeout(300000);
        dataSource.setConnectionTimeout(20000);
        dataSource.setLeakDetectionThreshold(60000);
        
        log.info("DataSource configured successfully");
        return dataSource;
    }
}