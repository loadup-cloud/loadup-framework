package com.github.loadup.components.scheduler.config;

/*-
 * #%L
 * loadup-components-scheduler-test
 * %%
 * Copyright (C) 2025 LoadUp Cloud
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.github.loadup.components.scheduler.api.SchedulerBinder;
import com.github.loadup.components.scheduler.api.SchedulerBinding;
import com.github.loadup.components.scheduler.core.SchedulerTaskRegistry;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

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

