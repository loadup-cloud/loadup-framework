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
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.github.loadup.commons.util.TenantUtil;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;

class OutboxWorkerTest {
    private static OutboxProperties properties() {
        OutboxProperties properties = new OutboxProperties();
        properties.setBatchSize(1);
        return properties;
    }

    private static JdbcOutboxRepository.Claim claim(String traceId) {
        return new JdbcOutboxRepository.Claim(
                new OutboxEvent(
                        "event-id",
                        new OutboxMessage("payment.completed", 1, "order-id", "{}"),
                        Instant.parse("2026-10-14T00:00:00Z"),
                        traceId),
                "lease-token",
                1);
    }

    private static OutboxHandler handler(HandlerAction action) {
        return new OutboxHandler() {
            public String eventType() {
                return "payment.completed";
            }

            public void handle(OutboxEvent event) throws Exception {
                action.handle(event);
            }
        };
    }

    @AfterEach
    void clearThreadState() {
        MDC.clear();
        Thread.interrupted();
    }

    @Test
    void successfulDispatchPreservesCallerTenantAndRestoresTraceContext() {
        JdbcOutboxRepository repository = mock(JdbcOutboxRepository.class);
        var claim = claim("trace-origin");
        when(repository.claim()).thenReturn(Optional.of(claim));
        when(repository.complete(claim)).thenReturn(true);
        SimpleMeterRegistry metrics = new SimpleMeterRegistry();
        try {
            OutboxHandler handler = handler(event -> {
                assertThat(TenantUtil.getTenantId()).isEqualTo("caller-tenant");
                assertThat(MDC.get("originTraceId")).isEqualTo("trace-origin");
                assertThat(MDC.get("caller")).isNull();
                assertThat(event).isSameAs(claim.event());
            });
            OutboxDispatcher dispatcher = new OutboxDispatcher(repository, properties(), List.of(handler), metrics);
            MDC.put("caller", "preserved");
            TenantUtil.runWithTenant("caller-tenant", () -> {
                assertThat(dispatcher.dispatchBatch()).isEqualTo(1);
                assertThat(TenantUtil.getTenantId()).isEqualTo("caller-tenant");
                assertThat(MDC.get("caller")).isEqualTo("preserved");
                assertThat(MDC.get("originTraceId")).isNull();
            });
            assertThat(metrics.get("loadup.outbox.delivery")
                            .tag("outcome", "success")
                            .timer()
                            .count())
                    .isEqualTo(1);
            verify(repository).complete(claim);
            verify(repository, never()).fail(any(), any());
        } finally {
            metrics.close();
        }
    }

    @Test
    void lostLeaseIsMeasuredWithoutMarkingAnotherWorkersEventFailed() {
        JdbcOutboxRepository repository = mock(JdbcOutboxRepository.class);
        var claim = claim(null);
        when(repository.claim()).thenReturn(Optional.of(claim));
        when(repository.complete(claim)).thenReturn(false);
        SimpleMeterRegistry metrics = new SimpleMeterRegistry();
        try {
            OutboxDispatcher dispatcher =
                    new OutboxDispatcher(repository, properties(), List.of(handler(event -> {})), metrics);
            assertThat(dispatcher.dispatchBatch()).isEqualTo(1);
            assertThat(metrics.get("loadup.outbox.delivery")
                            .tag("outcome", "lease_lost")
                            .timer()
                            .count())
                    .isEqualTo(1);
            assertThat(MDC.getCopyOfContextMap()).isNullOrEmpty();
            verify(repository, never()).fail(any(), any());
        } finally {
            metrics.close();
        }
    }

