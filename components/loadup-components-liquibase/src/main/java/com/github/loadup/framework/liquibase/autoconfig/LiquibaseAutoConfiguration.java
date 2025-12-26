/* Copyright (C) LoadUp Cloud 2022-2025 */
package com.github.loadup.framework.liquibase.autoconfig;

/*-
 * #%L
 * loadup-components-liquibase
 * %%
 * Copyright (C) 2022 - 2025 loadup_cloud
 * %%
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 * #L%
 */

import com.github.loadup.framework.liquibase.config.LiquibaseProperties;
import liquibase.integration.spring.SpringLiquibase;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.*;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.util.StringUtils;

import javax.sql.DataSource;

/**
 * Liquibase Auto Configuration for Spring Boot 3
 *
 * <p>This configuration class automatically configures Liquibase for database migration
 * when the following conditions are met:</p>
 * <ul>
 *   <li>SpringLiquibase class is present in the classpath</li>
 *   <li>A DataSource bean is available</li>
 *   <li>The property loadup.liquibase.enabled is true (default)</li>
 * </ul>
 *
 * <p>Configuration properties can be customized via application.yml/properties using the prefix 'loadup.liquibase'</p>
 *
 * @author Lise
 */
@AutoConfiguration
@ConditionalOnClass(SpringLiquibase.class)
@ConditionalOnBean(DataSource.class)
@ConditionalOnProperty(prefix = "loadup.liquibase", name = "enabled", havingValue = "true", matchIfMissing = true)
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

