package io.github.loadup.components.cache.serializer;

import io.github.loadup.commons.util.JsonUtil;

public class JsonCacheSerializer implements CacheSerializer {
    @Override
    public byte[] serialize(Object obj) {
        return JsonUtil.toJsonBytes(obj);
    }

    @Override
    public <T> T deserialize(byte[] bytes, Class<T> type) {
        return JsonUtil.parseObject(bytes, type);
    }
}
