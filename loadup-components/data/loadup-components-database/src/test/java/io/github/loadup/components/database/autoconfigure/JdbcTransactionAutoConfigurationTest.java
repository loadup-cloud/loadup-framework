/* Copyright 2026 LoadUp Cloud. Licensed under the Apache License, Version 2.0. */
package io.github.loadup.components.database.autoconfigure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import javax.sql.DataSource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.jdbc.support.JdbcTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

class JdbcTransactionAutoConfigurationTest {
    private final ApplicationContextRunner runner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(JdbcTransactionAutoConfiguration.class));

    @Test
    void createsManagerForSingleDataSource() {
        var source = mock(DataSource.class);
        runner.withBean(DataSource.class, () -> source).run(context -> {
            assertThat(context).hasNotFailed().hasSingleBean(JdbcTransactionManager.class);
            assertThat(context.getBean(JdbcTransactionManager.class).getDataSource()).isSameAs(source);
        });
    }

    @Test
    void respectsApplicationTransactionManager() {
        var manager = mock(PlatformTransactionManager.class);
        runner.withBean(DataSource.class, () -> mock(DataSource.class))
                .withBean(PlatformTransactionManager.class, () -> manager)
                .run(context -> {
                    assertThat(context).hasNotFailed().doesNotHaveBean(JdbcTransactionManager.class);
                    assertThat(context.getBean(PlatformTransactionManager.class)).isSameAs(manager);
                });
    }

    @Test
    void doesNotChooseAmongMultipleDataSources() {
        runner.withBean("first", DataSource.class, () -> mock(DataSource.class))
                .withBean("second", DataSource.class, () -> mock(DataSource.class))
                .run(context -> assertThat(context).hasNotFailed().doesNotHaveBean(JdbcTransactionManager.class));
    }
}
