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

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.loadup.commons.util.JsonUtil;
import com.github.loadup.components.cache.binder.AbstractCacheBinder;
import com.github.loadup.components.cache.binder.CacheBinder;
import com.github.loadup.components.cache.caffeine.cfg.CaffeineCacheBinderCfg;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.Assert;

import java.util.Collection;

@Slf4j
public class CaffeineCacheBinder extends AbstractCacheBinder<CaffeineCacheBinderCfg>
    implements CacheBinder {

  // 每一个 Binder 实例持有一个物理上的 Caffeine Cache 对象
  private Cache<String, Object> nativeCache;

  @Override
  public String getBinderType() {
    return "caffeine";
  }

  @Override
  protected void onInit() {
    // 这里的 binderCfg 是根据具体的 bizTag 路由过来的
    // 比如 loadup.cache.bindings.user-cache -> 对应的驱动配置

    Caffeine<Object, Object> builder = Caffeine.newBuilder();
    builder.ticker(ticker::read);
    // 1. 设置最大容量
    if (binderCfg.getMaximumSize() > 0) {
      builder.maximumSize(binderCfg.getMaximumSize());
    }

    // 2. 设置过期策略 (真正实现不同 bizType 不同策略的关键)
    if (binderCfg.getExpireAfterWrite() != null) {
      builder.expireAfterWrite(binderCfg.getExpireAfterWrite());
    }

    this.nativeCache = builder.build();
    log.info(
        "Caffeine Binder [{}] 初始化成功: max={}, expire={}",
        name,
        binderCfg.getMaximumSize(),
        binderCfg.getExpireAfterWrite());
  }

  @Override
  public boolean set(String key, Object value) {
    Assert.notNull(value, "Caffeine cache does not support null values");
    nativeCache.put(key, wrapValue(value));
    return true;
  }

  @Override
  public Object get(String key) {
    Object valueWrapper = nativeCache.getIfPresent(key);
    // 如果配置了序列化器，且存入的是字节数组，则反序列化（实现深拷贝保护）
    if (valueWrapper instanceof byte[] && serializer != null) {
      return serializer.deserialize((byte[]) valueWrapper, Object.class);
    }
    return valueWrapper;
  }

  @Override
  public boolean delete(String key) {
    nativeCache.invalidate(key);
    return true;
  }

  @Override
  public boolean deleteAll(Collection<String> keys) {
    nativeCache.invalidateAll(keys);
    return true;
  }

  @Override
  public void cleanUp() {
    nativeCache.cleanUp();
  }

  @Override
  protected void afterDestroy() {
    if (nativeCache != null) {
      nativeCache.cleanUp();
      log.info("Caffeine Binder [{}] 已销毁", name);
    }
  }
}
