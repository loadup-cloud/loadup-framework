package io.github.loadup.components.cache;

import java.time.Duration;
import java.util.Collection;

public interface CacheProvider {
    byte[] get(String key);
    void set(String key, byte[] value, Duration ttl);
    boolean delete(String key);
    boolean deleteAll(Collection<String> keys);
    void cleanUp();
    String getBinderType();
}
