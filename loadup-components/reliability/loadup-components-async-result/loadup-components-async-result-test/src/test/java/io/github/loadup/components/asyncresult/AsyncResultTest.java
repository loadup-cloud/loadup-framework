/*
 * #%L
 * LoadUp Async Result Test
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
package io.github.loadup.components.asyncresult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import io.github.loadup.commons.context.ContextHolder;
import io.github.loadup.commons.context.ContextKey;
import io.github.loadup.commons.enums.CommonResultCodeEnum;
import io.github.loadup.commons.error.CommonException;
import io.github.loadup.components.asyncresult.local.LocalAsyncResultProvider;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import tools.jackson.databind.json.JsonMapper;

class AsyncResultTest {
    static AsyncResultProperties settings(int pending, Duration timeout) {
        return new AsyncResultProperties(
                true,
                "local",
                "test",
                timeout,
                Duration.ofSeconds(5),
                pending,
                Duration.ofMinutes(1),
                Duration.ofMillis(10),
                1024,
                100);
    }

    static AsyncResultOptions options() {
        return new AsyncResultOptions("tenant", "echo.v1", "order", null);
    }

    static CompletableFuture<Void> sent() {
        return CompletableFuture.completedFuture(null);
    }

    static DefaultAsyncResultTemplate template(AsyncResultProvider p, AsyncResultProperties settings) {
        return new DefaultAsyncResultTemplate(p, settings, JsonMapper.builder().build(), null);
    }

    static void awaitCleanup(SimpleMeterRegistry metrics) throws Exception {
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(3);
        while (metrics.get("loadup.async_result.pending").gauge().value() != 0 && System.nanoTime() < deadline) {
            Thread.sleep(1);
        }
        assertThat(metrics.get("loadup.async_result.pending").gauge().value()).isZero();
    }

    @Test
    void earlyReplyIsCorrelatedAndSenderReceivesCapturedContext() {
        var key = new ContextKey<>("example", String.class);
        try (var provider = new LocalAsyncResultProvider(100);
                var template = template(provider, settings(5, Duration.ofSeconds(2)))) {
            String result = ContextHolder.callWith(
                    ContextHolder.current().with(key, "metadata"),
                    () -> template.execute(options(), String.class, reply -> {
                        assertThat(Thread.currentThread().isVirtual()).isTrue();
                        assertThat(ContextHolder.get(key)).isEqualTo("metadata");
                        assertThat(template.complete(reply, "accepted")).isEqualTo(AsyncCompletionStatus.ACCEPTED);
                        return sent();
                    }));
            assertThat(result).isEqualTo("accepted");
            assertThat(ContextHolder.get(key)).isNull();
        }
    }

    @Test
    void timeoutDoesNotEraseLateResultAndSlotsAreRecovered() throws Exception {
        var registry = new SimpleMeterRegistry();
        try (var provider = new LocalAsyncResultProvider(100);
                var template = new DefaultAsyncResultTemplate(
                        provider,
                        settings(1, Duration.ofMillis(150)),
                        JsonMapper.builder().build(),
                        registry)) {
            var issued = new CompletableFuture<AsyncReplyKey>();
            var waiting = template.executeAsync(options(), String.class, key -> {
                issued.complete(key);
                return sent();
            });
            var key = issued.get(2, TimeUnit.SECONDS);
            assertThatThrownBy(() -> template.executeAsync(options(), String.class, k -> sent()))
                    .isInstanceOf(CommonException.class)
                    .extracting(e -> ((CommonException) e).getResultCode())
                    .isEqualTo(AsyncResultCodeEnum.CAPACITY_EXCEEDED);
            assertThatThrownBy(() -> waiting.get(2, TimeUnit.SECONDS)).hasCauseInstanceOf(CommonException.class);
            assertThat(((CommonException) catchThrowable(waiting::join).getCause()).getResultCode())
                    .isEqualTo(AsyncResultCodeEnum.TIMEOUT);
            awaitCleanup(registry);
            assertThat(template.complete(key, "late")).isEqualTo(AsyncCompletionStatus.ACCEPTED);
            assertThat(provider.find(key).orElseThrow().payload()).isEqualTo("\"late\"");
            assertThat(template.execute(options(), String.class, k -> {
                        template.complete(k, "next");
                        return sent();
                    }))
                    .isEqualTo("next");
        }
    }

    @Test
    void duplicateAndConflictingRepliesAreClassifiedAndTenantsAreIsolated() throws Exception {
        try (var provider = new LocalAsyncResultProvider(100);
                var template = template(provider, settings(5, Duration.ofSeconds(2)))) {
            var issued = new CompletableFuture<AsyncReplyKey>();
            var future = template.executeAsync(options(), String.class, k -> {
                issued.complete(k);
                return sent();
            });
            var key = issued.get(2, TimeUnit.SECONDS);
            assertThat(template.complete(
                            new AsyncReplyKey("other", key.operation(), key.businessId(), key.correlationId()), "bad"))
                    .isEqualTo(AsyncCompletionStatus.UNKNOWN);
            assertThat(template.complete(key, "ok")).isEqualTo(AsyncCompletionStatus.ACCEPTED);
            assertThat(template.complete(key, "ok")).isEqualTo(AsyncCompletionStatus.DUPLICATE);
            assertThat(template.complete(key, "different")).isEqualTo(AsyncCompletionStatus.CONFLICT);
            assertThat(future.get(2, TimeUnit.SECONDS)).isEqualTo("ok");
        }
    }

    @Test
    void remoteFailurePreservesOnlyItsStableCode() throws Exception {
        try (var provider = new LocalAsyncResultProvider(100);
                var template = template(provider, settings(5, Duration.ofSeconds(2)))) {
            var future = template.executeAsync(options(), String.class, k -> {
                template.completeFailure(k, CommonResultCodeEnum.CONFLICT);
                return sent();
            });
            assertThatThrownBy(future::join).hasCauseInstanceOf(AsyncResultFailureException.class);
            var error =
                    (AsyncResultFailureException) catchThrowable(future::join).getCause();
            assertThat(error.getRemoteCode()).isEqualTo(CommonResultCodeEnum.CONFLICT.getCode());
            assertThat(error.getResultCode()).isEqualTo(AsyncResultCodeEnum.REMOTE_FAILURE);
        }
    }

    @Test
    void dispatchExceptionsAndFailedAcknowledgementsReleaseWaiters() throws Exception {
        try (var provider = new LocalAsyncResultProvider(100);
                var template = template(provider, settings(5, Duration.ofSeconds(2)))) {
            for (AsyncDispatch dispatch : List.<AsyncDispatch>of(
                    k -> {
                        throw new Exception("private");
                    },
                    k -> CompletableFuture.failedFuture(new Exception("private")),
                    k -> null)) {
                var future = template.executeAsync(options(), String.class, dispatch);
                assertThatThrownBy(() -> future.get(2, TimeUnit.SECONDS)).hasCauseInstanceOf(CommonException.class);
                assertThat(catchThrowable(future::join).getCause())
                        .hasMessage(AsyncResultCodeEnum.DISPATCH_FAILED.getMessage())
                        .hasNoCause();
            }
        }
    }

    @Test
    void cancellationShutdownAndSynchronousInterruptionReleaseWaiters() throws Exception {
        try (var provider = new LocalAsyncResultProvider(100);
                var template = template(provider, settings(10, Duration.ofSeconds(3)))) {
            var future = template.executeAsync(options(), String.class, k -> sent());
            assertThat(future.cancel(false)).isTrue();
            var issued = new CountDownLatch(1);
            var error = new AtomicReference<Throwable>();
            var interrupted = new java.util.concurrent.atomic.AtomicBoolean();
            var thread = Thread.ofVirtual().start(() -> {
                try {
                    template.execute(options(), String.class, k -> {
                        issued.countDown();
                        return sent();
                    });
                } catch (Throwable failure) {
                    error.set(failure);
                    interrupted.set(Thread.currentThread().isInterrupted());
                }
            });
            assertThat(issued.await(2, TimeUnit.SECONDS)).isTrue();
            thread.interrupt();
            thread.join(2000);
            assertThat(error.get()).isInstanceOf(CommonException.class);
            assertThat(interrupted).isTrue();
            var waiting = template.executeAsync(options(), String.class, k -> sent());
            template.close();
            template.close();
            assertThatThrownBy(waiting::join).hasCauseInstanceOf(CommonException.class);
            assertThatThrownBy(() -> template.executeAsync(options(), String.class, k -> sent()))
                    .isInstanceOf(CommonException.class);
        }
    }

    @Test
    void activeTransactionsAndInvalidTimeoutsAreRejectedBeforeDispatch() {
        try (var provider = new LocalAsyncResultProvider(100);
                var template = template(provider, settings(5, Duration.ofSeconds(2)))) {
            TransactionSynchronizationManager.setActualTransactionActive(true);
            try {
                assertThatThrownBy(() -> template.executeAsync(options(), String.class, k -> {
                            throw new AssertionError();
                        }))
                        .isInstanceOf(CommonException.class)
                        .extracting(e -> ((CommonException) e).getResultCode())
                        .isEqualTo(AsyncResultCodeEnum.ACTIVE_TRANSACTION);
            } finally {
                TransactionSynchronizationManager.clear();
            }
            assertThatThrownBy(() -> template.executeAsync(
                            new AsyncResultOptions("t", "o", "b", Duration.ofSeconds(6)), String.class, k -> sent()))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Test
    void malformedAndOversizedRepliesAreSafeFailures() throws Exception {
        try (var provider = new LocalAsyncResultProvider(100);
                var template = template(provider, settings(5, Duration.ofSeconds(2)))) {
            var issued = new CompletableFuture<AsyncReplyKey>();
            var future = template.executeAsync(options(), Integer.class, k -> {
                issued.complete(k);
                return sent();
            });
            var key = issued.get(2, TimeUnit.SECONDS);
            assertThatThrownBy(() -> template.complete(key, "s".repeat(1024))).isInstanceOf(CommonException.class);
            provider.complete(key, new AsyncResultValue("\"not-an-integer\"", null));
            assertThatThrownBy(future::join).hasCauseInstanceOf(CommonException.class);
            assertThat(new AsyncResultValue("private-payload", null).toString()).doesNotContain("private-payload");
        }
    }

    @Test
    void lostNotificationsAndTransientReadErrorsRecoverFromStoredResult() throws Exception {
        try (var actual = new LocalAsyncResultProvider(100)) {
            var provider = spy(actual);
            doAnswer(i -> (AutoCloseable) () -> {}).when(provider).subscribe(anyString(), any());
            var first = new java.util.concurrent.atomic.AtomicBoolean(true);
            doAnswer(i -> {
                        if (first.getAndSet(false)) {
                            throw new IllegalStateException("offline");
                        }
                        return actual.find(i.getArgument(0));
                    })
                    .when(provider)
                    .find(any());
            var registry = new SimpleMeterRegistry();
            try (var template = new DefaultAsyncResultTemplate(
                    provider,
                    settings(5, Duration.ofSeconds(2)),
                    JsonMapper.builder().build(),
                    registry)) {
                assertThat(template.execute(options(), String.class, k -> {
                            template.complete(k, "stored");
                            return sent();
                        }))
                        .isEqualTo("stored");
            }
        }
    }

    @Test
    void backendRegistrationAndCompletionFailuresAreClassified() {
        var provider = mock(AsyncResultProvider.class);
        when(provider.subscribe(anyString(), any())).thenReturn(() -> {});
        doThrow(new IllegalStateException("private")).when(provider).register(any(), anyString(), any());
        when(provider.find(any())).thenReturn(Optional.empty());
        when(provider.complete(any(), any())).thenThrow(new IllegalStateException("private"));
        try (var template = template(provider, settings(5, Duration.ofSeconds(2)))) {
            assertThatThrownBy(() -> template.execute(options(), String.class, k -> sent()))
                    .isInstanceOf(CommonException.class)
                    .extracting(e -> ((CommonException) e).getResultCode())
                    .isEqualTo(AsyncResultCodeEnum.UNAVAILABLE);
            assertThatThrownBy(() -> template.complete(
                            new AsyncReplyKey("t", "o", "b", UUID.randomUUID().toString()), "result"))
                    .isInstanceOf(CommonException.class)
                    .hasNoCause();
        }
    }

    @Test
    void manyRepliesCanArriveOutOfOrderWithoutCrossCompleting() throws Exception {
        try (var provider = new LocalAsyncResultProvider(100);
                var template = template(provider, settings(50, Duration.ofSeconds(3)))) {
            var keys = new ConcurrentHashMap<Integer, AsyncReplyKey>();
            var ready = new CountDownLatch(40);
            var futures = new ArrayList<CompletableFuture<Integer>>();
            for (int i = 0; i < 40; i++) {
                int index = i;
                futures.add(template.executeAsync(options(), Integer.class, k -> {
                    keys.put(index, k);
                    ready.countDown();
                    return sent();
                }));
            }
            assertThat(ready.await(2, TimeUnit.SECONDS)).isTrue();
            for (int i = 39; i >= 0; i--) {
                template.complete(keys.get(i), i);
            }
            for (int i = 0; i < 40; i++) {
                assertThat(futures.get(i).get(2, TimeUnit.SECONDS)).isEqualTo(i);
            }
        }
    }

    @Test
    void startupAndSubscriptionCleanupFailuresDoNotLeaveWaitersActive() {
        var unavailable = mock(AsyncResultProvider.class);
        when(unavailable.subscribe(anyString(), any())).thenThrow(new IllegalStateException("private"));
        assertThatThrownBy(() -> template(unavailable, settings(1, Duration.ofSeconds(1))))
                .isInstanceOf(IllegalStateException.class);
        var provider = mock(AsyncResultProvider.class);
        when(provider.subscribe(anyString(), any())).thenReturn(() -> {
            throw new Exception("private");
        });
        when(provider.find(any())).thenReturn(Optional.empty());
        var template = template(provider, settings(1, Duration.ofSeconds(1)));
        var future = template.executeAsync(options(), String.class, k -> sent());
        template.close();
        assertThatThrownBy(future::join).hasCauseInstanceOf(CommonException.class);
        assertThatThrownBy(() -> template.complete(LocalAsyncResultProviderTest.key(), "value"))
                .isInstanceOf(CommonException.class)
                .extracting(e -> ((CommonException) e).getResultCode())
                .isEqualTo(AsyncResultCodeEnum.CLOSED);
    }

    @Test
    void senderBusinessValidationRetainsItsEnumAndSerializationFailureIsSanitized() {
        try (var provider = new LocalAsyncResultProvider(100);
                var template = template(provider, settings(5, Duration.ofSeconds(1)))) {
            var failure = new CommonException(CommonResultCodeEnum.CONFLICT);
            assertThatThrownBy(() -> template.execute(options(), String.class, k -> {
                        throw failure;
                    }))
                    .isSameAs(failure);
            assertThatThrownBy(() -> template.complete(LocalAsyncResultProviderTest.key(), new BrokenReply()))
                    .isInstanceOf(CommonException.class)
                    .hasNoCause()
                    .hasMessage(AsyncResultCodeEnum.INVALID_REPLY.getMessage());
            assertThatThrownBy(() -> template.executeAsync(
                            new AsyncResultOptions("t", "op", "b", Duration.ofNanos(1)), String.class, k -> sent()))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Test
    void stalledDispatchRemainsBoundedAfterItsWaiterTimesOut() throws Exception {
        var release = new CountDownLatch(1);
        var entered = new CountDownLatch(1);
        try (var provider = new LocalAsyncResultProvider(100);
                var template = template(provider, settings(1, Duration.ofMillis(200)))) {
            var future = template.executeAsync(options(), String.class, k -> {
                entered.countDown();
                release.await(3, TimeUnit.SECONDS);
                return sent();
            });
            assertThat(entered.await(2, TimeUnit.SECONDS)).isTrue();
            assertThatThrownBy(() -> future.get(2, TimeUnit.SECONDS)).hasCauseInstanceOf(CommonException.class);
            assertThatThrownBy(() -> template.executeAsync(options(), String.class, k -> sent()))
                    .isInstanceOf(CommonException.class)
                    .extracting(e -> ((CommonException) e).getResultCode())
                    .isEqualTo(AsyncResultCodeEnum.CAPACITY_EXCEEDED);
        } finally {
            release.countDown();
        }
    }

    static final class BrokenReply {
        public String getValue() {
            throw new IllegalStateException("private-payload");
        }
    }
}