    @Test
    void checkedHandlerFailureKeepsOriginalFailureForRetryAndRestoresContext() {
        JdbcOutboxRepository repository = mock(JdbcOutboxRepository.class);
        var claim = claim(null);
        when(repository.claim()).thenReturn(Optional.of(claim));
        Exception failure = new Exception("synthetic failure");
        when(repository.fail(claim, failure)).thenReturn(true);
        SimpleMeterRegistry metrics = new SimpleMeterRegistry();
        try {
            OutboxDispatcher dispatcher = new OutboxDispatcher(
                    repository,
                    properties(),
                    List.of(handler(event -> {
                        throw failure;
                    })),
                    metrics);
            assertThat(dispatcher.dispatchBatch()).isEqualTo(1);
            verify(repository).fail(claim, failure);
            verify(repository, never()).complete(any());
            assertThat(metrics.get("loadup.outbox.delivery")
                            .tag("outcome", "failure")
                            .timer()
                            .count())
                    .isEqualTo(1);
            assertThat(TenantUtil.hasTenantId()).isFalse();
        } finally {
            metrics.close();
        }
    }

    @Test
    void missingHandlerFailsClaimAndDuplicateHandlersAreRejected() {
        JdbcOutboxRepository repository = mock(JdbcOutboxRepository.class);
        var claim = claim(null);
        when(repository.claim()).thenReturn(Optional.of(claim));
        OutboxDispatcher dispatcher = new OutboxDispatcher(repository, properties(), List.of(), null);
        assertThat(dispatcher.dispatchBatch()).isEqualTo(1);
        verify(repository)
                .fail(
                        org.mockito.ArgumentMatchers.eq(claim),
                        org.mockito.ArgumentMatchers.isA(IllegalStateException.class));
        OutboxHandler handler = handler(event -> {});
        assertThatThrownBy(() -> new OutboxDispatcher(repository, properties(), List.of(handler, handler), null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Duplicate outbox event type");
    }

    @Test
    void emptyQueueStopsAndInterruptedHandlerPreventsFurtherClaims() {
        JdbcOutboxRepository empty = mock(JdbcOutboxRepository.class);
        when(empty.claim()).thenReturn(Optional.empty());
        assertThat(new OutboxDispatcher(empty, properties(), List.of(), null).dispatchBatch())
                .isZero();
        verify(empty, times(1)).claim();
        JdbcOutboxRepository repository = mock(JdbcOutboxRepository.class);
        var claim = claim(null);
        when(repository.claim()).thenReturn(Optional.of(claim));
        OutboxProperties properties = properties();
        properties.setBatchSize(5);
        InterruptedException interrupted = new InterruptedException("synthetic interruption");
        OutboxDispatcher dispatcher = new OutboxDispatcher(
                repository,
                properties,
                List.of(handler(event -> {
                    throw interrupted;
                })),
                null);
        assertThat(dispatcher.dispatchBatch()).isEqualTo(1);
        assertThat(Thread.currentThread().isInterrupted()).isTrue();
        verify(repository, times(1)).claim();
        verify(repository).fail(claim, interrupted);
        assertThat(dispatcher.dispatchBatch()).isZero();
    }

    @Test
    void pollerSurvivesDispatchFailureAndSupportsIdempotentLifecycle() throws Exception {
        OutboxDispatcher dispatcher = mock(OutboxDispatcher.class);
        AtomicInteger attempts = new AtomicInteger();
        CountDownLatch recovered = new CountDownLatch(1);
        doAnswer(invocation -> {
                    if (attempts.incrementAndGet() == 1) {
                        throw new IllegalStateException("synthetic polling failure");
                    }
                    recovered.countDown();
                    return 0;
                })
                .when(dispatcher)
                .dispatchBatch();
        OutboxProperties properties = properties();
        properties.setPollInterval(Duration.ofMillis(10));
        OutboxWorker worker = new OutboxWorker(dispatcher, properties);
        assertThat(worker.isRunning()).isFalse();
        assertThat(worker.isAutoStartup()).isTrue();
        worker.stop();
        try {
            worker.start();
            worker.start();
            assertThat(worker.isRunning()).isTrue();
            assertThat(recovered.await(3, TimeUnit.SECONDS)).isTrue();
            assertThat(attempts.get()).isGreaterThanOrEqualTo(2);
        } finally {
            worker.stop();
        }
        assertThat(worker.isRunning()).isFalse();
        worker.stop();
        properties.setWorkerEnabled(false);
        assertThat(worker.isAutoStartup()).isFalse();
    }

    @FunctionalInterface
    private interface HandlerAction {
        void handle(OutboxEvent event) throws Exception;
    }
}
