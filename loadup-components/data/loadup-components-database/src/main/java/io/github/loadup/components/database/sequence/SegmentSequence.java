/*
 * #%L
 * Loadup Components Database
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
package io.github.loadup.components.database.sequence;

import io.github.loadup.commons.error.CommonException;
import java.time.Clock;
import java.time.LocalDate;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Bounded per-scope double buffers; the warm path performs only atomic claims.
 */
public final class SegmentSequence implements Sequence, AutoCloseable {
    private final SequenceRangeAllocator allocator;
    private final SequenceProperties properties;
    private final Clock clock;
    private final ConcurrentHashMap<String, State> states = new ConcurrentHashMap<>();
    private final java.util.Set<CompletableFuture<Void>> pending = ConcurrentHashMap.newKeySet();
    private final ThreadPoolExecutor executor;
    private final Object scopeMonitor = new Object();
    private final io.micrometer.core.instrument.Counter issued;
    private final io.micrometer.core.instrument.Timer refill;
    private final java.util.Map<DatabaseResultCodeEnum, io.micrometer.core.instrument.Counter> failures =
            new java.util.EnumMap<>(DatabaseResultCodeEnum.class);
    private volatile boolean closed;

    public SegmentSequence(SequenceRangeAllocator allocator, SequenceProperties properties, Clock clock) {
        this(allocator, properties, clock, null);
    }

    public SegmentSequence(
            SequenceRangeAllocator allocator,
            SequenceProperties properties,
            Clock clock,
            io.micrometer.core.instrument.MeterRegistry registry) {
        issued = registry == null ? null : registry.counter("loadup.database.sequence.issued");
        refill = registry == null ? null : registry.timer("loadup.database.sequence.refill");
        if (registry != null) {
            for (DatabaseResultCodeEnum code : DatabaseResultCodeEnum.values()) {
                failures.put(code, registry.counter("loadup.database.sequence.failures", "reason", code.name()));
            }
        }
        properties.validate();
        this.allocator = allocator;
        this.properties = properties;
        this.clock = clock;
        executor = new ThreadPoolExecutor(
                properties.getPrefetchThreads(),
                properties.getPrefetchThreads(),
                0,
                TimeUnit.MILLISECONDS,
                new ArrayBlockingQueue<>(properties.getQueueCapacity()),
                Thread.ofPlatform().daemon().name("loadup-sequence-", 0).factory(),
                new ThreadPoolExecutor.AbortPolicy());
    }

    public long next(SequenceScope scope) {
        if (closed) {
            throw new CommonException(DatabaseResultCodeEnum.CLOSED);
        }
        if (scope == null) {
            throw new CommonException(DatabaseResultCodeEnum.INVALID_INPUT);
        }
        State state = state(scope);
        Buffer buffer = state.active;
        if (buffer != null) {
            long value = buffer.claim(1);
            if (value > 0) {
                if (issued != null) {
                    issued.increment();
                }
                if (buffer.remaining() <= buffer.size / 5) {
                    prefetch(state);
                }
                return value;
            }
        }
        return take(scope, 1)[0];
    }

    public long[] nextBatch(SequenceScope scope, int count) {
        if (count < 1 || count > 10_000) {
            throw new CommonException(DatabaseResultCodeEnum.INVALID_INPUT);
        }
        return take(scope, count);
    }

