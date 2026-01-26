package io.github.loadup.components.cache.serializer;

import io.github.loadup.components.cache.model.CacheValueWrapper;

public interface CacheSerializer {
  byte[] serialize(CacheValueWrapper<?> obj);

  <T> CacheValueWrapper<T> deserialize(byte[] bytes, Class<T> typeRef);
}
