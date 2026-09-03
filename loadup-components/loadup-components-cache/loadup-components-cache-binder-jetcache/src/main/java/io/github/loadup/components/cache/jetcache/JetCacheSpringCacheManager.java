package io.github.loadup.components.cache.jetcache;

/*-
 * #%L
 * Loadup Cache Binder JetCache
 * %%
 * Copyright (C) 2025 - 2026 loadup_cloud
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

import com.alicp.jetcache.Cache;
import com.alicp.jetcache.template.QuickConfig;
import io.github.loadup.components.cache.LoadupCacheProperties;
import java.time.Duration;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import org.springframework.cache.CacheManager;
import org.springframework.cache.support.AbstractCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;

/**
 * Spring {@link CacheManager} that lazily creates {@link JetCacheSpringCache} adapters on top of the
 * JetCache {@code CacheManager}. Each Spring cache name maps to one JetCache cache created through
 * {@link QuickConfig}, so multi-level layout, local limit, TTL and null policy are applied per name.
 */
public class JetCacheSpringCacheManager extends AbstractCacheManager {

    private final com.alicp.jetcache.CacheManager jetCacheManager;
    private final LoadupCacheProperties properties;
    private final JetCacheCacheProperties binderProperties;
    private final Function<Object, Object> keyConvertor;
    private final Function<Object, byte[]> valueEncoder;
    private final Function<byte[], Object> valueDecoder;
    private final RedisConnectionFactory connectionFactory;
    private final String remoteKeyPrefix;

    public JetCacheSpringCacheManager(
            com.alicp.jetcache.CacheManager jetCacheManager,
            LoadupCacheProperties properties,
            JetCacheCacheProperties binderProperties,
            Function<Object, Object> keyConvertor,
            Function<Object, byte[]> valueEncoder,
            Function<byte[], Object> valueDecoder,
            RedisConnectionFactory connectionFactory,
            String remoteKeyPrefix) {
        this.jetCacheManager = jetCacheManager;
        this.properties = properties;
        this.binderProperties = binderProperties;
        this.keyConvertor = keyConvertor;
        this.valueEncoder = valueEncoder;
        this.valueDecoder = valueDecoder;
        this.connectionFactory = connectionFactory;
        this.remoteKeyPrefix = remoteKeyPrefix;
    }

    @Override
    protected Collection<org.springframework.cache.Cache> loadCaches() {
        return List.of();
    }

    @Override
    protected org.springframework.cache.Cache getMissingCache(String name) {
        return createSpringCache(name);
    }

    /** Pre-creates every cache declared in {@code loadup.cache.caches} at startup. */
    public void preloadConfiguredCaches() {
        properties.getCaches().keySet().forEach(this::getCache);
    }

    private org.springframework.cache.Cache createSpringCache(String name) {
        Duration ttl = properties.ttl(name, properties.getDefaultTtl());
        Duration jitter = properties.randomExpirationRange(name);
        QuickConfig.Builder builder = QuickConfig.newBuilder(name)
                .cacheType(binderProperties.cacheType(name))
                .localLimit(binderProperties.localLimit(name))
                .syncLocal(binderProperties.isSyncLocal())
                .cacheNullValue(false)
                .keyConvertor(keyConvertor)
                .valueEncoder(valueEncoder)
                .valueDecoder(valueDecoder);
        if (ttl != null) {
            builder.expire(ttl);
        }
        if (binderProperties.isPenetrationProtect()) {
            builder.penetrationProtect(true);
        }
        Cache<Object, Object> jetCache = jetCacheManager.getOrCreateCache(builder.build());
        return new JetCacheSpringCache(
                name, jetCache, properties.allowNullValues(name), ttl, jitter, connectionFactory, remoteKeyPrefix);
    }
}
