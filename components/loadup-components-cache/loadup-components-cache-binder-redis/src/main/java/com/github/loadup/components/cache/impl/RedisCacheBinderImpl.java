package com.github.loadup.components.cache.impl;

/*-
 * #%L
 * loadup-components-cache-binder-redis
 * %%
 * Copyright (C) 2022 - 2025 loadup_cloud
 * %%
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 * #L%
 */

import com.github.loadup.components.cache.api.CacheBinder;
import com.github.loadup.components.cache.constans.CacheConstants;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.Cache;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.util.Assert;

import java.util.Objects;

public class RedisCacheBinderImpl implements CacheBinder {

    @Resource
    @Qualifier("redisCacheManager")
    RedisCacheManager redisCacheManager;

    @Override
    public String getName() {
        return "RedisCache";
    }


    @Override
    public boolean set(String key, Object value, int exp) {
        Cache cache = redisCacheManager.getCache(CacheConstants.DEFAULT_CACHE_NAME);
        Assert.notNull(cache, "cache is null");
        cache.putIfAbsent(key, value);
        return true;
    }

    @Override
    public Object get(String key) {
        Cache cache = redisCacheManager.getCache(CacheConstants.DEFAULT_CACHE_NAME);
        Assert.notNull(cache, "cache is null");
        Cache.ValueWrapper valueWrapper = cache.get(key);
        if (Objects.isNull(valueWrapper)) {
            return null;
        }
        return valueWrapper.get();
    }

    @Override
    public <T> T get(String key, Class<T> clazz) {
        Cache cache = redisCacheManager.getCache(CacheConstants.DEFAULT_CACHE_NAME);
        Assert.notNull(cache, "cache is null");
        T value = cache.get(key, clazz);
        if (Objects.isNull(value)) {
            return null;
        }
        return value;
    }

    @Override
    public boolean delete(String key) {
        Cache cache = redisCacheManager.getCache(CacheConstants.DEFAULT_CACHE_NAME);
        Assert.notNull(cache, "cache is null");
        cache.evict(key);
        return true;
    }

    @Override
    public boolean deleteAll() {
        Cache cache = redisCacheManager.getCache(CacheConstants.DEFAULT_CACHE_NAME);
        Assert.notNull(cache, "cache is null");
        cache.clear();
        return true;
    }
}
