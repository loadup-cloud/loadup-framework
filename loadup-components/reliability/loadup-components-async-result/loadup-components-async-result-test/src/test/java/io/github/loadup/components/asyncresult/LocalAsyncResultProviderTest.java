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

import static org.assertj.core.api.Assertions.*;

import io.github.loadup.commons.error.CommonException;
import io.github.loadup.components.asyncresult.local.LocalAsyncResultProvider;
import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;

class LocalAsyncResultProviderTest {
    static AsyncReplyKey key() {
        return new AsyncReplyKey("tenant", "echo", "order", UUID.randomUUID().toString());
    }

    @Test
    void retentionCapacityDuplicateRegistrationAndSubscriptionLifecycle() throws Exception {
        try (var provider = new LocalAsyncResultProvider(1)) {
            var key = key();
            var notifications = new AtomicInteger();
            try (var subscription = provider.subscribe("owner", k -> notifications.incrementAndGet())) {
                assertThatThrownBy(() -> provider.subscribe("owner", k -> {}))
                        .isInstanceOf(IllegalArgumentException.class);
                provider.register(key, "owner", Duration.ofMillis(1000));
                assertThat(provider.find(key)).isEmpty();
                assertThatThrownBy(() -> provider.register(key(), "owner", Duration.ofSeconds(1)))
                        .isInstanceOf(CommonException.class);
                assertThat(provider.complete(key, new AsyncResultValue("1", null)))
                        .isEqualTo(AsyncCompletionStatus.ACCEPTED);
                assertThat(notifications).hasValue(1);
                Thread.sleep(1100);
                assertThat(provider.find(key)).isEmpty();
                assertThat(provider.complete(key, new AsyncResultValue("1", null)))
                        .isEqualTo(AsyncCompletionStatus.UNKNOWN);
                provider.register(key(), "owner", Duration.ofSeconds(1));
            }
        }
        try (var provider = new LocalAsyncResultProvider(3)) {
            var key = key();
            provider.register(key, "owner", Duration.ofSeconds(1));
            assertThatThrownBy(() -> provider.register(key, "owner", Duration.ofSeconds(1)))
                    .isInstanceOf(IllegalArgumentException.class);
            assertThat(provider.complete(key, new AsyncResultValue(null, "NOT_FOUND")))
                    .isEqualTo(AsyncCompletionStatus.ACCEPTED);
            assertThat(provider.complete(key, new AsyncResultValue(null, "NOT_FOUND")))
                    .isEqualTo(AsyncCompletionStatus.DUPLICATE);
            assertThat(provider.complete(key, new AsyncResultValue("2", null)))
                    .isEqualTo(AsyncCompletionStatus.CONFLICT);
            assertThatThrownBy(() -> provider.register(key(), "owner", Duration.ZERO))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Test
    void concurrentDifferentCompletionsHaveExactlyOneWinner() throws Exception {
        try (var provider = new LocalAsyncResultProvider(100);
                var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            var key = key();
            provider.register(key, "owner", Duration.ofSeconds(5));
            var tasks = new java.util.ArrayList<Future<AsyncCompletionStatus>>();
            for (int i = 0; i < 30; i++) {
                int value = i;
                tasks.add(executor.submit(
                        () -> provider.complete(key, new AsyncResultValue(String.valueOf(value), null))));
            }
            int accepted = 0;
            for (var task : tasks) if (task.get(2, TimeUnit.SECONDS) == AsyncCompletionStatus.ACCEPTED) accepted++;
            assertThat(accepted).isEqualTo(1);
            assertThat(provider.find(key)).isPresent();
        }
    }

    @Test
    void invalidRetentionCannotAllocateStorage() {
        assertThatThrownBy(() -> new LocalAsyncResultProvider(0)).isInstanceOf(IllegalArgumentException.class);
        try (var provider = new LocalAsyncResultProvider(1)) {
            for (var duration : java.util.List.of(Duration.ofMillis(-1), Duration.ofDays(8)))
                assertThatThrownBy(() -> provider.register(key(), "owner", duration))
                        .isInstanceOf(IllegalArgumentException.class);
            provider.register(key(), "owner", Duration.ofSeconds(1));
        }
    }
}
