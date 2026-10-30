/*
 * #%L
 * LoadUp Async Result
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

import io.github.loadup.commons.context.ContextHolder;
import io.github.loadup.commons.context.ExecutionContext;
import io.github.loadup.commons.error.CommonException;
import io.github.loadup.commons.log.LogUtil;
import io.github.loadup.commons.result.ResultCode;
import io.micrometer.context.ContextSnapshot;
import io.micrometer.context.ContextSnapshotFactory;
import io.micrometer.core.instrument.MeterRegistry;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import tools.jackson.databind.ObjectMapper;

/**
 * Bounded waiters; backend notifications never execute user continuations on network threads.
 */
public final class DefaultAsyncResultTemplate implements AsyncResultTemplate {
    private final AsyncResultProvider provider;
    private final AsyncResultProperties properties;
    private final ObjectMapper mapper;
    private final MeterRegistry metrics;
    private final String owner = UUID.randomUUID().toString();
    private final ConcurrentHashMap<AsyncReplyKey, Pending<?>> pending = new ConcurrentHashMap<>();
    private final Semaphore slots;
    private final Semaphore dispatchSlots;
    private final ExecutorService workers = Executors.newVirtualThreadPerTaskExecutor();
    private final ScheduledThreadPoolExecutor timer = new ScheduledThreadPoolExecutor(
            1, Thread.ofPlatform().daemon().name("loadup-async-result-timer").factory());
    private final Object lifecycle = new Object();
    private final AutoCloseable subscription;
    private boolean closed;

    public DefaultAsyncResultTemplate(
            AsyncResultProvider provider,
            AsyncResultProperties properties,
            ObjectMapper mapper,
            MeterRegistry metrics) {
        this.provider = Objects.requireNonNull(provider);
        this.properties = Objects.requireNonNull(properties);
        this.mapper = Objects.requireNonNull(mapper);
        this.metrics = metrics;
        slots = new Semaphore(properties.maxPending());
        dispatchSlots = new Semaphore(properties.maxPending());
        timer.setRemoveOnCancelPolicy(true);
        try {
            subscription = provider.subscribe(owner, key -> {
                var p = pending.get(key);
                if (p != null) {
                    check(p);
                }
            });
        } catch (RuntimeException failure) {
            workers.shutdownNow();
            timer.shutdownNow();
            throw failure;
        }
        timer.scheduleWithFixedDelay(
                () -> pending.values().forEach(this::check),
                properties.fallbackPollInterval().toMillis(),
                properties.fallbackPollInterval().toMillis(),
                TimeUnit.MILLISECONDS);
        if (metrics != null) {
            metrics.gauge("loadup.async_result.pending", pending, ConcurrentHashMap::size);
        }
    }

    @Override
    public <T> T execute(AsyncResultOptions options, Class<T> type, AsyncDispatch dispatch) {
        var future = executeAsync(options, type, dispatch);
        try {
            return future.get();
        } catch (InterruptedException failure) {
            future.cancel(false);
            Thread.currentThread().interrupt();
            throw new CommonException(AsyncResultCodeEnum.INTERRUPTED, failure);
        } catch (ExecutionException failure) {
            if (failure.getCause() instanceof RuntimeException cause) {
                throw cause;
            }
            if (failure.getCause() instanceof Error cause) {
                throw cause;
            }
            throw new CommonException(AsyncResultCodeEnum.UNAVAILABLE, failure.getCause());
        }
    }

    @Override
    public <T> CompletableFuture<T> executeAsync(AsyncResultOptions options, Class<T> type, AsyncDispatch dispatch) {
        Objects.requireNonNull(options);
        Objects.requireNonNull(type);
        Objects.requireNonNull(dispatch);
        if (TransactionSynchronizationManager.isActualTransactionActive()) {
            throw new CommonException(AsyncResultCodeEnum.ACTIVE_TRANSACTION);
        }
        var timeout = options.waitTimeout() == null ? properties.waitTimeout() : options.waitTimeout();
        if (timeout.toMillis() < 1 || timeout.compareTo(properties.maxWaitTimeout()) > 0) {
            throw new IllegalArgumentException("waitTimeout exceeds configured bounds");
        }
        var key = new AsyncReplyKey(
                options.tenantId(),
                options.operation(),
                options.businessId(),
                UUID.randomUUID().toString());
        var p = new Pending<T>(key, type);
        synchronized (lifecycle) {
            if (closed) {
                throw new CommonException(AsyncResultCodeEnum.CLOSED);
            }
            if (!slots.tryAcquire()) {
                throw new CommonException(AsyncResultCodeEnum.CAPACITY_EXCEEDED);
            }
            if (!dispatchSlots.tryAcquire()) {
                slots.release();
                throw new CommonException(AsyncResultCodeEnum.CAPACITY_EXCEEDED);
            }
            pending.put(key, p);
            p.future.whenComplete((value, failure) -> {
                pending.remove(key, p);
                slots.release();
                if (p.timeout != null) {
                    p.timeout.cancel(false);
                }
                record(p, failure);
            });
            p.timeout = timer.schedule(
                    () -> submit(
                            p, () -> p.future.completeExceptionally(new CommonException(AsyncResultCodeEnum.TIMEOUT))),
                    timeout.toNanos(),
                    TimeUnit.NANOSECONDS);
            if (!submit(p, () -> dispatch(p, dispatch))) {
                dispatchSlots.release();
            }
        }
        return p.future;
    }

