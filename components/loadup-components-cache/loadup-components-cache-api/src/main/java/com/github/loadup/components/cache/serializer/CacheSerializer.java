package com.github.loadup.components.cache.serializer;


public interface CacheSerializer {
    byte[] serialize(Object obj);
    <T> T deserialize(byte[] bytes, Class<T> clazz);
}
