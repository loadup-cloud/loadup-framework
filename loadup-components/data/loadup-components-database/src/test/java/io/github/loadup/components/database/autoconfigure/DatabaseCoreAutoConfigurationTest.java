/*
 * Copyright (C) 2025-2026 LoadUp Cloud
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

import io.github.loadup.commons.time.autoconfigure.ClockAutoConfiguration;
import io.github.loadup.components.database.tenant.DatabaseTenant;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

class DatabaseCoreAutoConfigurationTest {
    private final ApplicationContextRunner runner = new ApplicationContextRunner()
            .withConfiguration(
                    AutoConfigurations.of(DatabaseCoreAutoConfiguration.class, ClockAutoConfiguration.class));

    @Test
    void suppliesTenantResolverAndClock() {
        runner.run(context -> {
            assertThat(context).hasNotFailed().hasSingleBean(DatabaseTenant.class);
            assertThat(context).hasSingleBean(Clock.class);
            assertThat(context.getBean("clock", Clock.class).getZone()).isEqualTo(ZoneId.systemDefault());
        });
    }

    @Test
    void bindsDatabaseSchemaIndependentlyOfMultiTenancy() {
        runner.withPropertyValues("loadup.database.default-schema=ledger", "loadup.database.multi-tenant.enabled=false")
                .run(context -> {
                    assertThat(context).hasNotFailed();
                    assertThat(context.getBean(DatabaseTenant.class).currentSchema())
                            .isEqualTo("ledger");
                });
    }

    @Test
    void usesApplicationSuppliedClock() {
        Clock fixed = Clock.fixed(Instant.parse("2026-01-01T00:00:00Z"), ZoneOffset.UTC);
        runner.withBean("clock", Clock.class, () -> fixed).run(context -> {
            assertThat(context).hasNotFailed().hasSingleBean(Clock.class);
            assertThat(context.getBean(Clock.class)).isSameAs(fixed);
        });
    }
}
