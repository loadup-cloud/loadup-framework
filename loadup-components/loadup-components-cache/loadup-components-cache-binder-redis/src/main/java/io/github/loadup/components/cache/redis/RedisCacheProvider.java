package io.github.loadup.components.cache.redis;

import io.github.loadup.components.cache.CacheProvider;
import java.time.Duration;
import java.util.Collection;
import java.util.concurrent.TimeUnit;
import org.springframework.data.redis.core.RedisTemplate;

public class RedisCacheProvider implements CacheProvider {
    private final RedisTemplate<String, byte[]> redisTemplate;

    public RedisCacheProvider(RedisTemplate<String, byte[]> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public byte[] get(String key) {
        return redisTemplate.opsForValue().get(key);
    }

    @Override
    public void set(String key, byte[] value, Duration ttl) {
        if (ttl != null && !ttl.isZero() && !ttl.isNegative()) {
            redisTemplate.opsForValue().set(key, value, ttl.toMillis(), TimeUnit.MILLISECONDS);
        } else {
            redisTemplate.opsForValue().set(key, value);
        }
    }

    @Override
    public boolean delete(String key) {
        return Boolean.TRUE.equals(redisTemplate.delete(key));
    }

    @Override
    public boolean deleteAll(Collection<String> keys) {
        Long count = redisTemplate.delete(keys);
        return count != null && count > 0;
    }

    @Override
    public void cleanUp() {
        // Redis 无需本地清理
    }

    @Override
    public String getBinderType() {
        return "redis";
    }
}
