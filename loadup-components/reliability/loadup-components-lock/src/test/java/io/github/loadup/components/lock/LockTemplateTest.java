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

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.transaction.support.TransactionSynchronizationManager;

class LockTemplateTest {
    private final RedissonClient client = mock(RedissonClient.class);
    private final RLock lock = mock(RLock.class);
    private final LockKey key = LockKey.tenant("payment", "tenant", "order");
    private final LockOptions defaults = LockOptions.watchdog(Duration.ofSeconds(1));
    private final SimpleMeterRegistry metrics = new SimpleMeterRegistry();
    private final LockTemplate template = new RedissonLockTemplate(client, "payments", defaults, metrics);

    @AfterEach
    void resetThreadState() {
        Thread.interrupted();
        TransactionSynchronizationManager.clear();
        metrics.close();
    }

    @Test
    void watchdogAcquiresAndReleasesAroundBusinessWithoutClosingSharedClient() throws Exception {
        when(client.getLock(key.redisName("payments"))).thenReturn(lock);
        when(lock.tryLock(1000, TimeUnit.MILLISECONDS)).thenReturn(true);
        String result = template.execute(key, () -> "done");
        assertThat(result).isEqualTo("done");
        var order = inOrder(lock);
        order.verify(lock).tryLock(1000, TimeUnit.MILLISECONDS);
        order.verify(lock).unlock();
        verify(client, never()).shutdown();
        assertThat(metrics.get("loadup.lock.acquire")
                        .tag("outcome", "success")
                        .timer()
                        .count())
                .isEqualTo(1);
        assertThat(metrics.get("loadup.lock.hold")
                        .tag("outcome", "success")
                        .timer()
                        .count())
                .isEqualTo(1);
        assertThat(metrics.getMeters())
                .allSatisfy(meter -> assertThat(meter.getId().getTags())
                        .noneMatch(tag ->
                                tag.getKey().equals("key") || tag.getKey().equals("tenant")));
    }

    @Test
    void fixedLeaseUsesExplicitLeaseOverload() throws Exception {
        when(client.getLock(anyString())).thenReturn(lock);
        when(lock.tryLock(0, 5000, TimeUnit.MILLISECONDS)).thenReturn(true);
        template.run(key, LockOptions.fixed(Duration.ZERO, Duration.ofSeconds(5)), () -> {});
        verify(lock).tryLock(0, 5000, TimeUnit.MILLISECONDS);
        verify(lock).unlock();
        verify(lock, never()).tryLock(anyLong(), any(TimeUnit.class));
    }

    @Test
    void timeoutDoesNotRunBusinessOrUnlock() throws Exception {
        when(client.getLock(anyString())).thenReturn(lock);
        var called = new AtomicBoolean();
        assertThatThrownBy(() -> template.run(key, () -> called.set(true)))
                .isInstanceOf(LockUnavailableException.class);
        assertThat(called).isFalse();
        verify(lock, never()).unlock();
        assertThat(metrics.get("loadup.lock.acquire")
                        .tag("outcome", "failure")
                        .tag("reason", "timeout")
                        .timer()
                        .count())
                .isEqualTo(1);
    }

    @Test
    void interruptedWaitRestoresFlagAndDoesNotRunBusiness() throws Exception {
        when(client.getLock(anyString())).thenReturn(lock);
        when(lock.tryLock(anyLong(), any(TimeUnit.class))).thenThrow(new InterruptedException("cancelled"));
        assertThatThrownBy(() -> template.run(key, () -> fail("must not execute")))
                .isInstanceOf(InterruptedException.class);
        assertThat(Thread.currentThread().isInterrupted()).isTrue();
        verify(lock, never()).unlock();
    }

    @Test
    void alreadyInterruptedCallerNeverTouchesRedis() {
        Thread.currentThread().interrupt();
        assertThatThrownBy(() -> template.run(key, () -> fail("must not execute")))
                .isInstanceOf(InterruptedException.class);
        verifyNoInteractions(client);
    }

    @Test
    void acquisitionFailureDoesNotRunBusinessOrPretendItIsContention() {
        when(client.getLock(anyString())).thenThrow(new IllegalStateException("connection failed"));
        assertThatThrownBy(() -> template.run(key, () -> fail("must not execute")))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("connection failed");
        verify(lock, never()).unlock();
        assertThat(metrics.get("loadup.lock.acquire")
                        .tag("outcome", "failure")
                        .tag("reason", "error")
                        .timer()
                        .count())
                .isEqualTo(1);
    }

    @Test
    void checkedBusinessFailureRetainsReleaseFailureAsSuppressed() throws Exception {
        when(client.getLock(anyString())).thenReturn(lock);
        when(lock.tryLock(anyLong(), any(TimeUnit.class))).thenReturn(true);
        var primary = new IOException("business");
        var release = new IllegalMonitorStateException("expired");
        doThrow(release).when(lock).unlock();
        assertThatThrownBy(() -> template.execute(key, () -> {
                    throw primary;
                }))
                .isSameAs(primary)
                .hasSuppressedException(release);
        assertThat(metrics.get("loadup.lock.release.failures").counter().count())
                .isEqualTo(1);
        assertThat(metrics.get("loadup.lock.hold")
                        .tag("outcome", "failure")
                        .timer()
                        .count())
                .isEqualTo(1);
        verify(lock, never()).forceUnlock();
    }

    @Test
    void successfulBusinessStillFailsIfOwnershipWasLost() throws Exception {
        when(client.getLock(anyString())).thenReturn(lock);
        when(lock.tryLock(anyLong(), any(TimeUnit.class))).thenReturn(true);
        doThrow(new IllegalMonitorStateException("expired")).when(lock).unlock();
        assertThatThrownBy(() -> template.execute(key, () -> "done")).isInstanceOf(IllegalMonitorStateException.class);
    }

    @Test
    void existingTransactionIsRejectedBeforeAnyRemoteOperation() {
        TransactionSynchronizationManager.setActualTransactionActive(true);
        assertThatThrownBy(() -> template.run(key, () -> fail("must not execute")))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("before starting the transaction");
        verifyNoInteractions(client);
    }
}
