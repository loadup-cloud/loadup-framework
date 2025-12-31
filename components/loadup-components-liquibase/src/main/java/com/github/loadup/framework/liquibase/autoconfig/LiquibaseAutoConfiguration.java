package com.github.loadup.framework.liquibase.autoconfig;

/*-
 * #%L
 * loadup-components-liquibase
 * %%
 * Copyright (C) 2022 - 2025 loadup_cloud
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

import javax.sql.DataSource;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.*;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.util.StringUtils;

import com.github.loadup.framework.liquibase.config.LiquibaseProperties;

import liquibase.integration.spring.SpringLiquibase;

/**
 * Liquibase Auto Configuration for Spring Boot 3
 *
 * <p>This configuration class automatically configures Liquibase for database migration when the
 * following conditions are met:
 *
 * <ul>
 *   <li>SpringLiquibase class is present in the classpath
 *   <li>A DataSource bean is available
 *   <li>The property loadup.liquibase.enabled is true (default)
 * </ul>
 *
 * <p>Configuration properties can be customized via application.yml/properties using the prefix
 * 'loadup.liquibase'
 *
 * @author Lise
 */
@AutoConfiguration
@ConditionalOnClass(SpringLiquibase.class)
@ConditionalOnBean(DataSource.class)
@ConditionalOnProperty(
    prefix = "loadup.liquibase",
    name = "enabled",
    havingValue = "true",
    matchIfMissing = true)
@EnableConfigurationProperties(LiquibaseProperties.class)
public class LiquibaseAutoConfiguration {

  /**
   * Create SpringLiquibase bean with customized configuration
   *
   * @param dataSource the data source to use for migration
   * @param properties the Liquibase configuration properties
   * @return configured SpringLiquibase instance
   */
  @Bean
  public SpringLiquibase liquibase(DataSource dataSource, LiquibaseProperties properties) {
    SpringLiquibase liquibase = new SpringLiquibase();
    liquibase.setDataSource(dataSource);
    liquibase.setChangeLog(properties.getChangeLog());
    liquibase.setShouldRun(properties.isEnabled());

    // Optional configurations
    if (StringUtils.hasText(properties.getContexts())) {
      liquibase.setContexts(properties.getContexts());
    }

    if (StringUtils.hasText(properties.getLabels())) {
      liquibase.setLabels(properties.getLabels());
    }

    if (StringUtils.hasText(properties.getDefaultSchema())) {
      liquibase.setDefaultSchema(properties.getDefaultSchema());
    }

    if (StringUtils.hasText(properties.getLiquibaseSchema())) {
      liquibase.setLiquibaseSchema(properties.getLiquibaseSchema());
    }

    if (StringUtils.hasText(properties.getLiquibaseTablespace())) {
      liquibase.setLiquibaseTablespace(properties.getLiquibaseTablespace());
    }

    liquibase.setDatabaseChangeLogTable(properties.getDatabaseChangeLogTable());
    liquibase.setDatabaseChangeLogLockTable(properties.getDatabaseChangeLogLockTable());
    liquibase.setDropFirst(properties.isDropFirst());
    liquibase.setClearCheckSums(properties.isClearChecksums());

    return liquibase;
  }
}
