package io.github.loadup.components.cache.redis;

import io.github.loadup.components.cache.RandomExpiration;
import java.time.Duration;
import org.springframework.data.redis.cache.RedisCacheWriter;

/**
 * Spring Data Redis {@link RedisCacheWriter.TtlFunction} computing {@code ttl + random[0, range]}
 * per key on every write. Random expiration spreads cache expiry over time to avoid avalanche.
 */
public final class RandomTtlFunction implements RedisCacheWriter.TtlFunction {

    private final Duration base;
    private final Duration jitter;

    public RandomTtlFunction(Duration base, Duration jitter) {
        this.base = base;
        this.jitter = jitter;
    }

    @Override
    public Duration getTimeToLive(Object key, Object value) {
        Duration ttl = RandomExpiration.apply(base, jitter);
        return ttl == null ? NO_EXPIRATION : ttl;
    }
}
