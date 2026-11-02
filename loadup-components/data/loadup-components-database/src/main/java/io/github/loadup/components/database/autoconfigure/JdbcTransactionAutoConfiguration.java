/* Copyright 2026 LoadUp Cloud. Licensed under the Apache License, Version 2.0. */
package io.github.loadup.components.database.autoconfigure;

import javax.sql.DataSource;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnSingleCandidate;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.jdbc.support.JdbcTransactionManager;
import org.springframework.transaction.TransactionManager;

/**
 * Selects a DataSource-bound JDBC manager before MyBatis-Flex installs its fallback manager.
 * JDBC business writes and reliability records must participate in the same local transaction.
 */
@AutoConfiguration(
        afterName = "org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration",
        beforeName = {
            "com.mybatisflex.spring.boot.v4.FlexTransactionAutoConfiguration",
            "org.springframework.boot.jdbc.autoconfigure.DataSourceTransactionManagerAutoConfiguration"
        })
@ConditionalOnSingleCandidate(DataSource.class)
public class JdbcTransactionAutoConfiguration {
    @Bean
    @ConditionalOnMissingBean(TransactionManager.class)
    public JdbcTransactionManager transactionManager(DataSource dataSource) {
        return new JdbcTransactionManager(dataSource);
    }

    /**
     * Provides the fluent JDBC client for infrastructure queries. Spring Boot normally supplies
     * this bean; the fallback keeps narrow test slices and custom auto-configuration working.
     */
    @Bean
    @ConditionalOnMissingBean(JdbcClient.class)
    public JdbcClient jdbcClient(DataSource dataSource) {
        return JdbcClient.create(dataSource);
    }
}
