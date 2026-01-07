package com.github.loadup.components.cache.caffeine.binder;

/*-
 * #%L
 * loadup-components-cache-binder-caffeine
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

import com.github.loadup.components.cache.api.CacheBinder;
import jakarta.annotation.Resource;
import java.util.Objects;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.util.Assert;

public class CaffeineCacheBinder implements CacheBinder {

  @Resource
  @Qualifier("caffeineCacheManager")
  private CacheManager caffeineCacheManager;

  @Override
  public String type() {
    return "caffeine";
  }

  @Override
  public boolean set(String cacheName, String key, Object value) {
    Cache cache = caffeineCacheManager.getCache(cacheName);
    Assert.notNull(cache, "cache is null");
    Assert.notNull(value, "Caffeine cache does not support null values");
    cache.put(key, value);
    return true;
  }

  @Override
  public Object get(String cacheName, String key) {
    Cache cache = caffeineCacheManager.getCache(cacheName);
    Assert.notNull(cache, "cache is null");
    Cache.ValueWrapper valueWrapper = cache.get(key);
    if (Objects.isNull(valueWrapper)) {
      return null;
    }
    return valueWrapper.get();
  }

  @Override
  public <T> T get(String cacheName, String key, Class<T> clazz) {
    Cache cache = caffeineCacheManager.getCache(cacheName);
    Assert.notNull(cache, "cache is null");
    T value = cache.get(key, clazz);
    if (Objects.isNull(value)) {
      return null;
    }
    return value;
  }

  @Override
  public boolean delete(String cacheName, String key) {
    Cache cache = caffeineCacheManager.getCache(cacheName);
    Assert.notNull(cache, "cache is null");
    cache.evict(key);
    return true;
  }

  @Override
  public boolean deleteAll(String cacheName) {
    Cache cache = caffeineCacheManager.getCache(cacheName);
    Assert.notNull(cache, "cache is null");
    cache.clear();
    return true;
  }
}
