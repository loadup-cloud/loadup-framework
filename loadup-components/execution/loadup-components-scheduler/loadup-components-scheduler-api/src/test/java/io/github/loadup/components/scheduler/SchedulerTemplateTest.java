/*
 * #%L
 * LoadUp Components Scheduler
 * %%
 * Copyright (C) 2025 - 2026 LoadUp Cloud
 * %%
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
 * #L%
 */
package io.github.loadup.components.scheduler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;

class SchedulerTemplateTest {
    @Test
    void autoConfigurationSelectsOneProviderAndRejectsAmbiguity() {
        var runner = new org.springframework.boot.test.context.runner.ApplicationContextRunner()
                .withConfiguration(
                        org.springframework.boot.autoconfigure.AutoConfigurations.of(SchedulerAutoConfiguration.class));
        runner.run(context -> assertThat(context).doesNotHaveBean(SchedulerTemplate.class));
        runner.withBean("first", SchedulerProvider.class, () -> mock(SchedulerProvider.class))
                .run(context -> assertThat(
                                context.getBean(SchedulerTemplate.class).taskNames())
                        .isEmpty());
        runner.withBean("first", SchedulerProvider.class, () -> mock(SchedulerProvider.class))
                .withBean("second", SchedulerProvider.class, () -> mock(SchedulerProvider.class))
                .run(context -> assertThat(context).hasFailed());
    }

    @Test
    void lifecycleTriggerAndValidation() throws Exception {
        var provider = mock(SchedulerProvider.class);
        var task = mock(ScheduledTask.class);
        when(task.name()).thenReturn("test.scan");
        var scheduler = new SchedulerTemplate(provider, List.of(task));
        assertThat(scheduler.isRunning()).isFalse();
        scheduler.start();
        scheduler.start();
        verify(provider).start(scheduler);
        assertThat(scheduler.isRunning()).isTrue();
        scheduler.trigger("test.scan");
        verify(task).execute();
        scheduler.stop();
        scheduler.stop();
        verify(provider).stop();
        assertThatThrownBy(() -> scheduler.trigger("missing"))
                .isInstanceOf(io.github.loadup.commons.error.CommonException.class);
        assertThatThrownBy(() -> new SchedulerTemplate(provider, List.of(task, task)))
                .isInstanceOf(io.github.loadup.commons.error.CommonException.class);
        for (String invalid : Arrays.asList(null, "", "bad name")) {
            when(task.name()).thenReturn(invalid);
            assertThatThrownBy(() -> new SchedulerTemplate(provider, List.of(task)))
                    .isInstanceOf(io.github.loadup.commons.error.CommonException.class);
        }
    }

    @Test
    void overlappingWakeUpsAreSuppressedAndFailureReleasesGuard() throws Exception {
        var entered = new CountDownLatch(1);
        var release = new CountDownLatch(1);
        var calls = new java.util.concurrent.atomic.AtomicInteger();
        ScheduledTask task = new ScheduledTask() {
            public String name() {
                return "test";
            }

            public void execute() throws Exception {
                calls.incrementAndGet();
                entered.countDown();
                if (!release.await(5, TimeUnit.SECONDS)) {
                    throw new AssertionError("Timeout");
                }
            }
        };
        var scheduler = new SchedulerTemplate(mock(SchedulerProvider.class), List.of(task));
        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            var first = executor.submit(() -> {
                scheduler.trigger("test");
                return null;
            });
            assertThat(entered.await(5, TimeUnit.SECONDS)).isTrue();
            scheduler.trigger("test");
            assertThat(calls).hasValue(1);
            release.countDown();
            first.get(5, TimeUnit.SECONDS);
        } finally {
            release.countDown();
        }
        scheduler.trigger("test");
        assertThat(calls).hasValue(2);
        var failing = mock(ScheduledTask.class);
        when(failing.name()).thenReturn("failed");
        doThrow(new Exception("synthetic")).doNothing().when(failing).execute();
        var retried = new SchedulerTemplate(mock(SchedulerProvider.class), List.of(failing));
        assertThatThrownBy(() -> retried.trigger("failed")).isInstanceOf(Exception.class);
        retried.trigger("failed");
        verify(failing, times(2)).execute();
    }
}
