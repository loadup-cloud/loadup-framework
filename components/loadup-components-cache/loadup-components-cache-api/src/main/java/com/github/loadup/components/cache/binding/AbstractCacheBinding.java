package com.github.loadup.components.cache.binding;

/*-
 * #%L
 * loadup-components-cache-api
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
import com.github.loadup.components.cache.binder.AbstractCacheBinder;
import com.github.loadup.components.cache.binder.CacheBinder;
import com.github.loadup.components.cache.cfg.CacheBindingCfg;
import com.github.loadup.components.cache.serializer.CacheSerializer;
import com.github.loadup.framework.api.binding.AbstractBinding;

import java.util.List;

public abstract class AbstractCacheBinding<B extends CacheBinder, C extends CacheBindingCfg>
    extends AbstractBinding<B, C> implements CacheBinding {

  @Override
  public boolean set(String cacheName, String key, Object value) {
    if (value == null) {
      return false;
    }

    // 获取所有绑定的驱动（可能有多级缓存）
    List<B> binders = getBinders();
    if (binders == null || binders.isEmpty()) {
      return false;
    }

    for (B binder : binders) {
      // 在这一层我们统一调用 binder.put
      // 具体的“对象 -> 字节数组”转换逻辑下沉到各个 Binder 内部处理
      // 这样可以实现：Caffeine 存对象，Redis 存字节
      binder.set(cacheName, key, value);
    }
    return true;
  }

  @Override
  public Object get(String cacheName, String key) {
    return getBinder().get(cacheName, key);
  }

  @Override
  @SuppressWarnings("unchecked")
  public <T> T get(String cacheName, String key, Class<T> clazz) {
    // 1. 获取第一个可用的 Binder（简单策略）
    B binder = getBinder();
    if (binder == null) return null;

    // 2. 调用 Binder 获取原始对象
    Object value = binder.get(cacheName, key);
    if (value == null) return null;

    // 3. 处理类型转换逻辑
    // 情况 A: 如果拿到的已经是目标类型（比如本地缓存存的是原始对象）
    if (clazz.isInstance(value)) {
      return (T) value;
    }

    // 情况 B: 如果拿到的字节数组（如来自 Redis 或序列化后的 Caffeine）
    if (value instanceof byte[]) {
      // 从当前 Binder 中获取它持有的序列化器
      // 注意：这里需要我们在 Binder 接口或基类中暴露获取 Serializer 的方法
      if (binder instanceof AbstractCacheBinder) {
        CacheSerializer serializer = ((AbstractCacheBinder<?>) binder).getSerializer();
        if (serializer != null) {
          return serializer.deserialize((byte[]) value, clazz);
        }
      }
    }

    // 情况 C: 兜底处理（如 JSON 工具类强转）
    return JsonUtil.convert(value, clazz);
  }

  @Override
  public boolean delete(String cacheName, String key) {
    return getBinder().delete(cacheName, key);
  }

  @Override
  public boolean deleteAll(String cacheName) {
    return getBinder().deleteAll(cacheName);
  }
}
