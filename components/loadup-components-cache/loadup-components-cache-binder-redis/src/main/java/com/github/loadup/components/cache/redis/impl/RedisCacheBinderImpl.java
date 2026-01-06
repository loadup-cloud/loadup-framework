package com.github.loadup.components.cache.redis.impl;

/*-
 * #%L
 * loadup-components-cache-binder-redis
 * %%
 * Copyright (C) 2022 - 2025 loadup_cloud
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

import com.github.loadup.commons.util.JsonUtil;
import com.github.loadup.components.cache.api.CacheBinder;
import jakarta.annotation.Resource;
import java.util.Map;
import java.util.Objects;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.Cache;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

public class RedisCacheBinderImpl implements CacheBinder {

  @Resource
  @Qualifier("redisCacheManager")
  RedisCacheManager redisCacheManager;

  @Override
  public String getName() {
    return "RedisCache";
  }

  @Override
  public boolean set(String cacheName, String key, Object value) {
    Cache cache = redisCacheManager.getCache(cacheName);
    Assert.notNull(cache, "cache is null");
    cache.put(key, value);
    return true;
  }

  @Override
  public Object get(String cacheName, String key) {
    Cache cache = redisCacheManager.getCache(cacheName);
    if (Objects.isNull(cache)) {
      return null;
    }
    Cache.ValueWrapper valueWrapper = cache.get(key);
    if (Objects.isNull(valueWrapper)) {
      return null;
    }
    return valueWrapper.get();
  }

  @Override
  public <T> T get(String cacheName, String key, Class<T> clazz) {
    Cache cache = redisCacheManager.getCache(cacheName);
    if (Objects.isNull(cache)) {
      return null;
    }
    @SuppressWarnings("unchecked")
    Map<String, Object> map = cache.get(key, Map.class);
    if (CollectionUtils.isEmpty(map)) {
      return null;
    }
    return JsonUtil.mapToObject(map, clazz);
  }

  @Override
  public boolean delete(String cacheName, String key) {
    Cache cache = redisCacheManager.getCache(cacheName);
    if (Objects.isNull(cache)) {
      return false;
    }
    cache.evictIfPresent(key);
    return true;
  }

  @Override
  public boolean deleteAll(String cacheName) {
    Cache cache = redisCacheManager.getCache(cacheName);
    if (Objects.isNull(cache)) {
      return false;
    }
    cache.clear();
    return true;
  }
}
