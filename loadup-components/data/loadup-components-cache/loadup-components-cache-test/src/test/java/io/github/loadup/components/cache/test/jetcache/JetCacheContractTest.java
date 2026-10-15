/*
 * #%L
 * Loadup Cache Test
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
package io.github.loadup.components.cache.test.jetcache;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.alicp.jetcache.Cache;
import com.alicp.jetcache.CacheInvokeException;
import com.alicp.jetcache.MultiLevelCache;
import com.alicp.jetcache.anno.CacheType;
import com.alicp.jetcache.embedded.CaffeineCacheBuilder;
import io.github.loadup.components.cache.autoconfigure.LoadupCacheAutoConfiguration;
import io.github.loadup.components.cache.jetcache.JetCacheCacheProperties;
import io.github.loadup.components.cache.jetcache.JetCacheCacheSettings;
import io.github.loadup.components.cache.jetcache.JetCacheSpringCache;
import io.github.loadup.components.cache.jetcache.JetCacheSpringCacheManager;
import io.github.loadup.components.cache.jetcache.autoconfigure.JetCacheCacheAutoConfiguration;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.cache.support.NoOpCacheManager;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.ScanOptions;

class JetCacheContractTest {
    @Test
    void localFacadePreservesLoaderNullPutIfAbsentAndEvictionContracts() {
        for (Duration ttl : new Duration[] {null, Duration.ofMinutes(1)}) {
            Cache<Object, Object> nativeCache =
                    CaffeineCacheBuilder.createCaffeineCacheBuilder().limit(100).buildCache();
            try (nativeCache) {
                var cache = new JetCacheSpringCache(
                        "orders", nativeCache, true, ttl, Duration.ofSeconds(1), null, "tenant:");
                assertThat(cache.getName()).isEqualTo("orders");
                assertThat(cache.getNativeCache()).isSameAs(nativeCache);
                var loads = new AtomicInteger();
                assertThat(cache.get("loaded", () -> "value-" + loads.incrementAndGet()))
                        .isEqualTo("value-1");
                assertThat(cache.get("loaded", () -> "value-" + loads.incrementAndGet()))
                        .isEqualTo("value-1");
                assertThat(loads).hasValue(1);
                assertThat(cache.putIfAbsent("first", "original")).isNull();
                assertThat(cache.putIfAbsent("first", "replacement").get()).isEqualTo("original");
                cache.put("nullable", null);
                assertThat(cache.get("nullable")).isNotNull();
                assertThat(cache.get("nullable").get()).isNull();
                assertThat(cache.putIfAbsent("new-null", null)).isNull();
                assertThat(cache.get("new-null")).isNotNull();
                cache.evict("loaded");
                assertThat(cache.get("loaded")).isNull();
                cache.clear();
                assertThat(cache.get("first")).isNull();
            }
        }
    }

    @Test
    void strictNullPolicyRejectsBothWriteFormsAndLoaderFailuresRetainCause() {
        for (Duration ttl : new Duration[] {null, Duration.ofMinutes(1)}) {
            Cache<Object, Object> nativeCache =
                    CaffeineCacheBuilder.createCaffeineCacheBuilder().limit(100).buildCache();
            try (nativeCache) {
                var cache = new JetCacheSpringCache("strict", nativeCache, false, ttl, null, null, "tenant:");
                assertThatThrownBy(() -> cache.put("key", null)).isInstanceOf(IllegalArgumentException.class);
                assertThatThrownBy(() -> cache.putIfAbsent("key", null)).isInstanceOf(IllegalArgumentException.class);
                cache.put("key", "value");
                assertThat(cache.get("key", String.class)).isEqualTo("value");
                Object empty = cache.get("empty", () -> null);
                assertThat(empty).isNull();
                assertThat(cache.get("empty")).isNull();
                var failure = new IOException("Loader unavailable");
                assertThatThrownBy(() -> cache.get("failure", () -> {
                            throw failure;
                        }))
                        .isInstanceOf(org.springframework.cache.Cache.ValueRetrievalException.class)
                        .hasCause(failure);
            }
        }
    }

    @Test
    @SuppressWarnings("unchecked")
    void nonLoaderBackendFailurePropagatesWithoutBeingMisclassified() {
        Cache<Object, Object> backend = mock(Cache.class);
        var failure = new CacheInvokeException(new IOException("Backend unavailable"));
        when(backend.computeIfAbsent(eq("key"), any(), eq(false))).thenThrow(failure);
        var cache = new JetCacheSpringCache("orders", backend, false, null, null, null, "tenant:");
        assertThatThrownBy(() -> cache.get("key", () -> "value")).isSameAs(failure);
    }

    @Test
    @SuppressWarnings("unchecked")
    void remoteClearScansOnlyItsNamespaceAndDeletesFullAndPartialBatches() {
        var connection = mock(RedisConnection.class);
        var factory = mock(RedisConnectionFactory.class);
        when(factory.getConnection()).thenReturn(connection);
        Cursor<byte[]> cursor = mock(Cursor.class);
        var keys = new ArrayList<byte[]>();
        for (int i = 0; i < 501; i++) {
            keys.add(("tenant:orders::" + i).getBytes(StandardCharsets.UTF_8));
        }
        var iterator = keys.iterator();
        when(cursor.hasNext()).thenAnswer(invocation -> iterator.hasNext());
        when(cursor.next()).thenAnswer(invocation -> iterator.next());
        when(connection.scan(any(ScanOptions.class))).thenReturn(cursor);
        Cache<Object, Object> delegate = mock(Cache.class);
        var cache = new JetCacheSpringCache("orders", delegate, false, null, null, factory, "tenant:");
        cache.clear();
        var options = ArgumentCaptor.forClass(ScanOptions.class);
        verify(connection).scan(options.capture());
        assertThat(options.getValue().getPattern()).isEqualTo("tenant:orders::*");
        var batches = ArgumentCaptor.forClass(byte[][].class);
        verify(connection, times(2)).del(batches.capture());
        assertThat(batches.getAllValues()).extracting(batch -> batch.length).containsExactly(500, 1);
        verify(cursor).close();
        verify(connection).close();
    }

    @Test
    @SuppressWarnings("unchecked")
    void multiLevelClearInvalidatesLocalLevelAndClosesAnEmptyRemoteScan() {
        Cache<Object, Object> local =
                CaffeineCacheBuilder.createCaffeineCacheBuilder().limit(100).buildCache();
        try (local) {
            local.put("key", "value");
            MultiLevelCache<Object, Object> levels = mock(MultiLevelCache.class);
            when(levels.caches()).thenReturn(new Cache[] {local, mock(Cache.class)});
            var factory = mock(RedisConnectionFactory.class);
            var connection = mock(RedisConnection.class);
            Cursor<byte[]> cursor = mock(Cursor.class);
            when(factory.getConnection()).thenReturn(connection);
            when(connection.scan(any(ScanOptions.class))).thenReturn(cursor);
            new JetCacheSpringCache("orders", levels, false, null, null, factory, "tenant:").clear();
            assertThat(local.get("key")).isNull();
            verify(cursor).close();
            verify(connection).close();
            new JetCacheSpringCache("orders", mock(Cache.class), false, null, null, null, "tenant:").clear();
        }
    }

    @Test
    void typedPropertiesResolvePerNameOverridesWithoutDiscardingDefaults() {
        var settings = new JetCacheCacheProperties();
        settings.setDefaultCacheType("remote");
        settings.setLocalLimit(99);
        settings.setSyncLocal(false);
        settings.setPenetrationProtect(true);
        settings.setRemoteKeyPrefix("tenant:");
        settings.setCaches(Map.of(
                "local", new JetCacheCacheSettings("local", 7), "inherit", new JetCacheCacheSettings(null, null)));
        assertThat(settings.getDefaultCacheType()).isEqualTo("remote");
        assertThat(settings.getLocalLimit()).isEqualTo(99);
        assertThat(settings.isSyncLocal()).isFalse();
        assertThat(settings.isPenetrationProtect()).isTrue();
        assertThat(settings.getRemoteKeyPrefix()).isEqualTo("tenant:");
        assertThat(settings.getCaches()).hasSize(2);
        assertThat(settings.cacheType("local")).isEqualTo(CacheType.LOCAL);
        assertThat(settings.localLimit("local")).isEqualTo(7);
        assertThat(settings.cacheType("inherit")).isEqualTo(CacheType.REMOTE);
        assertThat(settings.localLimit("inherit")).isEqualTo(99);
        assertThat(settings.cacheType("absent")).isEqualTo(CacheType.REMOTE);
        assertThat(settings.localLimit("absent")).isEqualTo(99);
    }

    @Test
    void localAutoConfigurationAppliesPenetrationProtectionPreloadsAndBacksOff() {
        var contexts = new ApplicationContextRunner()
                .withConfiguration(
                        AutoConfigurations.of(LoadupCacheAutoConfiguration.class, JetCacheCacheAutoConfiguration.class))
                .withPropertyValues(
                        "loadup.cache.type=jetcache",
                        "loadup.cache.binder.jetcache.penetration-protect=true",
                        "loadup.cache.caches.orders.allow-null-values=false");
        contexts.run(context -> {
            assertThat(context).hasSingleBean(JetCacheSpringCacheManager.class);
            var manager = context.getBean(JetCacheSpringCacheManager.class);
            assertThat(manager.getCacheNames()).contains("orders");
            var orders = manager.getCache("orders");
            assertThat(orders.get("key", () -> "value")).isEqualTo("value");
            manager.preloadConfiguredCaches();
            assertThat(manager.getCacheNames()).contains("orders");
            assertThat(manager.getCache("orders")).isSameAs(orders);
            assertThat(orders.get("key", String.class)).isEqualTo("value");
        });
        var custom = new ConcurrentMapCacheManager();
        contexts.withBean(org.springframework.cache.CacheManager.class, () -> custom)
                .run(context -> assertThat(context.getBean(org.springframework.cache.CacheManager.class))
                        .isSameAs(custom));
        contexts.withPropertyValues("loadup.cache.type=none")
                .withBean(org.springframework.cache.CacheManager.class, NoOpCacheManager::new)
                .run(context -> assertThat(context).doesNotHaveBean(JetCacheSpringCacheManager.class));
    }
}
