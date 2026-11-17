/*
 * Copyright (C) 2026 LoadUp Cloud
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.github.loadup.components.database.autoconfigure;

import io.github.loadup.components.database.config.DatabaseProperties;
import io.github.loadup.components.database.schema.FixedSchemaVerifier;
import javax.sql.DataSource;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnSingleCandidate;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration;
import org.springframework.boot.sql.init.dependency.DependsOnDatabaseInitialization;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Assembles the fixed database target of one deployment.
 *
 * <p>The schema comes from {@code loadup.database.schema} only. Startup verifies that a physical
 * connection really resolves to it before workers, schedulers or scanners may start.
 */
@AutoConfiguration(after = DataSourceAutoConfiguration.class)
@EnableConfigurationProperties(DatabaseProperties.class)
public class DatabaseCoreAutoConfiguration {

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnSingleCandidate(DataSource.class)
    static class FixedSchemaVerification {
        @Bean
        @ConditionalOnMissingBean
        @DependsOnDatabaseInitialization
        FixedSchemaVerifier fixedSchemaVerifier(DataSource dataSource, DatabaseProperties properties) {
            var verifier = new FixedSchemaVerifier(dataSource, properties.getSchema());
            verifier.verify();
            return verifier;
        }
    }
}
