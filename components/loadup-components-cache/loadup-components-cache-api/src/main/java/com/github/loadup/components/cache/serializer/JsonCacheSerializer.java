package com.github.loadup.components.cache.serializer;

import com.fasterxml.jackson.core.type.TypeReference;
import com.github.loadup.commons.util.JsonUtil;
import com.github.loadup.components.cache.model.CacheValueWrapper;
import lombok.extern.slf4j.Slf4j;

/**
 * @author lise
 * @version JsonCacheSerializer.java, v 0.1 2026年01月13日 18:01 lise
 */
@Slf4j
public class JsonCacheSerializer implements CacheSerializer {
  @Override
  public byte[] serialize(CacheValueWrapper<?> value) {
    return JsonUtil.toBytes(value);
  }

  @Override
  public <T> CacheValueWrapper<T> deserialize(byte[] bytes, Class<T> typeRef) {
    if (bytes == null || bytes.length == 0) {
      return new CacheValueWrapper<>("NULL", null, null);
    }
    CacheValueWrapper<T> wrapper = JsonUtil.fromBytes(bytes, new TypeReference<>() {});
    return wrapper;
  }
}
