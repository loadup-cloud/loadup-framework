/* Copyright 2026 LoadUp Cloud. Licensed under the Apache License, Version 2.0. */
package io.github.loadup.components.database.autoconfigure;

import io.github.loadup.components.database.config.DatabaseProperties;
import io.github.loadup.components.database.tenant.DatabaseTenant;
import java.time.Clock;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
@EnableConfigurationProperties(DatabaseProperties.class)
public class DatabaseCoreAutoConfiguration {
    @Bean
    @ConditionalOnMissingBean
    public DatabaseTenant databaseTenant(DatabaseProperties properties) {
        return new DatabaseTenant(properties);
    }

    @Bean("databaseClock")
    @ConditionalOnMissingBean(name = "databaseClock")
    public Clock databaseClock() {
        return Clock.systemDefaultZone();
    }
}