    private void dispatch(Pending<?> p, AsyncDispatch dispatch) {
        try {
            if (p.future.isDone()) {
                return;
            }
            try {
                try {
                    provider.register(p.key, owner, properties.resultRetention());
                } catch (RuntimeException failure) {
                    p.future.completeExceptionally(new CommonException(AsyncResultCodeEnum.UNAVAILABLE));
                    return;
                }
                if (p.future.isDone()) {
                    return;
                }
                var sent = Objects.requireNonNull(
                        dispatch.send(p.key), "Dispatch must return a send acknowledgement stage");
                sent.whenComplete((ignored, failure) -> {
                    if (failure != null) {
                        submit(
                                p,
                                () -> p.future.completeExceptionally(
                                        new CommonException(AsyncResultCodeEnum.DISPATCH_FAILED)));
                    }
                });
                check(p);
            } catch (Exception failure) {
                p.future.completeExceptionally(
                        failure instanceof CommonException
                                ? failure
                                : new CommonException(AsyncResultCodeEnum.DISPATCH_FAILED));
            }
        } finally {
            dispatchSlots.release();
        }
    }

    private <T> void check(Pending<T> p) {
        if (p.future.isDone() || !p.reading.compareAndSet(false, true)) {
            return;
        }
        submit(p, () -> {
            try {
                provider.find(p.key).ifPresent(value -> {
                    try {
                        if (value.failureCode() != null) {
                            p.future.completeExceptionally(new AsyncResultFailureException(value.failureCode()));
                        } else {
                            checkSize(value.payload());
                            p.future.complete(mapper.readValue(value.payload(), p.type));
                        }
                    } catch (RuntimeException failure) {
                        p.future.completeExceptionally(new CommonException(AsyncResultCodeEnum.INVALID_REPLY));
                    }
                });
            } catch (RuntimeException failure) {
                // The next notification or poll can recover a temporary backend read failure.
                if (metrics != null) {
                    metrics.counter("loadup.async_result.read_failures").increment();
                }
            } finally {
                p.reading.set(false);
            }
        });
    }

    private boolean submit(Pending<?> p, Runnable task) {
        try {
            workers.execute(p.snapshot.wrap(() -> ContextHolder.runWith(p.context, task)));
            return true;
        } catch (RejectedExecutionException failure) {
            p.future.completeExceptionally(new CommonException(AsyncResultCodeEnum.CLOSED));
            return false;
        }
    }

    @Override
    public AsyncCompletionStatus complete(AsyncReplyKey key, Object value) {
        Objects.requireNonNull(key);
        final String payload;
        try {
            payload = mapper.writeValueAsString(value);
        } catch (RuntimeException failure) {
            throw new CommonException(AsyncResultCodeEnum.INVALID_REPLY);
        }
        checkSize(payload);
        return finish(key, new AsyncResultValue(payload, null));
    }

    @Override
    public AsyncCompletionStatus completeFailure(AsyncReplyKey key, ResultCode failure) {
        return finish(
                Objects.requireNonNull(key),
                new AsyncResultValue(null, ResultCode.requireFailure(failure).getCode()));
    }

    private AsyncCompletionStatus finish(AsyncReplyKey key, AsyncResultValue value) {
        synchronized (lifecycle) {
            if (closed) {
                throw new CommonException(AsyncResultCodeEnum.CLOSED);
            }
        }
        try {
            var status = provider.complete(key, value);
            if (metrics != null) {
                metrics.counter(
                                "loadup.async_result.completions",
                                "outcome",
                                status.name().toLowerCase(java.util.Locale.ROOT))
                        .increment();
            }
            return status;
        } catch (CommonException failure) {
            throw failure;
        } catch (RuntimeException failure) {
            throw new CommonException(AsyncResultCodeEnum.UNAVAILABLE);
        }
    }

    private void checkSize(String payload) {
        if (payload.getBytes(StandardCharsets.UTF_8).length > properties.maxResultBytes()) {
            throw new CommonException(AsyncResultCodeEnum.INVALID_REPLY);
        }
    }

    private void record(Pending<?> p, Throwable failure) {
        if (metrics == null) {
            return;
        }
        String outcome = failure == null
                ? "success"
                : failure instanceof CancellationException
                        ? "cancelled"
                        : failure instanceof CommonException coded
                                ? coded.getResultCode().getCode()
                                : "failure";
        metrics.timer("loadup.async_result.wait", "outcome", outcome)
                .record(System.nanoTime() - p.started, TimeUnit.NANOSECONDS);
    }

    @Override
    public void close() {
        synchronized (lifecycle) {
            if (closed) {
                return;
            }
            closed = true;
        }
        try {
            subscription.close();
        } catch (Exception failure) {
            LogUtil.warn(
                    DefaultAsyncResultTemplate.class,
                    "Async result subscription cleanup failed; type={}",
                    failure.getClass().getSimpleName());
        }
        pending.values().forEach(p -> p.future.completeExceptionally(new CommonException(AsyncResultCodeEnum.CLOSED)));
        timer.shutdownNow();
        workers.shutdownNow();
    }

    private static final class Pending<T> {
        final AsyncReplyKey key;
        final Class<T> type;
        final CompletableFuture<T> future = new CompletableFuture<>();
        final AtomicBoolean reading = new AtomicBoolean();
        final ExecutionContext context = ContextHolder.current();
        final ContextSnapshot snapshot =
                ContextSnapshotFactory.builder().build().captureAll();
        final long started = System.nanoTime();
        volatile ScheduledFuture<?> timeout;

        Pending(AsyncReplyKey key, Class<T> type) {
            this.key = key;
            this.type = type;
        }
    }
}
