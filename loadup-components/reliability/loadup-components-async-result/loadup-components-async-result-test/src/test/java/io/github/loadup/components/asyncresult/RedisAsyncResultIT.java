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

import io.github.loadup.components.asyncresult.redis.RedisAsyncResultProvider;
import io.github.loadup.components.testcontainers.annotation.ContainerType;
import io.github.loadup.components.testcontainers.annotation.EnableTestContainers;
import io.github.loadup.components.testcontainers.cache.SharedRedisContainer;
import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(classes = RedisAsyncResultIT.Application.class)
@ActiveProfiles("test")
@EnableTestContainers(value = ContainerType.REDIS, reuse = false)
class RedisAsyncResultIT {
    static RedissonClient client() {
        var config = new Config();
        config.useSingleServer().setAddress(SharedRedisContainer.getUrl());
        return Redisson.create(config);
    }

    @Test
    void independentClientsCompleteAcrossInstancesAndPreserveClientOwnership() throws Exception {
        var a = client();
        var b = client();
        String namespace = UUID.randomUUID().toString();
        try (var first = AsyncResultTest.template(
                        new RedisAsyncResultProvider(a, namespace),
                        AsyncResultTest.settings(10, Duration.ofSeconds(4)));
                var second = AsyncResultTest.template(
                        new RedisAsyncResultProvider(b, namespace),
                        AsyncResultTest.settings(10, Duration.ofSeconds(4)))) {
            var issued = new CompletableFuture<AsyncReplyKey>();
            var waiting = first.executeAsync(AsyncResultTest.options(), String.class, k -> {
                issued.complete(k);
                return AsyncResultTest.sent();
            });
            var key = issued.get(3, TimeUnit.SECONDS);
            assertThat(second.complete(key, "remote")).isEqualTo(AsyncCompletionStatus.ACCEPTED);
            assertThat(second.complete(key, "remote")).isEqualTo(AsyncCompletionStatus.DUPLICATE);
            assertThat(second.complete(key, "other")).isEqualTo(AsyncCompletionStatus.CONFLICT);
            assertThat(waiting.get(3, TimeUnit.SECONDS)).isEqualTo("remote");
            assertThat(second.complete(
                            new AsyncReplyKey("other", key.operation(), key.businessId(), key.correlationId()),
                            "wrong"))
                    .isEqualTo(AsyncCompletionStatus.UNKNOWN);
        } finally {
            assertThat(a.isShutdown()).isFalse();
            assertThat(b.isShutdown()).isFalse();
            a.shutdown();
            b.shutdown();
        }
    }

    @Test
    void redisRetentionCannotBeExtendedByDuplicatesAndNamespaceIsIsolated() throws Exception {
        var client = client();
        try {
            var p = new RedisAsyncResultProvider(client, UUID.randomUUID().toString());
            var key = LocalAsyncResultProviderTest.key();
            assertThat(p.find(key)).isEmpty();
            assertThat(p.complete(key, new AsyncResultValue("1", null))).isEqualTo(AsyncCompletionStatus.UNKNOWN);
            p.register(key, "owner", Duration.ofSeconds(3));
            assertThatThrownBy(() -> p.register(key, "owner", Duration.ofSeconds(3)))
                    .isInstanceOf(IllegalArgumentException.class);
            assertThat(p.find(key)).isEmpty();
            assertThat(p.complete(key, new AsyncResultValue(null, "NOT_FOUND")))
                    .isEqualTo(AsyncCompletionStatus.ACCEPTED);
            assertThat(p.find(key).orElseThrow().failureCode()).isEqualTo("NOT_FOUND");
            assertThat(new RedisAsyncResultProvider(client, "separate").find(key))
                    .isEmpty();
            var expired = LocalAsyncResultProviderTest.key();
            p.register(expired, "owner", Duration.ofMillis(100));
            Thread.sleep(150);
            assertThat(p.complete(expired, new AsyncResultValue("1", null))).isEqualTo(AsyncCompletionStatus.UNKNOWN);
            assertThatThrownBy(() -> p.register(key, "owner", Duration.ZERO))
                    .isInstanceOf(IllegalArgumentException.class);
        } finally {
            client.shutdown();
        }
    }

    @SpringBootConfiguration
    static class Application {}
}
