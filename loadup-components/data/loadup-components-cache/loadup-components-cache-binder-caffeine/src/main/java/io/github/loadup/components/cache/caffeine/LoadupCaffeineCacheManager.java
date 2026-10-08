package io.github.loadup.components.cache.caffeine;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.Expiry;
import io.github.loadup.components.cache.CacheNameSettings;
import io.github.loadup.components.cache.LoadupCacheProperties;
import io.github.loadup.components.cache.RandomExpiration;
import java.time.Duration;
import java.util.concurrent.TimeUnit;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.cache.caffeine.CaffeineCacheManager;

/**
 * {@link CaffeineCacheManager} that applies per-cache TTL, random expiration (anti-avalanche) and
 * null-value policy from {@link LoadupCacheProperties}. Caches without a per-name configuration use
 * the component defaults.
 */
public class LoadupCaffeineCacheManager extends CaffeineCacheManager {

    private final LoadupCacheProperties properties;
    private final long maximumSize;

    public LoadupCaffeineCacheManager(LoadupCacheProperties properties, long maximumSize) {
        this.properties = properties;
        this.maximumSize = maximumSize;
    }

    @Override
    protected CaffeineCache createCaffeineCache(String name) {
        return new CaffeineCache(name, createNativeCaffeineCache(name), properties.allowNullValues(name));
    }

    @Override
    protected Cache<Object, Object> createNativeCaffeineCache(String name) {
        CacheNameSettings settings = properties.cacheSettings(name);
        Duration ttl = settings != null && settings.ttl() != null ? settings.ttl() : properties.getDefaultTtl();
        Duration jitter = settings != null ? settings.randomExpirationRange() : null;
        Caffeine<Object, Object> builder = Caffeine.newBuilder().maximumSize(maximumSize);
        if (ttl != null || jitter != null) {
            builder.expireAfter(new RandomExpiry(ttl, jitter));
        }
        return builder.build();
    }

    /** Caffeine {@link Expiry} that adds a random jitter on top of the configured TTL per entry. */
    private static final class RandomExpiry implements Expiry<Object, Object> {

        private static final long MAX_NANOS = Long.MAX_VALUE;

        private final Duration ttl;
        private final Duration jitter;

        private RandomExpiry(Duration ttl, Duration jitter) {
            this.ttl = ttl;
            this.jitter = jitter;
        }

        @Override
        public long expireAfterCreate(Object key, Object value, long currentTime) {
            Duration effective = RandomExpiration.apply(ttl, jitter);
            return effective == null ? MAX_NANOS - currentTime : TimeUnit.MILLISECONDS.toNanos(effective.toMillis());
        }

        @Override
        public long expireAfterUpdate(Object key, Object value, long currentTime, long currentDuration) {
            return currentDuration;
        }

        @Override
        public long expireAfterRead(Object key, Object value, long currentTime, long currentDuration) {
            return currentDuration;
        }
    }
}
