package com.github.loadup.components.cache.caffeine;

import com.github.loadup.components.cache.api.CacheBinder;
import com.github.loadup.components.cache.constans.CacheConstants;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.util.Assert;

import java.util.Objects;

public class CaffeineCacheBinderImpl implements CacheBinder {

    @Resource
    @Qualifier("defaultCacheManager")
    private CacheManager defaultCacheManager;

    @Override
    public String getName() {
        return "CaffeineCache";
    }

    @Override
    public boolean set(String key, Object value, int exp) {
        Cache cache = defaultCacheManager.getCache(CacheConstants.DEFAULT_CACHE_NAME);
        Assert.notNull(cache, "cache is null");
        cache.putIfAbsent(key, value);
        return true;
    }

    @Override
    public Object get(String key) {
        Cache cache = defaultCacheManager.getCache(CacheConstants.DEFAULT_CACHE_NAME);
        Assert.notNull(cache, "cache is null");
        Cache.ValueWrapper valueWrapper = cache.get(key);
        if (Objects.isNull(valueWrapper)) {
            return null;
        }
        return valueWrapper.get();
    }

    @Override
    public <T> T get(String key, Class<T> clazz) {
        Cache cache = defaultCacheManager.getCache(CacheConstants.DEFAULT_CACHE_NAME);
        Assert.notNull(cache, "cache is null");
        T value = cache.get(key, clazz);
        if (Objects.isNull(value)) {
            return null;
        }
        return value;
    }

    @Override
    public boolean delete(String key) {
        Cache cache = defaultCacheManager.getCache(CacheConstants.DEFAULT_CACHE_NAME);
        Assert.notNull(cache, "cache is null");
        cache.evict(key);
        return true;
    }

    @Override
    public boolean deleteAll() {
        Cache cache = defaultCacheManager.getCache(CacheConstants.DEFAULT_CACHE_NAME);
        Assert.notNull(cache, "cache is null");
        cache.clear();
        return true;
    }


}
