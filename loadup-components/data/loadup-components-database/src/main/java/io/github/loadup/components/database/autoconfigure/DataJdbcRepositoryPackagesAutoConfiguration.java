/* Copyright 2026 LoadUp Cloud. Licensed under the Apache License, Version 2.0. */
package io.github.loadup.components.database.autoconfigure;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.data.jdbc.autoconfigure.DataJdbcRepositoriesAutoConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.data.jdbc.repository.config.AbstractJdbcConfiguration;

/**
 * Makes the framework module packages visible to Spring Boot's single Spring Data JDBC repository
 * scan. Boot scans only the application's auto-configuration packages by default, which excludes
 * the reusable modules packaged in this framework.
 */
@AutoConfiguration(before = DataJdbcRepositoriesAutoConfiguration.class)
@ConditionalOnClass(AbstractJdbcConfiguration.class)
@Import(DataJdbcRepositoryPackagesRegistrar.class)
public class DataJdbcRepositoryPackagesAutoConfiguration {}
