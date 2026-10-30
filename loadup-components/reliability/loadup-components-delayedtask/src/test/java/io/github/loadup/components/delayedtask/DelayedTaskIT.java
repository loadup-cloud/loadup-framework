/*
 * #%L
 * LoadUp Components DelayedTask
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
package io.github.loadup.components.delayedtask;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.github.loadup.commons.context.ContextHolder;
import io.github.loadup.commons.context.ContextKeys;
import io.github.loadup.commons.context.ExecutionContext;
import io.github.loadup.commons.error.CommonException;
import io.github.loadup.components.testcontainers.annotation.ContainerType;
import io.github.loadup.components.testcontainers.annotation.EnableTestContainers;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

@SpringBootTest(classes = DelayedTaskIT.Application.class)
@EnableTestContainers(value = ContainerType.POSTGRESQL, reuse = false)
class DelayedTaskIT {
    private static final AtomicInteger calls = new AtomicInteger();
    private static final AtomicInteger failures = new AtomicInteger();
    private final DelayedTaskTemplate tasks;
    private final DelayedTaskStore store;
    private final DelayedTaskDispatcher dispatcher;
    private final DelayedTaskProperties properties;
    private final MutableClock clock;
    private final TransactionTemplate transactions;
    private final JdbcTemplate jdbc;

    @Autowired
    DelayedTaskIT(
            DelayedTaskTemplate tasks,
            DelayedTaskStore store,
            DelayedTaskDispatcher dispatcher,
            DelayedTaskProperties properties,
            MutableClock clock,
            PlatformTransactionManager manager,
            JdbcTemplate jdbc) {
        this.tasks = tasks;
        this.store = store;
        this.dispatcher = dispatcher;
        this.properties = properties;
        this.clock = clock;
        this.transactions = new TransactionTemplate(manager);
        this.jdbc = jdbc;
    }

    @BeforeEach
    void clear() {
        jdbc.update("DELETE FROM delayed_task");
        calls.set(0);
        failures.set(0);
        properties.setMaxAttempts(2);
    }

    private <T> T tenant(String tenant, java.util.function.Supplier<T> action) {
        return ContextHolder.callWith(ExecutionContext.empty().with(ContextKeys.TENANT_ID, tenant), action::get);
    }

    private DelayedTask register(String id, LocalDateTime time) {
        return transactions.execute(status -> tasks.register("close-order", id, time));
    }

    private DelayedTask get(String id) {
        return tasks.find("close-order", id).orElseThrow();
    }

    private LocalDateTime now() {
        return LocalDateTime.now(clock).withNano(0);
    }

    @Test
    void dispatcherProxySuspendsAndRestoresTheCallerTransaction() {
        assertThat(org.springframework.aop.support.AopUtils.isAopProxy(dispatcher))
                .isTrue();
        register("transaction-suspension", now());
        transactions.executeWithoutResult(
                status -> {
                    assertThat(org.springframework.transaction.support.TransactionSynchronizationManager
                                    .isActualTransactionActive())
                            .isTrue();
                    dispatcher.execute();
                    assertThat(org.springframework.transaction.support.TransactionSynchronizationManager
                                    .isActualTransactionActive())
                            .isTrue();
                });
        assertThat(calls).hasValue(1);
        assertThat(get("transaction-suspension").status()).isEqualTo(DelayedTaskStatus.SUCCEEDED);
    }

    @Test
    void deadlineTransactionDeduplication() {
        assertThatThrownBy(() -> tenant("a", () -> tasks.register("close-order", "no-tx", now())))
                .isInstanceOf(CommonException.class);
        tenant(
                "a",
                () -> transactions.execute(status -> {
                    tasks.register("close-order", "rollback", now());
                    status.setRollbackOnly();
                    return null;
                }));
        assertThat(tenant("a", () -> tasks.find("close-order", "rollback"))).isEmpty();
        var deadline = now().plusDays(30);
        var first = register("order", deadline);
        assertThat(register("order", deadline).id()).isEqualTo(first.id());
        assertThatThrownBy(() -> register("order", deadline.plusSeconds(1))).isInstanceOf(CommonException.class);
        dispatcher.execute();
        assertThat(calls).hasValue(0);
        clock.advance(Duration.ofDays(30).plusSeconds(2));
        dispatcher.execute();
        assertThat(calls).hasValue(2);
        assertThat(get("order").status()).isEqualTo(DelayedTaskStatus.SUCCEEDED);
        assertThat(first.toString()).contains("order");
    }

    @Test
    void duplicateRegistrationLeavesTheEnclosingPostgresqlTransactionUsable() {
        tenant(
                "a",
                () -> transactions.execute(status -> {
                    var first = tasks.register("close-order", "duplicate", now());
                    assertThat(tasks.register("close-order", "duplicate", now()).id())
                            .isEqualTo(first.id());
                    tasks.register("close-order", "after-duplicate", now());
                    return null;
                }));
        assertThat(get("after-duplicate").status()).isEqualTo(DelayedTaskStatus.PENDING);
    }

    @Test
    void retryDelayFinalFailureAndExplicitRecovery() {
        failures.set(5);
        register("retry", now());
        dispatcher.execute();
        assertThat(get("retry").attempts()).isEqualTo(1);
        assertThat(get("retry").status()).isEqualTo(DelayedTaskStatus.PENDING);
        dispatcher.execute();
        assertThat(calls).hasValue(1);
        clock.advance(properties.getRetryDelay().plusSeconds(1));
        dispatcher.execute();
        assertThat(get("retry").status()).isEqualTo(DelayedTaskStatus.FAILED);
        tenant("a", () -> {
            tasks.retryFailed("close-order", "retry");
            return null;
        });
        failures.set(0);
        dispatcher.execute();
        assertThat(get("retry").status()).isEqualTo(DelayedTaskStatus.SUCCEEDED);
        assertThatThrownBy(() -> tenant("a", () -> {
                    tasks.retryFailed("close-order", "retry");
                    return null;
                }))
                .isInstanceOf(CommonException.class);
    }

    @Test
    void cancellationRescheduleAndStaleCompletionAreFenced() {
        register("cancel", now());
        var old = store.claim(LocalDateTime.now(clock), properties).orElseThrow();
        assertThat(tenant("a", () -> tasks.cancel("close-order", "cancel"))).isTrue();
        assertThat(store.finish(old, LocalDateTime.now(clock), true, properties))
                .isFalse();
        tenant("a", () -> {
            tasks.reschedule("close-order", "cancel", now().plusDays(1));
            return null;
        });
        dispatcher.execute();
        assertThat(calls).hasValue(0);
        clock.advance(Duration.ofDays(1).plusSeconds(2));
        dispatcher.execute();
        assertThat(get("cancel").status()).isEqualTo(DelayedTaskStatus.SUCCEEDED);
        assertThat(tenant("a", () -> tasks.cancel("close-order", "cancel"))).isFalse();
    }

    @Test
    void expiredLeasesRecoverAndOldOwnersCannotAcknowledge() {
        register("lease", now());
        var first = store.claim(LocalDateTime.now(clock), properties).orElseThrow();
        assertThat(store.claim(LocalDateTime.now(clock), properties)).isEmpty();
        clock.advance(properties.getLease().plusSeconds(1));
        var second = store.claim(LocalDateTime.now(clock), properties).orElseThrow();
        assertThat(second.getAttempts()).isEqualTo(2);
        assertThat(store.finish(first, LocalDateTime.now(clock), true, properties))
                .isFalse();
        assertThat(store.finish(second, LocalDateTime.now(clock), true, properties))
                .isTrue();
        register("exhausted", now());
        store.claim(LocalDateTime.now(clock), properties).orElseThrow();
        clock.advance(properties.getLease().plusSeconds(1));
        store.claim(LocalDateTime.now(clock), properties).orElseThrow();
        clock.advance(properties.getLease().plusSeconds(1));
        store.recoverExhausted(LocalDateTime.now(clock));
        assertThat(store.claim(LocalDateTime.now(clock), properties)).isEmpty();
        assertThat(get("exhausted").status()).isEqualTo(DelayedTaskStatus.FAILED);
    }

    @Test
    void concurrentWorkersClaimOnce() throws Exception {
        register("concurrent", now());
        var ready = new CountDownLatch(2);
        var start = new CountDownLatch(1);
        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            Callable<Boolean> claim = () -> {
                ready.countDown();
                if (!start.await(5, TimeUnit.SECONDS)) {
                    throw new AssertionError("Timeout");
                }
                return store.claim(LocalDateTime.now(clock), properties).isPresent();
            };
            var a = executor.submit(claim);
            var b = executor.submit(claim);
            assertThat(ready.await(5, TimeUnit.SECONDS)).isTrue();
            start.countDown();
            assertThat(List.of(a.get(10, TimeUnit.SECONDS), b.get(10, TimeUnit.SECONDS)))
                    .containsExactlyInAnyOrder(true, false);
        } finally {
            start.countDown();
        }
    }

    @Test
    void lockedDueTaskDoesNotBlockClaimOfAnotherTask() throws Exception {
        register("locked", now().minusSeconds(2));
        register("available", now().minusSeconds(1));
        var locked = new CountDownLatch(1);
        var release = new CountDownLatch(1);
        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            var holder = executor.submit(() -> transactions.executeWithoutResult(status -> {
                jdbc.queryForObject("SELECT id FROM delayed_task WHERE biz_id = ? FOR UPDATE", String.class, "locked");
                locked.countDown();
                try {
                    if (!release.await(5, TimeUnit.SECONDS)) {
                        throw new AssertionError("Timeout waiting for release");
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new IllegalStateException(e);
                }
            }));
            try {
                assertThat(locked.await(5, TimeUnit.SECONDS)).isTrue();
                var claim = executor.submit(() -> store.claim(LocalDateTime.now(clock), properties));
                assertThat(claim.get(2, TimeUnit.SECONDS).orElseThrow().getBizId())
                        .isEqualTo("available");
            } finally {
                release.countDown();
                holder.get(5, TimeUnit.SECONDS);
            }
        }
    }

    @Test
    void invalidArgumentsAndMissingHandlerFailSafely() {
        assertThatThrownBy(() -> tenant("a", () -> tasks.find("bad name", "order")))
                .isInstanceOf(CommonException.class);
        assertThatThrownBy(() -> tasks.find("close-order", "order")).isInstanceOf(CommonException.class);
        tenant("a", () -> transactions.execute(status -> tasks.register("missing-handler", "missing", now())));
        dispatcher.execute();
        assertThat(tenant(
                        "a",
                        () -> tasks.find("missing-handler", "missing")
                                .orElseThrow()
                                .status()))
                .isEqualTo(DelayedTaskStatus.PENDING);
        assertThat(calls).hasValue(0);
        assertThat(ContextHolder.isBound()).isFalse();
    }

    static class MutableClock extends Clock {
        private final AtomicReference<Instant> instant = new AtomicReference<>(Instant.now());

        public ZoneId getZone() {
            return ZoneId.systemDefault();
        }

        public Clock withZone(ZoneId zone) {
            return Clock.fixed(instant(), zone);
        }

        public Instant instant() {
            return instant.get();
        }

        void advance(Duration duration) {
            instant.updateAndGet(now -> now.plus(duration));
        }
    }

    @SpringBootConfiguration
    @EnableAutoConfiguration
    static class Application {
        @Bean("delayedTaskClock")
        MutableClock clock() {
            return new MutableClock();
        }

        @Bean
        DelayedTaskHandler closeOrder() {
            return new DelayedTaskHandler() {
                public String taskType() {
                    return "close-order";
                }

                public void handle(DelayedTask task) {
                    assertThat(org.springframework.transaction.support.TransactionSynchronizationManager
                                    .isActualTransactionActive())
                            .isFalse();
                    calls.incrementAndGet();
                    if (failures.getAndDecrement() > 0) {
                        throw new IllegalStateException("Synthetic failure");
                    }
                }
            };
        }
    }
}
