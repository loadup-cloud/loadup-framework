package io.github.loadup.components.cache.caffeine;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.github.loadup.components.cache.CacheProvider;
import java.time.Duration;
import java.util.Collection;

public class CaffeineCacheProvider implements CacheProvider {
    private final Cache<String, byte[]> cache;

    public CaffeineCacheProvider(CaffeineCacheConfig config) {
        Caffeine<Object, Object> builder = Caffeine.newBuilder();
        if (config.getMaximumSize() > 0) {
            builder.maximumSize(config.getMaximumSize());
        }
        if (config.getExpireAfterWrite() != null) {
            builder.expireAfterWrite(config.getExpireAfterWrite());
        }
        if (config.getExpireAfterAccess() != null) {
            builder.expireAfterAccess(config.getExpireAfterAccess());
        }
        this.cache = builder.build();
    }

    @Override
    public byte[] get(String key) {
        return cache.getIfPresent(key);
    }

    @Override
    public void set(String key, byte[] value, Duration ttl) {
        cache.put(key, value);
    }

    @Override
    public boolean delete(String key) {
        cache.invalidate(key);
        return true;
    }

    @Override
    public boolean deleteAll(Collection<String> keys) {
        cache.invalidateAll(keys);
        return true;
    }

    @Override
    public void cleanUp() {
        cache.cleanUp();
    }

    @Override
    public String getBinderType() {
        return "caffeine";
    }
}
