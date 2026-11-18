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
package io.github.loadup.components.scheduler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.github.loadup.commons.context.ContextHolder;
import io.github.loadup.commons.context.ContextKey;
import io.github.loadup.commons.context.ExecutionContext;
import io.github.loadup.commons.error.CommonException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

class SchedulerTemplateTest {
    @Test
    void autoConfigurationSelectsOneProviderAndRejectsAmbiguity() {
        var runner = new ApplicationContextRunner()
                .withConfiguration(AutoConfigurations.of(SchedulerAutoConfiguration.class));
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
    void bindsConfiguredSchedulesAndDispatchesOnlyTheirTaskNames() throws Exception {
        var provider = mock(SchedulerProvider.class);
        var configured = mock(ScheduledTask.class);
        var manual = mock(ScheduledTask.class);
        when(configured.name()).thenReturn("configured");
        when(manual.name()).thenReturn("manual");
        var calls = ArgumentCaptor.forClass(SchedulerProvider.TaskTrigger.class);
        new ApplicationContextRunner()
                .withConfiguration(AutoConfigurations.of(SchedulerAutoConfiguration.class))
                .withBean(SchedulerProvider.class, () -> provider)
                .withBean("configured", ScheduledTask.class, () -> configured)
                .withBean("manual", ScheduledTask.class, () -> manual)
                .withPropertyValues(
                        "loadup.scheduler.tasks[0].name=configured",
                        "loadup.scheduler.tasks[0].cron=0 15 9 * * mon-fri")
                .run(context -> {
                    verify(provider)
                            .start(eq(List.of(new TaskSchedule("configured", "0 15 9 ? * MON-FRI"))), calls.capture());
                    calls.getValue().trigger("configured");
                    verify(configured).execute();
                    assertThatThrownBy(() -> calls.getValue().trigger("manual")).isInstanceOf(CommonException.class);
                    context.getBean(SchedulerTemplate.class).trigger("manual");
                    verify(manual).execute();
                });
    }

    @Test
    void rejectsMissingBinderUnknownDuplicateAndInvalidSchedules() {
        var runner = new ApplicationContextRunner()
                .withConfiguration(AutoConfigurations.of(SchedulerAutoConfiguration.class));
        runner.withPropertyValues("loadup.scheduler.binder-type=missing")
                .run(context -> assertThat(context).hasFailed());

        var task = mock(ScheduledTask.class);
        when(task.name()).thenReturn("configured");
        var configured = runner.withBean(SchedulerProvider.class, () -> mock(SchedulerProvider.class))
                .withBean(ScheduledTask.class, () -> task)
                .withPropertyValues("loadup.scheduler.tasks[0].name=configured");
        configured
                .withPropertyValues("loadup.scheduler.tasks[0].cron=bad")
                .run(context -> assertThat(context).hasFailed());
        configured
                .withPropertyValues(
                        "loadup.scheduler.tasks[0].cron=0 * * * * *",
                        "loadup.scheduler.tasks[1].name=missing",
                        "loadup.scheduler.tasks[1].cron=0 * * * * *")
                .run(context -> assertThat(context).hasFailed());
        configured
                .withPropertyValues(
                        "loadup.scheduler.tasks[0].cron=0 * * * * *",
                        "loadup.scheduler.tasks[1].name=configured",
                        "loadup.scheduler.tasks[1].cron=0 * * * * *")
                .run(context -> assertThat(context).hasFailed());
        assertThatThrownBy(() -> new TaskSchedule("configured", "0 0 9 ? * 1"))
                .isInstanceOf(IllegalArgumentException.class);
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
        verify(provider).start(eq(List.of()), any(SchedulerProvider.TaskTrigger.class));
        assertThat(scheduler.isRunning()).isTrue();
        scheduler.trigger("test.scan");
        verify(task).execute();
        scheduler.stop();
        scheduler.stop();
        verify(provider).stop();
        assertThatThrownBy(() -> scheduler.trigger("missing")).isInstanceOf(CommonException.class);
        assertThatThrownBy(() -> new SchedulerTemplate(provider, List.of(task, task)))
                .isInstanceOf(CommonException.class);
        for (String invalid : Arrays.asList(null, "", "bad name")) {
            when(task.name()).thenReturn(invalid);
            assertThatThrownBy(() -> new SchedulerTemplate(provider, List.of(task)))
                    .isInstanceOf(CommonException.class);
        }
    }

    @Test
    void overlappingWakeUpsAreSuppressedAndFailureReleasesGuard() throws Exception {
        var entered = new CountDownLatch(1);
        var release = new CountDownLatch(1);
        var calls = new AtomicInteger();
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

    @Test
    void triggerKeepsTheCallerContextAndRestoresItAfterSuccessAndFailure() throws Exception {
        var metadataKey = new ContextKey<String>("test.metadata", String.class);
        var outer = ExecutionContext.empty().with(metadataKey, "outer-metadata");
        var failure = new IllegalStateException("synthetic");
        var scheduler = new SchedulerTemplate(
                mock(SchedulerProvider.class),
                List.of(
                        new ScheduledTask() {
                            public String name() {
                                return "success";
                            }

                            public void execute() {
                                assertThat(ContextHolder.get(metadataKey)).isEqualTo("outer-metadata");
                            }
                        },
                        new ScheduledTask() {
                            public String name() {
                                return "failure";
                            }

                            public void execute() {
                                assertThat(ContextHolder.get(metadataKey)).isEqualTo("outer-metadata");
                                throw failure;
                            }
                        }));

        ContextHolder.runWith(outer, () -> {
            assertThatCode(() -> scheduler.trigger("success")).doesNotThrowAnyException();
            assertThat(ContextHolder.current()).isSameAs(outer);
            assertThatThrownBy(() -> scheduler.trigger("failure")).isSameAs(failure);
            assertThat(ContextHolder.current()).isSameAs(outer);
            assertThat(ContextHolder.get(metadataKey)).isEqualTo("outer-metadata");
        });
    }
}
