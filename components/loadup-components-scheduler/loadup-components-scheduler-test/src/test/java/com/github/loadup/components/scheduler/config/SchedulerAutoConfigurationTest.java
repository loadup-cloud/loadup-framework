/* Copyright (C) LoadUp Cloud 2022-2025 */
package com.github.loadup.components.scheduler.config;

import com.github.loadup.components.scheduler.api.SchedulerBinder;
import com.github.loadup.components.scheduler.api.SchedulerBinding;
import com.github.loadup.components.scheduler.core.SchedulerTaskRegistry;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit tests for SchedulerAutoConfiguration.
 */
class SchedulerAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
        .withConfiguration(AutoConfigurations.of(SchedulerAutoConfiguration.class));

    @Test
    void testSchedulerTaskRegistryBean() {
        contextRunner.run(context -> {
            assertThat(context).hasSingleBean(SchedulerTaskRegistry.class);
        });
    }

    @Test
    void testSchedulerBindingBean_WhenBinderExists() {
        contextRunner
            .withUserConfiguration(TestBinderConfiguration.class)
            .run(context -> {
                assertThat(context).hasSingleBean(SchedulerBinding.class);
                assertThat(context).hasSingleBean(SchedulerBinder.class);
            });
    }

    @Test
    void testSchedulerBindingBean_WhenNoBinderExists() {
        contextRunner.run(context -> {
            assertThat(context).doesNotHaveBean(SchedulerBinding.class);
        });
    }

    @Configuration
    static class TestBinderConfiguration {
        @Bean
        public SchedulerBinder testSchedulerBinder() {
            SchedulerBinder binder = mock(SchedulerBinder.class);
            when(binder.getName()).thenReturn("test-binder");
            return binder;
        }
    }
}

