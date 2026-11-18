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
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import io.github.loadup.commons.context.ContextHolder;
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

class TaskWorkerTest {
    private static TaskProperties properties() {
        TaskProperties properties = new TaskProperties();
        properties.setBatchSize(1);
        return properties;
    }

    private static JdbcTaskRepository.Claim claim(String traceId) {
        return new JdbcTaskRepository.Claim(
                new TaskExecution(
                        "event-id",
                        TaskSubmission.immediate("payment.completed", "order-id", "{}"),
                        Instant.parse("2026-10-14T00:00:00Z"),
                        traceId),
                "lease-token",
                1);
    }

    private static TaskHandler handler(HandlerAction action) {
        return new TaskHandler() {
            public String taskType() {
                return "payment.completed";
            }

            public void handle(TaskExecution event) throws Exception {
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
    void successfulDispatchRestoresTraceContextAndLeavesCallerState() {
        JdbcTaskRepository repository = mock(JdbcTaskRepository.class);
        doAnswer(call -> {
                    ((Runnable) call.getArgument(0)).run();
                    return null;
                })
                .when(repository)
                .withoutTransaction(any());
        var claim = claim("trace-origin");
        when(repository.claim(List.of("payment.completed"))).thenReturn(Optional.of(claim));
        when(repository.complete(claim)).thenReturn(true);
        SimpleMeterRegistry metrics = new SimpleMeterRegistry();
        try {
            TaskHandler handler = handler(event -> {
                assertThat(MDC.get("originTraceId")).isEqualTo("trace-origin");
                assertThat(MDC.get("caller")).isNull();
                assertThat(event).isSameAs(claim.event());
            });
            TaskDispatcher dispatcher = new TaskDispatcher(repository, properties(), List.of(handler), metrics);
            MDC.put("caller", "preserved");
            dispatcher.execute();
            assertThat(MDC.get("caller")).isEqualTo("preserved");
            assertThat(MDC.get("originTraceId")).isNull();
            assertThat(metrics.get("loadup.task.delivery")
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
        JdbcTaskRepository repository = mock(JdbcTaskRepository.class);
        doAnswer(call -> {
                    ((Runnable) call.getArgument(0)).run();
                    return null;
                })
                .when(repository)
                .withoutTransaction(any());
        var claim = claim(null);
        when(repository.claim(List.of("payment.completed"))).thenReturn(Optional.of(claim));
        when(repository.complete(claim)).thenReturn(false);
        SimpleMeterRegistry metrics = new SimpleMeterRegistry();
        try {
            TaskDispatcher dispatcher =
                    new TaskDispatcher(repository, properties(), List.of(handler(event -> {})), metrics);
            dispatcher.execute();
            assertThat(metrics.get("loadup.task.delivery")
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
        JdbcTaskRepository repository = mock(JdbcTaskRepository.class);
        doAnswer(call -> {
                    ((Runnable) call.getArgument(0)).run();
                    return null;
                })
                .when(repository)
                .withoutTransaction(any());
        var claim = claim(null);
        when(repository.claim(List.of("payment.completed"))).thenReturn(Optional.of(claim));
        Exception failure = new Exception("synthetic failure");
        when(repository.fail(claim, failure)).thenReturn(true);
        SimpleMeterRegistry metrics = new SimpleMeterRegistry();
        try {
            TaskDispatcher dispatcher = new TaskDispatcher(
                    repository,
                    properties(),
                    List.of(handler(event -> {
                        throw failure;
                    })),
                    metrics);
            dispatcher.execute();
            verify(repository).fail(claim, failure);
            verify(repository, never()).complete(any());
            assertThat(metrics.get("loadup.task.delivery")
                            .tag("outcome", "failure")
                            .timer()
                            .count())
                    .isEqualTo(1);
        } finally {
            metrics.close();
        }
    }

    @Test
    void noHandlersSkipTheQueueAndDuplicateHandlersAreRejected() {
        JdbcTaskRepository repository = mock(JdbcTaskRepository.class);
        var dispatcher = new TaskDispatcher(repository, properties(), List.of(), null);
        dispatcher.execute();
        var worker = new TaskWorker(dispatcher, properties());
        worker.start();
        assertThat(worker.isAutoStartup()).isFalse();
        assertThat(worker.isRunning()).isFalse();
        verifyNoInteractions(repository);
        TaskHandler handler = handler(event -> {});
        assertThatThrownBy(() -> new TaskDispatcher(repository, properties(), List.of(handler, handler), null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Duplicate task type");
    }

    @Test
    void emptyQueueStopsAndInterruptedHandlerPreventsFurtherClaims() {
        JdbcTaskRepository empty = mock(JdbcTaskRepository.class);
        when(empty.claim(List.of("payment.completed"))).thenReturn(Optional.empty());
        doAnswer(call -> {
                    ((Runnable) call.getArgument(0)).run();
                    return null;
                })
                .when(empty)
                .withoutTransaction(any());
        new TaskDispatcher(empty, properties(), List.of(handler(event -> {})), null).execute();
        verify(empty, times(1)).claim(List.of("payment.completed"));
        JdbcTaskRepository repository = mock(JdbcTaskRepository.class);
        doAnswer(call -> {
                    ((Runnable) call.getArgument(0)).run();
                    return null;
                })
                .when(repository)
                .withoutTransaction(any());
        var claim = claim(null);
        when(repository.claim(List.of("payment.completed"))).thenReturn(Optional.of(claim));
        TaskProperties properties = properties();
        properties.setBatchSize(5);
        InterruptedException interrupted = new InterruptedException("synthetic interruption");
        TaskDispatcher dispatcher = new TaskDispatcher(
                repository,
                properties,
                List.of(handler(event -> {
                    throw interrupted;
                })),
                null);
        dispatcher.execute();
        assertThat(Thread.currentThread().isInterrupted()).isTrue();
        verify(repository, times(1)).claim(List.of("payment.completed"));
        verify(repository).fail(claim, interrupted);
        dispatcher.execute();
    }

    @Test
    void pollerSurvivesDispatchFailureAndSupportsIdempotentLifecycle() throws Exception {
        TaskDispatcher dispatcher = mock(TaskDispatcher.class);
        when(dispatcher.hasHandlers()).thenReturn(true);
        AtomicInteger attempts = new AtomicInteger();
        CountDownLatch recovered = new CountDownLatch(1);
        doAnswer(invocation -> {
                    if (attempts.incrementAndGet() == 1) {
                        throw new IllegalStateException("synthetic polling failure");
                    }
                    recovered.countDown();
                    return null;
                })
                .when(dispatcher)
                .execute();
        TaskProperties properties = properties();
        properties.setPollInterval(Duration.ofMillis(10));
        TaskWorker worker = new TaskWorker(dispatcher, properties);
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

    @Test
    void workerDispatchesWithoutBindingDeploymentContext() throws Exception {
        JdbcTaskRepository repository = mock(JdbcTaskRepository.class);
        doAnswer(call -> {
                    ((Runnable) call.getArgument(0)).run();
                    return null;
                })
                .when(repository)
                .withoutTransaction(any());
        var claim = claim(null);
        when(repository.claim(List.of("payment.completed")))
                .thenReturn(Optional.of(claim))
                .thenReturn(Optional.empty());
        when(repository.complete(claim)).thenReturn(true);
        CountDownLatch handled = new CountDownLatch(1);
        TaskHandler handler = handler(event -> handled.countDown());
        TaskProperties properties = properties();
        properties.setPollInterval(Duration.ofMillis(10));
        TaskWorker worker =
                new TaskWorker(new TaskDispatcher(repository, properties, List.of(handler), null), properties);
        try {
            worker.start();
            assertThat(handled.await(3, TimeUnit.SECONDS)).isTrue();
            assertThat(ContextHolder.isBound()).isFalse();
        } finally {
            worker.stop();
        }
    }

    @FunctionalInterface
    private interface HandlerAction {
        void handle(TaskExecution event) throws Exception;
    }
}
