/* Copyright 2026 LoadUp Cloud. Licensed under the Apache License, Version 2.0. */
package io.github.loadup.components.database.autoconfigure;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.loadup.components.database.tenant.DatabaseTenant;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

class DatabaseCoreAutoConfigurationTest {
    private final ApplicationContextRunner runner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(DatabaseCoreAutoConfiguration.class));

    @Test
    void suppliesTenantResolverAndDatabaseClock() {
        runner.run(context -> {
            assertThat(context).hasNotFailed().hasSingleBean(DatabaseTenant.class);
            assertThat(context).hasBean("databaseClock");
            assertThat(context.getBean("databaseClock", Clock.class).getZone()).isEqualTo(java.time.ZoneId.systemDefault());
        });
    }

    @Test
    void usesApplicationSuppliedDatabaseClock() {
        Clock fixed = Clock.fixed(Instant.parse("2026-01-01T00:00:00Z"), ZoneOffset.UTC);
        runner.withBean("databaseClock", Clock.class, () -> fixed).run(context -> {
            assertThat(context).hasNotFailed();
            assertThat(context.getBean("databaseClock", Clock.class)).isSameAs(fixed);
        });
    }
}
