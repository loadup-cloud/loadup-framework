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
package io.github.loadup.components.cache.test.redis;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import io.github.loadup.components.cache.LoadupCacheProperties;
import io.github.loadup.components.cache.autoconfigure.LoadupCacheAutoConfiguration;
import io.github.loadup.components.cache.redis.RandomTtlFunction;
import io.github.loadup.components.cache.redis.autoconfigure.RedisCacheAutoConfiguration;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.cache.autoconfigure.CacheManagerCustomizer;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.cache.CacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.cache.support.NoOpCacheManager;
import org.springframework.data.redis.cache.RedisCache;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;

class RedisCacheConfigurationTest {
    private final ApplicationContextRunner contexts = new ApplicationContextRunner()
            .withConfiguration(
                    AutoConfigurations.of(LoadupCacheAutoConfiguration.class, RedisCacheAutoConfiguration.class))
            .withBean(RedisConnectionFactory.class, () -> mock(RedisConnectionFactory.class))
            .withPropertyValues("loadup.cache.type=redis");

    @Test
    void emptyPerCacheConfigurationRetainsDefaultTtlAndConfiguredKeyPrefix() {
        contexts.withPropertyValues("loadup.cache.default-ttl=30s", "loadup.cache.binder.redis.key-prefix=tenant:")
                .run(context -> {
                    var manager = context.getBean(RedisCacheManager.class);
                    assertThat(manager.getCacheNames()).isEmpty();
                    var configuration = ((RedisCache) manager.getCache("orders")).getCacheConfiguration();
                    assertThat(configuration.getKeyPrefixFor("orders")).isEqualTo("tenant:orders::");
                    assertThat(configuration.getTtlFunction().getTimeToLive("key", "value"))
                            .isEqualTo(Duration.ofSeconds(30));
                });
    }

    @Test
    void perCacheOptionalFieldsRetainDefaultsAndExplicitNullPoliciesOverride() {
        contexts.withPropertyValues(
                        "loadup.cache.default-ttl=30s",
                        "loadup.cache.caches.inherited.allow-null-values=true",
                        "loadup.cache.caches.strict.allow-null-values=false",
                        "loadup.cache.caches.jitter.random-expiration-range=1s",
                        "loadup.cache.caches.timed.ttl=5s")
                .run(context -> {
                    var manager = context.getBean(RedisCacheManager.class);
                    assertThat(((RedisCache) manager.getCache("inherited"))
                                    .getCacheConfiguration()
                                    .getAllowCacheNullValues())
                            .isTrue();
                    assertThat(((RedisCache) manager.getCache("strict"))
                                    .getCacheConfiguration()
                                    .getAllowCacheNullValues())
                            .isFalse();
                    assertThat(((RedisCache) manager.getCache("inherited"))
                                    .getCacheConfiguration()
                                    .getTtlFunction()
                                    .getTimeToLive("key", "value"))
                            .isEqualTo(Duration.ofSeconds(30));
                    assertThat(((RedisCache) manager.getCache("timed"))
                                    .getCacheConfiguration()
                                    .getTtlFunction()
                                    .getTimeToLive("key", "value"))
                            .isEqualTo(Duration.ofSeconds(5));
                    assertThat(context.getBean(LoadupCacheProperties.class).getCaches())
                            .hasSize(4);
                });
    }

    @Test
    void customManagerAndBackendSelectorControlAssembly() {
        var custom = new ConcurrentMapCacheManager();
        contexts.withBean(CacheManager.class, () -> custom)
                .run(context -> assertThat(context.getBean(CacheManager.class)).isSameAs(custom));
        contexts.withPropertyValues("loadup.cache.type=none")
                .withBean(CacheManager.class, NoOpCacheManager::new)
                .run(context -> assertThat(context).doesNotHaveBean(RedisCacheManager.class));
    }

    @Test
    void appliesConsumerManagerCustomizer() {
        var called = new AtomicBoolean();
        contexts.withBean("redisCustomizer", CacheManagerCustomizer.class, () ->
                        (CacheManagerCustomizer<RedisCacheManager>) manager -> called.set(true))
                .run(context -> {
                    assertThat(context).hasSingleBean(RedisCacheManager.class);
                    assertThat(called).isTrue();
                });
    }

    @Test
    void expirationFunctionPreservesPersistentAndFinitePolicies() {
        assertThat(new RandomTtlFunction(null, Duration.ofSeconds(10)).getTimeToLive("key", null))
                .isEqualTo(Duration.ZERO);
        assertThat(new RandomTtlFunction(Duration.ofSeconds(5), null).getTimeToLive("key", "value"))
                .isEqualTo(Duration.ofSeconds(5));
        assertThat(new RandomTtlFunction(Duration.ofSeconds(5), Duration.ofSeconds(1)).getTimeToLive("key", "value"))
                .isBetween(Duration.ofSeconds(5), Duration.ofSeconds(6));
    }
}
