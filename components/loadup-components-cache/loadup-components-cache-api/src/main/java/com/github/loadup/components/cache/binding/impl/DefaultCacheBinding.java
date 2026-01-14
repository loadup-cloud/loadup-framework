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

import com.github.loadup.components.cache.binder.CacheBinder;
import com.github.loadup.components.cache.binding.CacheBinding;
import com.github.loadup.components.cache.cfg.CacheBindingCfg;
import com.github.loadup.components.cache.serializer.CacheSerializer;
import com.github.loadup.framework.api.binding.AbstractBinding;
import com.github.loadup.framework.api.context.BindingContext;
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
  public void init(BindingContext<CacheBinder<?, CacheBindingCfg>, CacheBindingCfg> ctx) {
    // ctx 包含了刚才基类 captureAndCreate 捕获的所有对象
    this.setBinders(ctx.getBinders());
    this.setBindingCfg(ctx.getBindingCfg());

    // 你可以在这里做额外的初始化，比如打印日志或检查多级缓存顺序
    log.info("Binding [{}] initialized with {} binders", ctx.getBizTag(), ctx.getBinders().size());
  }

  @Override
  public Object get(String key) {
    String finalKey = decorateKey(key);
    // 遍历所有 Binder 写入（如同时写入内存和 Redis）
    return getBinder().get(finalKey);
  }

  @Override
  @SuppressWarnings("unchecked")
  public <T> T getObject(String key, Class<T> clazz) {
    String finalKey = decorateKey(key);

    // 1. 获取所有绑定的驱动（可能包含 Caffeine + Redis）
    List<CacheBinder<?, CacheBindingCfg>> binders = getBinders();
    if (binders == null || binders.isEmpty()) return null;

    Object value = null;
    CacheBinder<?, CacheBindingCfg> hitBinder = null;

    // 2. 依次查询（多级缓存逻辑：本地 -> 远程）
    for (CacheBinder<?, CacheBindingCfg> binder : binders) {
      value = binder.get(finalKey);
      if (value != null) {
        hitBinder = binder;
        break;
      }
    }

    if (value == null) return null;

    // 3. 结果处理与反序列化
    T result = transform(value, clazz, hitBinder.getSerializer());

    // 4. 回填逻辑（如果从二级缓存查到，回填到一级缓存）
    if (result != null && binders.size() > 1) {
      backfill(finalKey, result, hitBinder);
    }
    return result;
  }

  @Override
  public boolean set(String key, Object value) {
    if (value == null) return false;

    String finalKey = decorateKey(key);
    // 遍历所有 Binder 写入（如同时写入内存和 Redis）
    getBinders().forEach(binder -> binder.set(finalKey, value));
    return true;
  }

  @Override
  public boolean delete(String key) {
    String finalKey = decorateKey(key);
    getBinders().forEach(binder -> binder.delete(finalKey));
    return true;
  }

  @Override
  public boolean deleteAll(Collection<String> keys) {
    if (keys == null || keys.isEmpty()) return false;

    List<String> finalKeys = keys.stream().map(this::decorateKey).collect(Collectors.toList());

    getBinders().forEach(binder -> binder.deleteAll(finalKeys));
    return true;
  }

  @Override
  public void cleanUp() {}

  /** 根据配置自动修饰 Key（加上 KeyPrefix） */
  private String decorateKey(String key) {
    String prefix = bindingCfg.getKeyPrefix();
    if (StringUtils.hasText(prefix)) {
      return prefix + ":" + key;
    }
    return key;
  }

  /** 统一的类型转换逻辑 */
  @SuppressWarnings("unchecked")
  private <T> T transform(Object value, Class<T> clazz, CacheSerializer serializer) {
    // 情况 A: 类型直接匹配（如本地缓存存的 POJO）
    if (clazz.isInstance(value)) {
      return clazz.cast(value);
    }

    // 情况 B: 字节数组（来自 Redis 等），调用驱动配的序列化器
    if (value instanceof byte[] && serializer != null) {
      return serializer.deserialize((byte[]) value, clazz);
    }

    // 情况 C: 兜底逻辑（如从 Map 转 POJO）
    // 情况 C：兜底强转，并使用 @SuppressWarnings 压制警告
    // 这是必要的，因为编译器无法感知运行时的动态泛型
    try {
      return clazz.cast(value);
    } catch (ClassCastException e) {
      return (T) value;
    }
  }

  /** 简单的多级缓存回填逻辑 */
  private <T> void backfill(String key, T value, CacheBinder<?, CacheBindingCfg> hitBinder) {
    List<CacheBinder<?, CacheBindingCfg>> binders = getBinders();
    if (binders.size() <= 1) return;

    // 找到命中 Binder 在列表中的位置，将其之前的（更高层级的）缓存全部填满
    for (CacheBinder<?, CacheBindingCfg> binder : binders) {
      if (binder == hitBinder) break;
      binder.set(key, value);
    }
  }

  @Override
  public String getBizTag() {
    return bizTag;
  }
}
