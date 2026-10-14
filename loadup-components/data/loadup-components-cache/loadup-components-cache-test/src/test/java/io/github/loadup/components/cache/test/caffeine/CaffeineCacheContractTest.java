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
package io.github.loadup.components.cache.test.caffeine;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.github.benmanes.caffeine.cache.Caffeine;
import io.github.loadup.components.cache.CacheNameSettings;
import io.github.loadup.components.cache.LoadupCacheProperties;
import io.github.loadup.components.cache.autoconfigure.LoadupCacheAutoConfiguration;
import io.github.loadup.components.cache.caffeine.CaffeineCacheProperties;
import io.github.loadup.components.cache.caffeine.LoadupCaffeineCacheManager;
import io.github.loadup.components.cache.caffeine.autoconfigure.CaffeineCacheAutoConfiguration;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.cache.autoconfigure.CacheManagerCustomizer;
import org.springframework.boot.test.context.FilteredClassLoader;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.cache.support.NoOpCacheManager;

class CaffeineCacheContractTest {
    private final ApplicationContextRunner runner = new ApplicationContextRunner()
            .withConfiguration(
                    AutoConfigurations.of(LoadupCacheAutoConfiguration.class, CaffeineCacheAutoConfiguration.class));

    @Test
    void springCacheOperationsPreserveNullAndPutIfAbsentSemantics() {
        Cache cache = new LoadupCaffeineCacheManager(new LoadupCacheProperties(), 100).getCache("values");
        assertThat(cache.get("missing")).isNull();
        cache.put("null", null);
        assertThat(cache.get("null")).isNotNull();
        assertThat(cache.get("null").get()).isNull();
        assertThat(cache.putIfAbsent("key", "first")).isNull();
        assertThat(cache.putIfAbsent("key", "second").get()).isEqualTo("first");
        assertThat(cache.get("key", String.class)).isEqualTo("first");
        cache.evict("key");
        assertThat(cache.get("key")).isNull();
        cache.put("other", "value");
        cache.clear();
        assertThat(cache.get("other")).isNull();
        assertThat(cache.get("null")).isNull();
    }

    @Test
    void perCacheNullPolicyRejectsNullAndZeroTtlExpiresImmediately() {
        LoadupCacheProperties properties = new LoadupCacheProperties();
        properties.setDefaultTtl(Duration.ofMinutes(5));
        properties.setCaches(Map.of("ephemeral", new CacheNameSettings(Duration.ZERO, null, false)));
        Cache cache = new LoadupCaffeineCacheManager(properties, 100).getCache("ephemeral");
        assertThatThrownBy(() -> cache.put("null", null)).isInstanceOf(IllegalArgumentException.class);
        cache.put("key", "value");
        assertThat(cache.get("key")).isNull();
    }

    @Test
    void variableExpiryHonorsJitterAndDoesNotRefreshOnReadOrUpdate() {
        LoadupCacheProperties properties = new LoadupCacheProperties();
        properties.setDefaultTtl(Duration.ofMinutes(5));
        properties.setCaches(
                Map.of("jitter", new CacheNameSettings(Duration.ofMinutes(2), Duration.ofSeconds(10), true)));
        LoadupCaffeineCacheManager manager = new LoadupCaffeineCacheManager(properties, 100);
        CaffeineCache cache = (CaffeineCache) manager.getCache("jitter");
        cache.put("key", "first");
        var expiration = cache.getNativeCache().policy().expireVariably().orElseThrow();
        Duration initial = expiration.getExpiresAfter("key").orElseThrow();
        assertThat(initial).isBetween(Duration.ofSeconds(119), Duration.ofSeconds(130));
        assertThat(cache.get("key", String.class)).isEqualTo("first");
        cache.put("key", "updated");
        assertThat(expiration.getExpiresAfter("key").orElseThrow()).isLessThanOrEqualTo(initial);
        assertThat(cache.get("key", String.class)).isEqualTo("updated");
        CaffeineCache defaultCache = (CaffeineCache) manager.getCache("default");
        defaultCache.put("key", "value");
        assertThat(defaultCache
                        .getNativeCache()
                        .policy()
                        .expireVariably()
                        .orElseThrow()
                        .getExpiresAfter("key")
                        .orElseThrow())
                .isBetween(Duration.ofSeconds(299), Duration.ofMinutes(5));
    }

