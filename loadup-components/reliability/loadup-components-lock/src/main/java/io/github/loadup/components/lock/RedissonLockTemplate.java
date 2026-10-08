/*
 * #%L
 * LoadUp Lock
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
package io.github.loadup.components.lock;

import io.github.loadup.commons.log.LogUtil;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/** Delegates lock ownership and lease renewal to Redisson; never silently skips release failures. */
public final class RedissonLockTemplate implements LockTemplate {
    private final RedissonClient client;
    private final String namespace;
    private final LockOptions defaults;
    private final MeterRegistry metrics;
    private final Map<String, Timer> acquisitionTimers;
    private final Map<String, Timer> holdTimers;

    public RedissonLockTemplate(RedissonClient client, String namespace, LockOptions defaults, MeterRegistry metrics) {
        this.client = Objects.requireNonNull(client, "Redisson client");
        this.namespace = LockKey.required(namespace, "namespace", 128);
        this.defaults = Objects.requireNonNull(defaults, "lock options");
        this.metrics = metrics;
        this.acquisitionTimers = timers(metrics, "loadup.lock.acquire", "acquired", "timeout", "interrupted", "error");
        this.holdTimers = timers(metrics, "loadup.lock.hold", "success", "failure");
        if (metrics != null) metrics.counter("loadup.lock.release.failures");
    }

    @Override
    public <T, X extends Throwable> T execute(LockKey key, ScopedValue.CallableOp<T, X> action)
            throws X, InterruptedException {
        return execute(key, defaults, action);
    }

    @Override
    public <T, X extends Throwable> T execute(LockKey key, LockOptions options, ScopedValue.CallableOp<T, X> action)
            throws X, InterruptedException {
        Objects.requireNonNull(key, "lock key");
        Objects.requireNonNull(options, "lock options");
        Objects.requireNonNull(action, "action");
        if (TransactionSynchronizationManager.isActualTransactionActive())
            throw new IllegalStateException(
                    "Acquire the lock before starting the transaction; release it after commit");
        long start = System.nanoTime();
        RLock lock;
        boolean acquired;
        try {
            if (Thread.currentThread().isInterrupted()) throw new InterruptedException("Lock acquisition interrupted");
            lock = client.getLock(key.redisName(namespace));
            long waitMillis = options.waitTimeout().toMillis();
            acquired = options.leaseMode() == LockOptions.LeaseMode.WATCHDOG
                    ? lock.tryLock(waitMillis, TimeUnit.MILLISECONDS)
                    : lock.tryLock(waitMillis, options.leaseTime().toMillis(), TimeUnit.MILLISECONDS);
        } catch (InterruptedException interrupted) {
            Thread.currentThread().interrupt();
            record(acquisitionTimers, "interrupted", start);
            throw interrupted;
        } catch (RuntimeException | Error failure) {
            record(acquisitionTimers, "error", start);
            throw failure;
        }
        if (!acquired) {
            record(acquisitionTimers, "timeout", start);
            throw new LockUnavailableException();
        }
        try (HeldLock held = new HeldLock(lock)) {
            record(acquisitionTimers, "acquired", start);
            T result = action.call();
            held.success = true;
            return result;
        }
    }

    private final class HeldLock implements AutoCloseable {
        private final RLock lock;
        private final long start = System.nanoTime();
        private boolean success;

        private HeldLock(RLock lock) {
            this.lock = lock;
        }

        @Override
        public void close() {
            boolean released = false;
            try {
                lock.unlock();
                released = true;
            } catch (RuntimeException | Error failure) {
                if (metrics != null) {
                    try {
                        metrics.counter("loadup.lock.release.failures").increment();
                    } catch (RuntimeException ignored) {
                        LogUtil.warn(RedissonLockTemplate.class, "Lock release metric recording failed", ignored);
                    }
                }
                throw failure;
            } finally {
                record(holdTimers, success && released ? "success" : "failure", start);
            }
        }
    }

    private static Map<String, Timer> timers(MeterRegistry metrics, String name, String... outcomes) {
        if (metrics == null) return Map.of();
        var timers = new HashMap<String, Timer>();
        for (String outcome : outcomes) {
            boolean acquisition = name.equals("loadup.lock.acquire");
            var builder = Timer.builder(name)
                    .tag("outcome", acquisition ? (outcome.equals("acquired") ? "success" : "failure") : outcome);
            if (acquisition) builder.tag("reason", outcome.equals("acquired") ? "none" : outcome);
            timers.put(outcome, builder.register(metrics));
        }
        return Map.copyOf(timers);
    }

    private static void record(Map<String, Timer> timers, String outcome, long start) {
        Timer timer = timers.get(outcome);
        if (timer == null) return;
        try {
            timer.record(System.nanoTime() - start, TimeUnit.NANOSECONDS);
        } catch (RuntimeException failure) {
            LogUtil.warn(RedissonLockTemplate.class, "Lock timer recording failed", failure);
        }
    }
}
