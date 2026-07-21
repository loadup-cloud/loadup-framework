package io.github.loadup.components.cache;

import java.time.Duration;

public interface CacheTemplate {
    <T> T get(String key, Class<T> type);
    void set(String key, Object value, Duration ttl);
    <T> T getAndLoad(String key, Class<T> type, java.util.function.Supplier<T> loader, Duration ttl);
    boolean delete(String key);
    void cleanUp();
}
