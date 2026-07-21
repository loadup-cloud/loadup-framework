package io.github.loadup.components.cache.caffeine;

/*-
 * #%L
 * Loadup Cache Binder Caffeine
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
