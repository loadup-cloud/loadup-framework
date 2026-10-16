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
package io.github.loadup.components.asyncresult.local;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.Expiry;
import com.github.benmanes.caffeine.cache.Scheduler;
import io.github.loadup.commons.error.CommonException;
import io.github.loadup.components.asyncresult.*;
import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;
import java.util.function.Consumer;

public final class LocalAsyncResultProvider implements AsyncResultProvider, AutoCloseable {
    private final Cache<AsyncReplyKey, Entry> records;
    private final ConcurrentHashMap<String, Consumer<AsyncReplyKey>> listeners = new ConcurrentHashMap<>();
    private final Semaphore slots;

    public LocalAsyncResultProvider(int maxRetained) {
        if (maxRetained < 1) throw new IllegalArgumentException("Positive retention capacity is required");
        slots = new Semaphore(maxRetained);
        records = Caffeine.newBuilder()
                .executor(Runnable::run)
                .scheduler(Scheduler.systemScheduler())
                .expireAfter(new Expiry<AsyncReplyKey, Entry>() {
                    public long expireAfterCreate(AsyncReplyKey key, Entry value, long now) {
                        return value.retention.toNanos();
                    }

                    public long expireAfterUpdate(AsyncReplyKey key, Entry value, long now, long duration) {
                        return duration;
                    }

                    public long expireAfterRead(AsyncReplyKey key, Entry value, long now, long duration) {
                        return duration;
                    }
                })
                .removalListener((key, value, cause) -> slots.release())
                .build();
    }

    @Override
    public void register(AsyncReplyKey key, String owner, Duration retention) {
        AsyncReplyKey.required(owner, 128);
        if (retention.isNegative() || retention.isZero() || retention.compareTo(Duration.ofDays(7)) > 0)
            throw new IllegalArgumentException("Invalid result retention");
        records.cleanUp();
        if (!slots.tryAcquire()) throw new CommonException(AsyncResultCodeEnum.CAPACITY_EXCEEDED);
        if (records.asMap().putIfAbsent(key, new Entry(owner, retention)) != null) {
            slots.release();
            throw new IllegalArgumentException("Reply key is already registered");
        }
    }

    @Override
    public Optional<AsyncResultValue> find(AsyncReplyKey key) {
        var entry = records.getIfPresent(key);
        return entry == null ? Optional.empty() : Optional.ofNullable(entry.value);
    }

    @Override
    public AsyncCompletionStatus complete(AsyncReplyKey key, AsyncResultValue value) {
        java.util.Objects.requireNonNull(value);
        var entry = records.getIfPresent(key);
        if (entry == null) return AsyncCompletionStatus.UNKNOWN;
        synchronized (entry) {
            if (System.nanoTime() - entry.created >= entry.retention.toNanos()) return AsyncCompletionStatus.UNKNOWN;
            if (entry.value != null)
                return entry.value.equals(value) ? AsyncCompletionStatus.DUPLICATE : AsyncCompletionStatus.CONFLICT;
            entry.value = value;
        }
        var listener = listeners.get(entry.owner);
        if (listener != null) listener.accept(key);
        return AsyncCompletionStatus.ACCEPTED;
    }

    @Override
    public AutoCloseable subscribe(String owner, Consumer<AsyncReplyKey> listener) {
        if (listeners.putIfAbsent(owner, listener) != null)
            throw new IllegalArgumentException("Owner is already subscribed");
        return () -> listeners.remove(owner, listener);
    }

    @Override
    public void close() {
        listeners.clear();
        records.invalidateAll();
        records.cleanUp();
    }

    private static final class Entry {
        final String owner;
        final Duration retention;
        final long created = System.nanoTime();
        volatile AsyncResultValue value;

        Entry(String owner, Duration retention) {
            this.owner = owner;
            this.retention = retention;
        }
    }
}
