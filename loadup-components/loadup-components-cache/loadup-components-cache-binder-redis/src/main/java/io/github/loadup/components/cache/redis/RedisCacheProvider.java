package io.github.loadup.components.cache.redis;

/*-
 * #%L
 * Loadup Cache Binder Redis
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
