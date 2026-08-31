package io.github.loadup.components.cache.jetcache;

/*-
 * #%L
 * Loadup Cache Binder JetCache
 * %%
 * Copyright (C) 2025 - 2026 loadup_cloud
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
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
    private final JetCacheCacheBinderProperties binderProperties;
    private final Function<Object, Object> keyConvertor;
    private final Function<Object, byte[]> valueEncoder;
    private final Function<byte[], Object> valueDecoder;
    private final RedisConnectionFactory connectionFactory;
    private final String remoteKeyPrefix;

    public JetCacheSpringCacheManager(
            com.alicp.jetcache.CacheManager jetCacheManager,
            LoadupCacheProperties properties,
            JetCacheCacheBinderProperties binderProperties,
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