    private long[] take(SequenceScope scope, int count) {
        if (closed) {
            throw new CommonException(DatabaseResultCodeEnum.CLOSED);
        }
        if (scope == null) {
            throw new CommonException(DatabaseResultCodeEnum.INVALID_INPUT);
        }
        State state = state(scope);
        long[] result = new long[count];
        int offset = 0;
        long deadline = System.nanoTime() + properties.getWaitTimeout().toNanos();
        while (offset < count) {
            if (closed) {
                throw new CommonException(DatabaseResultCodeEnum.CLOSED);
            }
            Buffer buffer = state.active;
            if (buffer != null) {
                long first = buffer.claim(count - offset);
                if (first > 0) {
                    int claimed = (int) Math.min(count - offset, buffer.last - first + 1);
                    for (int i = 0; i < claimed; i++) {
                        result[offset++] = first + i;
                    }
                    if (buffer.remaining() <= buffer.size / 5) {
                        prefetch(state);
                    }
                    continue;
                }
            }
            CompletableFuture<Void> future;
            synchronized (state) {
                if (state.active != null && state.active.remaining() > 0) {
                    continue;
                }
                if (state.standby != null) {
                    state.active = state.standby;
                    state.standby = null;
                    continue;
                }
                if (state.exhausted) {
                    throw new CommonException(DatabaseResultCodeEnum.EXHAUSTED);
                }
                future = schedule(state);
            }
            try {
                long remaining = deadline - System.nanoTime();
                if (remaining <= 0) {
                    throw new TimeoutException();
                }
                future.get(remaining, TimeUnit.NANOSECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new CommonException(DatabaseResultCodeEnum.UNAVAILABLE, e);
            } catch (TimeoutException e) {
                throw new CommonException(DatabaseResultCodeEnum.TIMEOUT, e);
            } catch (ExecutionException e) {
                if (e.getCause() instanceof CommonException failure) {
                    throw failure;
                }
                throw new CommonException(DatabaseResultCodeEnum.UNAVAILABLE, e.getCause());
            }
        }
        if (issued != null) {
            issued.increment(count);
        }
        return result;
    }

    private State state(SequenceScope scope) {
        State existing = states.get(scope.key());
        if (existing != null) {
            check(existing, scope);
            return existing;
        }
        synchronized (scopeMonitor) {
            if (closed) {
                throw new CommonException(DatabaseResultCodeEnum.CLOSED);
            }
            existing = states.get(scope.key());
            if (existing != null) {
                check(existing, scope);
                return existing;
            }
            if (states.size() >= properties.getMaxScopes()) {
                LocalDate today = LocalDate.now(clock);
                states.entrySet()
                        .removeIf(entry -> entry.getValue().scope.date() != null
                                && entry.getValue().scope.date().isBefore(today));
            }
            if (states.size() >= properties.getMaxScopes()) {
                throw new CommonException(DatabaseResultCodeEnum.SCOPE_LIMIT);
            }
            State created = new State(scope);
            states.put(scope.key(), created);
            return created;
        }
    }

    private void check(State state, SequenceScope scope) {
        if (state.scope.maximum() != scope.maximum()) {
            throw new CommonException(DatabaseResultCodeEnum.CONFIG_CONFLICT);
        }
    }

    private void prefetch(State state) {
        if (closed || state.exhausted || state.standby != null || state.loading != null || coolingDown(state)) {
            return;
        }
        synchronized (state) {
            if (!closed
                    && !state.exhausted
                    && state.standby == null
                    && state.loading == null
                    && states.get(state.scope.key()) == state
                    && !coolingDown(state)) {
                schedule(state);
            }
        }
    }

    private boolean coolingDown(State state) {
        return state.lastFailure != null && System.nanoTime() - state.retryAfter < 0;
    }

    private CompletableFuture<Void> schedule(State state) {
        if (state.loading != null) {
            return state.loading;
        }
        if (state.lastFailure != null && coolingDown(state)) {
            return CompletableFuture.failedFuture(state.lastFailure);
        }
        CompletableFuture<Void> future = new CompletableFuture<>();
        state.loading = future;
        pending.add(future);
        future.whenComplete((ignored, failure) -> pending.remove(future));
        if (closed) {
            state.loading = null;
            future.completeExceptionally(new CommonException(DatabaseResultCodeEnum.CLOSED));
            return future;
        }
        try {
            executor.execute(() -> {
                long started = System.nanoTime();
                try {
                    SequenceRange range = allocator.reserve(state.scope, properties.getStep());
                    if (range.last() > state.scope.maximum()) {
                        throw new CommonException(DatabaseResultCodeEnum.CORRUPT_STATE);
                    }
                    synchronized (state) {
                        if (!closed) {
                            if (state.active == null) {
                                state.active = new Buffer(range);
                            } else {
                                state.standby = new Buffer(range);
                            }
                        }
                        state.loading = null;
                        state.lastFailure = null;
                        if (range.last() == state.scope.maximum()) {
                            state.exhausted = true;
                        }
                    }
                    future.complete(null);
                } catch (Exception failure) {
                    synchronized (state) {
                        state.loading = null;
                        state.lastFailure = failure;
                        state.retryAfter = System.nanoTime() + TimeUnit.MILLISECONDS.toNanos(250);
                        if (failure instanceof CommonException common
                                && common.getResultCode() == DatabaseResultCodeEnum.EXHAUSTED) {
                            state.exhausted = true;
                        }
                    }
                    if (!failures.isEmpty()) {
                        failures.get(
                                        failure instanceof CommonException common
                                                        && common.getResultCode() instanceof DatabaseResultCodeEnum code
                                                ? code
                                                : DatabaseResultCodeEnum.UNAVAILABLE)
                                .increment();
                    }
                    future.completeExceptionally(failure);
                } finally {
                    if (refill != null) {
                        refill.record(System.nanoTime() - started, TimeUnit.NANOSECONDS);
                    }
                }
            });
        } catch (RejectedExecutionException rejected) {
            state.loading = null;
            state.lastFailure = new CommonException(DatabaseResultCodeEnum.UNAVAILABLE, rejected);
            state.retryAfter = System.nanoTime() + TimeUnit.MILLISECONDS.toNanos(250);
            if (!failures.isEmpty()) {
                failures.get(DatabaseResultCodeEnum.UNAVAILABLE).increment();
            }
            future.completeExceptionally(state.lastFailure);
        }
        return future;
    }

    @Override
    public void close() {
        closed = true;
        pending.forEach(future -> future.completeExceptionally(new CommonException(DatabaseResultCodeEnum.CLOSED)));
        executor.shutdownNow();
        states.clear();
    }

    private static final class State {
        final SequenceScope scope;
        volatile Buffer active;
        volatile Buffer standby;
        volatile CompletableFuture<Void> loading;
        volatile boolean exhausted;
        volatile long retryAfter;
        volatile Throwable lastFailure;

        State(SequenceScope scope) {
            this.scope = scope;
        }
    }

    private static final class Buffer {
        final long last, size;
        final AtomicLong cursor;

        Buffer(SequenceRange range) {
            last = range.last();
            size = last - range.first() + 1;
            cursor = new AtomicLong(range.first());
        }

        long remaining() {
            return Math.max(0, last - cursor.get() + 1);
        }

        long claim(int requested) {
            while (true) {
                long start = cursor.get();
                if (start > last) {
                    return -1;
                }
                long count = Math.min(requested, last - start + 1);
                if (cursor.compareAndSet(start, start + count)) {
                    return start;
                }
            }
        }
    }
}
