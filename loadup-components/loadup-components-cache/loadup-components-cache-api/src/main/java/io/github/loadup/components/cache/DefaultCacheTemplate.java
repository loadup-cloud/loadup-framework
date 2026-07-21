package io.github.loadup.components.cache;

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