    @Test
    void jitterWithoutBaseTtlDoesNotInventAnExpiration() {
        LoadupCacheProperties properties = new LoadupCacheProperties();
        properties.setCaches(Map.of("persistent", new CacheNameSettings(null, Duration.ofSeconds(2), null)));
        CaffeineCache cache = (CaffeineCache) new LoadupCaffeineCacheManager(properties, 100).getCache("persistent");
        cache.put("key", "value");
        assertThat(cache.getNativeCache()
                        .policy()
                        .expireVariably()
                        .orElseThrow()
                        .getExpiresAfter("key")
                        .orElseThrow())
                .isGreaterThan(Duration.ofDays(365));
    }

    @Test
    void defaultBinderBindsPoliciesCapacityAndInvokesCustomizer() {
        runner.withPropertyValues(
                        "loadup.cache.type=caffeine",
                        "loadup.cache.default-ttl=5m",
                        "loadup.cache.default-allow-null-values=false",
                        "loadup.cache.binder.caffeine.maximum-size=7",
                        "loadup.cache.caches.allowed.ttl=1m",
                        "loadup.cache.caches.allowed.allow-null-values=true")
                .withBean(
                        "namedCaches",
                        CacheManagerCustomizer.class,
                        () -> (CacheManagerCustomizer<LoadupCaffeineCacheManager>)
                                manager -> manager.setCacheNames(List.of("allowed")))
                .run(context -> {
                    assertThat(context).hasNotFailed().hasSingleBean(CacheManager.class);
                    LoadupCaffeineCacheManager manager = context.getBean(LoadupCaffeineCacheManager.class);
                    assertThat(manager.getCache("unknown")).isNull();
                    CaffeineCache cache = (CaffeineCache) manager.getCache("allowed");
                    cache.put("key", null);
                    assertThat(cache.get("key")).isNotNull();
                    assertThat(cache.getNativeCache()
                                    .policy()
                                    .eviction()
                                    .orElseThrow()
                                    .getMaximum())
                            .isEqualTo(7);
                    assertThat(context.getBean(CaffeineCacheProperties.class).getMaximumSize())
                            .isEqualTo(7);
                    assertThat(context.getBean(LoadupCacheProperties.class).getDefaultTtl())
                            .isEqualTo(Duration.ofMinutes(5));
                });
    }

    @Test
    void defaultBackendAndDefaultCapacityAreUsable() {
        runner.run(context -> {
            assertThat(context).hasNotFailed().hasSingleBean(LoadupCaffeineCacheManager.class);
            CaffeineCache cache =
                    (CaffeineCache) context.getBean(CacheManager.class).getCache("default");
            assertThat(cache.getNativeCache().policy().eviction().orElseThrow().getMaximum())
                    .isEqualTo(10_000);
            assertThat(cache.getNativeCache().policy().expireVariably()).isEmpty();
        });
    }

    @Test
    void consumerManagerWinsAndOtherBackendDoesNotActivateCaffeine() {
        CacheManager supplied = new NoOpCacheManager();
        runner.withBean(CacheManager.class, () -> supplied).run(context -> {
            assertThat(context).hasNotFailed().hasSingleBean(CacheManager.class);
            assertThat(context.getBean(CacheManager.class)).isSameAs(supplied);
        });
        runner.withPropertyValues("loadup.cache.type=redis")
                .withBean(CacheManager.class, () -> supplied)
                .run(context -> assertThat(context).hasNotFailed().doesNotHaveBean(CaffeineCacheProperties.class));
    }

    @Test
    void absentCaffeineLibraryDoesNotRegisterBinderProperties() {
        runner.withClassLoader(new FilteredClassLoader(Caffeine.class))
                .withBean(CacheManager.class, NoOpCacheManager::new)
                .run(context -> assertThat(context).hasNotFailed().doesNotHaveBean(CaffeineCacheProperties.class));
    }
}
