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
package io.github.loadup.components.task;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.github.loadup.components.scheduler.SchedulerAutoConfiguration;
import io.github.loadup.components.scheduler.SchedulerProvider;
import io.github.loadup.components.scheduler.TaskSchedule;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import io.micrometer.observation.ObservationRegistry;
import java.time.Duration;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;
import javax.sql.DataSource;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

class TaskConfigurationTest {
    private final ApplicationContextRunner runner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(TaskAutoConfiguration.class))
            .withPropertyValues("loadup.task.worker-enabled=false");

    static Stream<Consumer<TaskProperties>> invalidSettings() {
        return Stream.of(
                p -> p.setPollInterval(null),
                p -> p.setPollInterval(Duration.ZERO),
                p -> p.setLease(null),
                p -> p.setLease(Duration.ofMillis(-1)),
                p -> p.setRetryDelay(null),
                p -> p.setRetryDelay(Duration.ofNanos(1)),
                p -> p.setRetryDelay(Duration.ofDays(1).plusMillis(1)),
                p -> p.setMaxAttempts(0),
                p -> p.setMaxAttempts(1001),
                p -> p.setBatchSize(0),
                p -> p.setBatchSize(1001));
    }

    @ParameterizedTest
    @MethodSource("invalidSettings")
    void invalidDeliverySettingsFailBeforeWorkerStarts(Consumer<TaskProperties> mutation) {
        TaskProperties settings = new TaskProperties();
        mutation.accept(settings);
        assertThatThrownBy(settings::validate)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Invalid task delivery settings");
    }

    @Test
    void localWorkerDoesNotStartWithoutHandlers() {
        DataSource source = mock(DataSource.class);
        runner.withBean(DataSource.class, () -> source)
                .withBean(PlatformTransactionManager.class, () -> new DataSourceTransactionManager(source))
                .withPropertyValues("loadup.task.worker-enabled=true")
                .run(context -> {
                    assertThat(context).hasNotFailed().hasSingleBean(TaskWorker.class);
                    assertThat(context.getBean(TaskWorker.class).isRunning()).isFalse();
                });
    }

    @Test
    void schedulerRegistersOnlyOneMinuteScannerAndKeepsLocalWorkerStopped() {
        SchedulerProvider provider = mock(SchedulerProvider.class);
        schedulerRunner()
                .withBean(SchedulerProvider.class, () -> provider)
                .withBean(TaskHandler.class, TaskConfigurationTest::handler)
                .run(context -> {
                    assertThat(context).hasNotFailed();
                    TaskWorker worker = context.getBean(TaskWorker.class);
                    worker.start();
                    assertThat(worker.isRunning()).isFalse();
                    var schedule = new TaskSchedule("loadup-task-scan", "0 * * * * ?");
                    verify(provider).start(eq(List.of(schedule)), any());
                });
    }

    @Test
    void schedulerWithoutHandlersDoesNotRegisterAScanner() {
        SchedulerProvider provider = mock(SchedulerProvider.class);
        schedulerRunner().withBean(SchedulerProvider.class, () -> provider).run(context -> {
            assertThat(context).hasNotFailed();
            verify(provider).start(eq(List.of()), any());
            assertThat(context.getBean(TaskWorker.class).isRunning()).isFalse();
        });
    }

    @Test
    void schedulerModeWithHandlersRequiresABinder() {
        schedulerRunner()
                .withBean(TaskHandler.class, TaskConfigurationTest::handler)
                .run(context -> {
                    assertThat(context).hasFailed();
                    assertThat(context.getStartupFailure())
                            .hasRootCauseMessage("loadup.task.trigger=scheduler requires a Scheduler binder");
                });
    }

    private ApplicationContextRunner schedulerRunner() {
        DataSource source = mock(DataSource.class);
        return runner.withConfiguration(AutoConfigurations.of(SchedulerAutoConfiguration.class))
                .withBean(DataSource.class, () -> source)
                .withBean(PlatformTransactionManager.class, () -> new DataSourceTransactionManager(source))
                .withPropertyValues("loadup.task.worker-enabled=true", "loadup.task.trigger=scheduler");
    }

    private static TaskHandler handler() {
        return new TaskHandler() {
            @Override
            public String taskType() {
                return "order.expire";
            }

            @Override
            public void handle(TaskExecution task) {}
        };
    }

    @Test
    void minimumAndMaximumSupportedSettingsAreAccepted() {
        TaskProperties settings = new TaskProperties();
        settings.setPollInterval(Duration.ofMillis(1));
        settings.setLease(Duration.ofMillis(1));
        settings.setRetryDelay(Duration.ofMillis(1));
        settings.setMaxAttempts(1);
        settings.setBatchSize(1);
        assertThatCode(settings::validate).doesNotThrowAnyException();
        assertThat(settings.getPollInterval()).isEqualTo(Duration.ofMillis(1));
        assertThat(settings.getLease()).isEqualTo(Duration.ofMillis(1));
        settings.setRetryDelay(Duration.ofDays(1));
        settings.setMaxAttempts(1000);
        settings.setBatchSize(1000);
        assertThatCode(settings::validate).doesNotThrowAnyException();
        assertThat(settings.getRetryDelay()).isEqualTo(Duration.ofDays(1));
        assertThat(settings.getMaxAttempts()).isEqualTo(1000);
        assertThat(settings.getBatchSize()).isEqualTo(1000);
    }

    @Test
    void disabledOrMissingDataSourceDoesNotExposeTaskBeans() {
        runner.run(context -> assertThat(context).doesNotHaveBean(JdbcTaskRepository.class));
        runner.withBean(DataSource.class, () -> mock(DataSource.class))
                .withPropertyValues("loadup.task.enabled=false")
                .run(context -> {
                    assertThat(context).doesNotHaveBean(TaskDispatcher.class);
                });
    }

    @Test
    void assemblesWithoutObservabilityAndHonorsWorkerSwitch() {
        DataSource source = mock(DataSource.class);
        runner.withBean(DataSource.class, () -> source)
                .withBean(PlatformTransactionManager.class, () -> new DataSourceTransactionManager(source))
                .run(context -> {
                    assertThat(context).hasSingleBean(JdbcTaskRepository.class);
                    assertThat(context).hasSingleBean(TaskDispatcher.class);
                    TaskWorker worker = context.getBean(TaskWorker.class);
                    assertThat(worker.isAutoStartup()).isFalse();
                    assertThat(worker.isRunning()).isFalse();
                });
    }

    @Test
    void rejectsTransactionManagerBoundToAnotherDataSource() {
        DataSource source = mock(DataSource.class);
        DataSource other = mock(DataSource.class);
        runner.withBean(DataSource.class, () -> source)
                .withBean(PlatformTransactionManager.class, () -> new DataSourceTransactionManager(other))
                .run(context -> assertThat(context).hasFailed());
    }

    @Test
    void gaugesUseSharedRegistryAndExistingRepository() {
        DataSource source = mock(DataSource.class);
        JdbcTaskRepository repository = mock(JdbcTaskRepository.class);
        doAnswer(call -> {
                    ((Runnable) call.getArgument(0)).run();
                    return null;
                })
                .when(repository)
                .withoutTransaction(any());
        when(repository.pendingCount()).thenReturn(3L);
        when(repository.failedCount()).thenReturn(2L);
        when(repository.oldestPendingSeconds()).thenReturn(12L);
        SimpleMeterRegistry metrics = new SimpleMeterRegistry();
        ObservationRegistry observations = ObservationRegistry.create();
        runner.withBean(DataSource.class, () -> source)
                .withBean(PlatformTransactionManager.class, () -> new DataSourceTransactionManager(source))
                .withBean(JdbcTaskRepository.class, () -> repository)
                .withBean(SimpleMeterRegistry.class, () -> metrics)
                .withBean(ObservationRegistry.class, () -> observations)
                .run(context -> {
                    assertThat(context.getBean(JdbcTaskRepository.class)).isSameAs(repository);
                    assertThat(context.getBean(ObservationRegistry.class)).isSameAs(observations);
                    assertThat(metrics.get("loadup.task.pending").gauge().value())
                            .isEqualTo(3);
                    assertThat(metrics.get("loadup.task.failed").gauge().value())
                            .isEqualTo(2);
                    assertThat(metrics.get("loadup.task.oldest.age").gauge().value())
                            .isEqualTo(12);
                    when(repository.pendingCount()).thenReturn(0L);
                    assertThat(metrics.get("loadup.task.pending").gauge().value())
                            .isZero();
                });
        metrics.close();
    }
}
