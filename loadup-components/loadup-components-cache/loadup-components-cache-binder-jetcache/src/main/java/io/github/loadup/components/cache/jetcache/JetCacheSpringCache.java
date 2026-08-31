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
import com.alicp.jetcache.CacheGetResult;
import com.alicp.jetcache.CacheResult;
import com.alicp.jetcache.MultiLevelCache;
import com.alicp.jetcache.embedded.CaffeineCache;
import io.github.loadup.components.cache.RandomExpiration;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import org.springframework.cache.support.AbstractValueAdaptingCache;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.ScanOptions;

/**
 * Thin adapter exposing a JetCache {@link Cache} through the Spring Cache contract so that
 * {@code @Cacheable} / {@code @CacheEvict} / {@code @CachePut} work on the JetCache backend.
 *
 * <p>Per-key TTL plus random expiration (anti-avalanche) is applied on every write; when no TTL is
 * configured the JetCache cache default (from {@code QuickConfig.expire(...)}) is used.
 */
public class JetCacheSpringCache extends AbstractValueAdaptingCache {

    private final String name;
    private final Cache<Object, Object> delegate;
    private final Duration ttl;
    private final Duration jitter;
    private final RedisConnectionFactory connectionFactory;
    private final String remoteKeyPrefix;

    public JetCacheSpringCache(
            String name,
            Cache<Object, Object> delegate,
            boolean allowNullValues,
            Duration ttl,
            Duration jitter,
            RedisConnectionFactory connectionFactory,
            String remoteKeyPrefix) {
        super(allowNullValues);
        this.name = name;
        this.delegate = delegate;
        this.ttl = ttl;
        this.jitter = jitter;
        this.connectionFactory = connectionFactory;
        this.remoteKeyPrefix = remoteKeyPrefix;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Object getNativeCache() {
        return delegate;
    }

    @Override
    protected Object lookup(Object key) {
        CacheGetResult<Object> result = delegate.GET(key);
        return result.isSuccess() ? result.getValue() : null;
    }

    @Override
    public void put(Object key, Object value) {
        if (value == null && !isAllowNullValues()) {
            throw new IllegalArgumentException("Cache '" + name + "' is configured to not allow null values");
        }
        Object storeValue = toStoreValue(value);
        putWithTtl(key, storeValue);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T get(Object key, Callable<T> valueLoader) {
        Duration effective = RandomExpiration.apply(ttl, jitter);
        Object loaded;
        if (effective == null) {
            loaded = delegate.computeIfAbsent(key, k -> load(k, valueLoader), isAllowNullValues());
        } else {
            loaded = delegate.computeIfAbsent(
                    key, k -> load(k, valueLoader), isAllowNullValues(), effective.toMillis(), TimeUnit.MILLISECONDS);
        }
        return (T) fromStoreValue(loaded);
    }

    @Override
    public ValueWrapper putIfAbsent(Object key, Object value) {
        if (value == null && !isAllowNullValues()) {
            throw new IllegalArgumentException("Cache '" + name + "' is configured to not allow null values");
        }
        Object storeValue = toStoreValue(value);
        CacheResult result = putIfAbsentWithTtl(key, storeValue);
        return result.isSuccess() ? null : toValueWrapper(lookup(key));
    }

    @Override
    public void evict(Object key) {
        delegate.remove(key);
    }

    @Override
    public void clear() {
        Cache<Object, Object> target = delegate;
        if (target instanceof MultiLevelCache multiLevel) {
            for (Cache<?, ?> level : multiLevel.caches()) {
                clearLocalLevel(level);
            }
            deleteRemoteKeys();
        } else if (target instanceof CaffeineCache caffeineCache) {
            clearLocalLevel(caffeineCache);
        } else {
            deleteRemoteKeys();
        }
    }

    private void putWithTtl(Object key, Object value) {
        Duration effective = RandomExpiration.apply(ttl, jitter);
        if (effective == null) {
            delegate.put(key, value);
        } else {
            delegate.put(key, value, effective.toMillis(), TimeUnit.MILLISECONDS);
        }
    }

    private CacheResult putIfAbsentWithTtl(Object key, Object value) {
        Duration effective = RandomExpiration.apply(ttl, jitter);
        if (effective == null) {
            return delegate.putIfAbsent(key, value) ? CacheResult.SUCCESS_WITHOUT_MSG : CacheResult.EXISTS_WITHOUT_MSG;
        }
        return delegate.PUT_IF_ABSENT(key, value, effective.toMillis(), TimeUnit.MILLISECONDS);
    }

    private static void clearLocalLevel(Cache<?, ?> level) {
        if (level instanceof CaffeineCache caffeineCache) {
            @SuppressWarnings("unchecked")
            com.github.benmanes.caffeine.cache.Cache<Object, Object> nativeCache =
                    (com.github.benmanes.caffeine.cache.Cache<Object, Object>)
                            caffeineCache.unwrap(com.github.benmanes.caffeine.cache.Cache.class);
            nativeCache.invalidateAll();
        }
    }

    private static Object load(Object key, Callable<?> valueLoader) {
        try {
            return valueLoader.call();
        } catch (Exception ex) {
            throw new ValueRetrievalException(key, valueLoader, ex);
        }
    }

    private void deleteRemoteKeys() {
        if (connectionFactory == null) {
            return;
        }
        byte[] pattern = (remoteKeyPrefix + name + "::*").getBytes(StandardCharsets.UTF_8);
        try (RedisConnection connection = connectionFactory.getConnection()) {
            List<byte[]> batch = new ArrayList<>();
            try (Cursor<byte[]> cursor = connection.scan(
                    ScanOptions.scanOptions().match(pattern).count(500).build())) {
                while (cursor.hasNext()) {
                    batch.add(cursor.next());
                    if (batch.size() >= 500) {
                        connection.del(batch.toArray(new byte[0][]));
                        batch.clear();
                    }
                }
            }
            if (!batch.isEmpty()) {
                connection.del(batch.toArray(new byte[0][]));
            }
        }
    }
}
