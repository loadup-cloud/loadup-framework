package com.github.loadup.components.cache.binding.impl;

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
import com.github.loadup.components.cache.binder.CacheBinder;
import com.github.loadup.components.cache.binding.CacheBinding;
import com.github.loadup.components.cache.cfg.CacheBindingCfg;
import com.github.loadup.components.cache.model.CacheValueWrapper;
import com.github.loadup.framework.api.binding.AbstractBinding;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

/** 通用的缓存业务绑定实现 职责：Key 修饰、多级缓存协调、类型转换、反序列化调度 */
@Slf4j
public class DefaultCacheBinding
    extends AbstractBinding<CacheBinder<?, CacheBindingCfg>, CacheBindingCfg>
    implements CacheBinding {

  @Override
  public Object get(String key) {
    String finalKey = decorateKey(key);
    CacheValueWrapper o = getBinder().get(finalKey);
    return unwrapValue(o, Object.class);
  }

  @Override
  public <T> T getObject(String key, Class<T> clazz) {
    String finalKey = decorateKey(key);
    CacheValueWrapper o = getBinder().get(finalKey);
    return unwrapValue(o, clazz);
  }

  @Override
  public boolean set(String key, Object value) {
    if (value == null) return false;
    String finalKey = decorateKey(key);
    // 遍历所有 Binder 写入（如同时写入内存和 Redis）
    getBinder().set(finalKey, wrapValue(value));
    return true;
  }

  @Override
  public boolean delete(String key) {
    if (key == null) return true;
    String finalKey = decorateKey(key);
    getBinder().delete(finalKey);
    return true;
  }

  @Override
  public boolean deleteAll(Collection<String> keys) {
    if (keys == null || keys.isEmpty()) return false;
    List<String> finalKeys = keys.stream().map(this::decorateKey).collect(Collectors.toList());
    getBinder().deleteAll(finalKeys);
    return true;
  }

  @Override
  public void cleanUp() {
    getBinder().cleanUp();
  }

  /** 根据配置自动修饰 Key（加上 KeyPrefix） */
  private String decorateKey(String key) {
    String prefix = bindingCfg.getKeyPrefix();
    if (StringUtils.hasText(prefix)) {
      return prefix + ":" + key;
    }
    return key;
  }

  protected CacheValueWrapper<?> wrapValue(Object value) {
    CacheValueWrapper<?> wrapper;
    if (value == null) {
      wrapper = new CacheValueWrapper<>("NULL", null, null);
    } else if (value instanceof String s) {
      wrapper = new CacheValueWrapper<>("STR", s, String.class.getName());
    } else {
      wrapper = new CacheValueWrapper<>("OBJ", value, value.getClass().getName());
    }
    return wrapper;
  }

  public <T> T unwrapValue(CacheValueWrapper<?> value, Class<T> clazz) {
    return JsonUtil.convert(value.data(), clazz);
  }
}
