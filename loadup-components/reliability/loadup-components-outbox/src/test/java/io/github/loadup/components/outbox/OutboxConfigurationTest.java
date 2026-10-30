/*
 * #%L
 * LoadUp Outbox
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
package io.github.loadup.components.outbox;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import io.micrometer.observation.ObservationRegistry;
import java.time.Duration;
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

class OutboxConfigurationTest {
    private final ApplicationContextRunner runner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(OutboxAutoConfiguration.class))
            .withPropertyValues("loadup.outbox.worker-enabled=false");

    static Stream<Consumer<OutboxProperties>> invalidSettings() {
        return Stream.of(
                p -> p.setPollInterval(null),
                p -> p.setPollInterval(Duration.ZERO),
                p -> p.setLease(null),
                p -> p.setLease(Duration.ofMillis(-1)),
                p -> p.setRetryDelay(null),
                p -> p.setRetryDelay(Duration.ofNanos(1)),
                p -> p.setRetryDelay(Duration.ofDays(1).plusMillis(1)),
                p -> p.setMaxAttempts(0),
                p -> p.setMaxAttempts(101),
                p -> p.setBatchSize(0),
                p -> p.setBatchSize(1001));
    }

    @ParameterizedTest
    @MethodSource("invalidSettings")
    void invalidDeliverySettingsFailBeforeWorkerStarts(Consumer<OutboxProperties> mutation) {
        OutboxProperties settings = new OutboxProperties();
        mutation.accept(settings);
        assertThatThrownBy(settings::validate)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Invalid outbox delivery settings");
    }

    @Test
    void minimumAndMaximumSupportedSettingsAreAccepted() {
        OutboxProperties settings = new OutboxProperties();
        settings.setPollInterval(Duration.ofMillis(1));
        settings.setLease(Duration.ofMillis(1));
        settings.setRetryDelay(Duration.ofMillis(1));
        settings.setMaxAttempts(1);
        settings.setBatchSize(1);
        assertThatCode(settings::validate).doesNotThrowAnyException();
        assertThat(settings.getPollInterval()).isEqualTo(Duration.ofMillis(1));
        assertThat(settings.getLease()).isEqualTo(Duration.ofMillis(1));
        settings.setRetryDelay(Duration.ofDays(1));
        settings.setMaxAttempts(100);
        settings.setBatchSize(1000);
        assertThatCode(settings::validate).doesNotThrowAnyException();
        assertThat(settings.getRetryDelay()).isEqualTo(Duration.ofDays(1));
        assertThat(settings.getMaxAttempts()).isEqualTo(100);
        assertThat(settings.getBatchSize()).isEqualTo(1000);
        settings.setWorkerEnabled(true);
        assertThat(new OutboxWorker(mock(OutboxDispatcher.class), settings).isAutoStartup())
                .isTrue();
    }

    @Test
    void disabledOrMissingDataSourceDoesNotExposeOutboxBeans() {
        runner.run(context -> assertThat(context).doesNotHaveBean(JdbcOutboxRepository.class));
        runner.withBean(DataSource.class, () -> mock(DataSource.class))
                .withPropertyValues("loadup.outbox.enabled=false")
                .run(context -> {
                    assertThat(context).doesNotHaveBean(OutboxDispatcher.class);
                    assertThat(context).doesNotHaveBean(OutboxInbox.class);
                });
    }

    @Test
    void assemblesWithoutObservabilityAndHonorsWorkerSwitch() {
        DataSource source = mock(DataSource.class);
        runner.withBean(DataSource.class, () -> source)
                .withBean(PlatformTransactionManager.class, () -> new DataSourceTransactionManager(source))
                .run(context -> {
                    assertThat(context).hasSingleBean(JdbcOutboxRepository.class);
                    assertThat(context).hasSingleBean(OutboxInbox.class);
                    assertThat(context).hasSingleBean(OutboxDispatcher.class);
                    OutboxWorker worker = context.getBean(OutboxWorker.class);
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
        JdbcOutboxRepository repository = mock(JdbcOutboxRepository.class);
        when(repository.pendingCount()).thenReturn(3L);
        when(repository.failedCount()).thenReturn(2L);
        when(repository.oldestPendingSeconds()).thenReturn(12L);
        SimpleMeterRegistry metrics = new SimpleMeterRegistry();
        ObservationRegistry observations = ObservationRegistry.create();
        runner.withBean(DataSource.class, () -> source)
                .withBean(PlatformTransactionManager.class, () -> new DataSourceTransactionManager(source))
                .withBean(JdbcOutboxRepository.class, () -> repository)
                .withBean(SimpleMeterRegistry.class, () -> metrics)
                .withBean(ObservationRegistry.class, () -> observations)
                .run(context -> {
                    assertThat(context.getBean(JdbcOutboxRepository.class)).isSameAs(repository);
                    assertThat(context.getBean(ObservationRegistry.class)).isSameAs(observations);
                    assertThat(metrics.get("loadup.outbox.pending").gauge().value())
                            .isEqualTo(3);
                    assertThat(metrics.get("loadup.outbox.failed").gauge().value())
                            .isEqualTo(2);
                    assertThat(metrics.get("loadup.outbox.oldest.age").gauge().value())
                            .isEqualTo(12);
                    when(repository.pendingCount()).thenReturn(0L);
                    assertThat(metrics.get("loadup.outbox.pending").gauge().value())
                            .isZero();
                });
        metrics.close();
    }
}
