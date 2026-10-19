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

import static io.github.loadup.components.delayedtask.dataobject.table.Tables.*;
import static org.assertj.core.api.Assertions.*;

import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.tenant.TenantManager;
import io.github.loadup.commons.context.*;
import io.github.loadup.commons.error.CommonException;
import io.github.loadup.components.delayedtask.mapper.DelayedTaskDOMapper;
import io.github.loadup.components.testcontainers.annotation.*;
import java.time.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

@SpringBootTest(classes = DelayedTaskIT.Application.class)
@EnableTestContainers(value = ContainerType.MYSQL, reuse = false)
class DelayedTaskIT {
    private final DelayedTaskTemplate tasks;
    private final DelayedTaskStore store;
    private final DelayedTaskDispatcher dispatcher;
    private final DelayedTaskProperties properties;
    private final MutableClock clock;
    private final TransactionTemplate transactions;
    private final DelayedTaskDOMapper mapper;
    private static final AtomicInteger calls = new AtomicInteger();
    private static final AtomicInteger failures = new AtomicInteger();

    @Autowired
    DelayedTaskIT(
            DelayedTaskTemplate tasks,
            DelayedTaskStore store,
            DelayedTaskDispatcher dispatcher,
            DelayedTaskProperties properties,
            MutableClock clock,
            PlatformTransactionManager manager,
            DelayedTaskDOMapper mapper) {
        this.tasks = tasks;
        this.store = store;
        this.dispatcher = dispatcher;
        this.properties = properties;
        this.clock = clock;
        this.transactions = new TransactionTemplate(manager);
        this.mapper = mapper;
    }

    @BeforeEach
    void clear() {
        TenantManager.withoutTenantCondition(() -> mapper.deleteByQuery(
                QueryWrapper.create().from(DELAYED_TASK_DO).where(DELAYED_TASK_DO.ID.isNotNull())));
        calls.set(0);
        failures.set(0);
        properties.setMaxAttempts(2);
    }

    private <T> T tenant(String tenant, java.util.function.Supplier<T> action) {
        return ContextHolder.callWith(ExecutionContext.empty().with(ContextKeys.TENANT_ID, tenant), action::get);
    }

    private DelayedTask register(String tenant, String id, LocalDateTime time) {
        return tenant(tenant, () -> transactions.execute(status -> tasks.register("close-order", id, time)));
    }

    private DelayedTask get(String tenant, String id) {
        return tenant(tenant, () -> tasks.find("close-order", id).orElseThrow());
    }

    private LocalDateTime now() {
        return LocalDateTime.now(clock).withNano(0);
    }

    @Test
    void deadlineTransactionDeduplicationAndTenantIsolation() {
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
        var first = register("a", "order", deadline);
        assertThat(register("a", "order", deadline).id()).isEqualTo(first.id());
        assertThatThrownBy(() -> register("a", "order", deadline.plusSeconds(1)))
                .isInstanceOf(CommonException.class);
        assertThat(register("b", "order", deadline).id()).isNotEqualTo(first.id());
        dispatcher.execute();
        assertThat(calls).hasValue(0);
        clock.advance(Duration.ofDays(30).plusSeconds(2));
        dispatcher.execute();
        assertThat(calls).hasValue(2);
        assertThat(get("a", "order").status()).isEqualTo(DelayedTaskStatus.SUCCEEDED);
        assertThat(first.toString()).contains("order");
    }

    @Test
    void retryDelayFinalFailureAndExplicitRecovery() {
        failures.set(5);
        register("a", "retry", now());
        dispatcher.execute();
        assertThat(get("a", "retry").attempts()).isEqualTo(1);
        assertThat(get("a", "retry").status()).isEqualTo(DelayedTaskStatus.PENDING);
        dispatcher.execute();
        assertThat(calls).hasValue(1);
        clock.advance(properties.getRetryDelay().plusSeconds(1));
        dispatcher.execute();
        assertThat(get("a", "retry").status()).isEqualTo(DelayedTaskStatus.FAILED);
        tenant("a", () -> {
            tasks.retryFailed("close-order", "retry");
            return null;
        });
        failures.set(0);
        dispatcher.execute();
        assertThat(get("a", "retry").status()).isEqualTo(DelayedTaskStatus.SUCCEEDED);
        assertThatThrownBy(() -> tenant("a", () -> {
                    tasks.retryFailed("close-order", "retry");
                    return null;
                }))
                .isInstanceOf(CommonException.class);
    }

    @Test
    void cancellationRescheduleAndStaleCompletionAreFenced() {
        register("a", "cancel", now());
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
        assertThat(get("a", "cancel").status()).isEqualTo(DelayedTaskStatus.SUCCEEDED);
        assertThat(tenant("a", () -> tasks.cancel("close-order", "cancel"))).isFalse();
    }

    @Test
    void expiredLeasesRecoverAndOldOwnersCannotAcknowledge() {
        register("a", "lease", now());
        var first = store.claim(LocalDateTime.now(clock), properties).orElseThrow();
        assertThat(store.claim(LocalDateTime.now(clock), properties)).isEmpty();
        clock.advance(properties.getLease().plusSeconds(1));
        var second = store.claim(LocalDateTime.now(clock), properties).orElseThrow();
        assertThat(second.getAttempts()).isEqualTo(2);
        assertThat(store.finish(first, LocalDateTime.now(clock), true, properties))
                .isFalse();
        assertThat(store.finish(second, LocalDateTime.now(clock), true, properties))
                .isTrue();
        register("a", "exhausted", now());
        store.claim(LocalDateTime.now(clock), properties).orElseThrow();
        clock.advance(properties.getLease().plusSeconds(1));
        store.claim(LocalDateTime.now(clock), properties).orElseThrow();
        clock.advance(properties.getLease().plusSeconds(1));
        store.recoverExhausted(LocalDateTime.now(clock));
        assertThat(store.claim(LocalDateTime.now(clock), properties)).isEmpty();
        assertThat(get("a", "exhausted").status()).isEqualTo(DelayedTaskStatus.FAILED);
    }

    @Test
    void concurrentWorkersClaimOnce() throws Exception {
        register("a", "concurrent", now());
        var ready = new CountDownLatch(2);
        var start = new CountDownLatch(1);
        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            Callable<Boolean> claim = () -> {
                ready.countDown();
                if (!start.await(5, TimeUnit.SECONDS)) throw new AssertionError("Timeout");
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
                    assertThat(ContextHolder.get(ContextKeys.TENANT_ID)).isEqualTo(task.tenantId());
                    calls.incrementAndGet();
                    if (failures.getAndDecrement() > 0) throw new IllegalStateException("Synthetic failure");
                }
            };
        }
    }
}
