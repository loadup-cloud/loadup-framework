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

import static org.assertj.core.api.Assertions.assertThat;

import io.github.loadup.components.database.config.DatabaseProperties;
import io.github.loadup.components.database.schema.FixedSchemaVerifier;
import java.time.Clock;
import javax.sql.DataSource;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

class DatabaseCoreAutoConfigurationTest {
    private final ApplicationContextRunner runner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(DatabaseCoreAutoConfiguration.class));

    @Test
    void readsTheSchemaFromTheSingleConfiguredProperty() {
        runner.withPropertyValues("loadup.database.schema=ledger").run(context -> {
            assertThat(context).hasNotFailed().hasSingleBean(DatabaseProperties.class);
            assertThat(context.getBean(DatabaseProperties.class).getSchema()).isEqualTo("ledger");
            assertThat(context).doesNotHaveBean(Clock.class);
            assertThat(context).doesNotHaveBean(FixedSchemaVerifier.class);
        });
    }

    @Test
    void failsStartupWithoutASchema() {
        runner.run(context -> assertThat(context).hasFailed());
    }

    @Test
    void failsStartupForASharedReservedOrMalformedSchema() {
        for (String invalid : new String[] {"public", "pg_catalog", "Invalid-Schema", "../../etc"}) {
            runner.withPropertyValues("loadup.database.schema=" + invalid)
                    .run(context -> assertThat(context).hasFailed());
        }
    }

    @Test
    void verifiesThePhysicalConnectionWhenADataSourceIsPresent() {
        DataSource dataSource = Mockito.mock(DataSource.class);
        runner.withPropertyValues("loadup.database.schema=ledger")
                .withBean(DataSource.class, () -> dataSource)
                .run(context -> assertThat(context).hasFailed());
    }

    @Test
    void nonWebDatabaseAssemblyRegistersNoFilters() {
        runner.withPropertyValues("loadup.database.schema=ledger", "loadup.deployment.code=deployment-a")
                .run(context -> {
                    assertThat(context).hasNotFailed();
                    assertThat(context).doesNotHaveBean("tenantFilterRegistration");
                    assertThat(context).doesNotHaveBean(FixedSchemaVerifier.class);
                });
    }
}
