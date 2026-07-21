package io.github.loadup.components.cache;

/*-
 * #%L
 * Loadup Cache Components API
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

import io.github.loadup.components.cache.serializer.CacheSerializer;
import java.time.Duration;
import java.util.function.Supplier;
import org.springframework.util.StringUtils;

public class DefaultCacheTemplate implements CacheTemplate {
    private final CacheProvider provider;
    private final CacheSerializer serializer;
    private final String keyPrefix;

    public DefaultCacheTemplate(CacheProvider provider, CacheSerializer serializer, String keyPrefix) {
        this.provider = provider;
        this.serializer = serializer;
        this.keyPrefix = keyPrefix != null ? keyPrefix : "";
    }

    @Override
    public <T> T get(String key, Class<T> type) {
        byte[] data = provider.get(decorateKey(key));
        if (data == null) return null;
        return serializer.deserialize(data, type);
    }

    @Override
    public void set(String key, Object value, Duration ttl) {
        byte[] data = serializer.serialize(value);
        provider.set(decorateKey(key), data, ttl);
    }

    @Override
    public <T> T getAndLoad(String key, Class<T> type, Supplier<T> loader, Duration ttl) {
        T cached = get(key, type);
        if (cached != null) return cached;
        T loaded = loader.get();
        if (loaded != null) set(key, loaded, ttl);
        return loaded;
    }

    @Override
    public boolean delete(String key) {
        return provider.delete(decorateKey(key));
    }

    @Override
    public void cleanUp() {
        provider.cleanUp();
    }

    private String decorateKey(String key) {
        return StringUtils.hasText(keyPrefix) ? keyPrefix + ":" + key : key;
    }
}
