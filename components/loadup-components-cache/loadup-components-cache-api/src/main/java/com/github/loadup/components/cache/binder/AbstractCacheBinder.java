package com.github.loadup.components.cache.binder;

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

import com.github.loadup.components.cache.cfg.CacheBinderCfg;
import com.github.loadup.components.cache.serializer.CacheSerializer;
import com.github.loadup.framework.api.binder.AbstractBinder;

public abstract class AbstractCacheBinder<C extends CacheBinderCfg> extends AbstractBinder<C>
    implements CacheBinder {
  protected CacheSerializer serializer;
  protected CacheTicker ticker; // 时间源

  @Override
  protected void afterConfigInjected() {
    // 自动从容器中根据配置的名称获取序列化器
    String beanName = binderCfg.getSerializerBeanName();
    // 如果容器里没有 customKryoSerializer，则降级使用 defaultCacheSerializer
    if (!context.containsBean(beanName)) {
      beanName = "jsonCacheSerializer";
    }
    // 这里的 context 是在 AbstractBinder 实例化时由 Manager 注入的 ApplicationContext
    this.serializer = context.getBean(beanName, CacheSerializer.class);
    // 2. 注入 Ticker
    String tickerName = binderCfg.getTickerBeanName();
    if (context.containsBean(tickerName)) {
      this.ticker = context.getBean(tickerName, CacheTicker.class);
    } else {
      this.ticker = CacheTicker.SYSTEM;
    }
    // 执行子类特有的初始化逻辑
    onInit();
  }

  protected abstract void onInit();

  public CacheSerializer getSerializer() {
    return serializer;
  }

  /** 提供一个工具方法给子类（CaffeineBinder/RedisBinder）使用 将对象包装成驱动能识别的格式 */
  protected Object wrapValue(Object value) {
    if (value == null) return null;

    // 如果当前驱动配置了序列化器，则转为字节数组
    if (this.serializer != null) {
      return this.serializer.serialize(value);
    }

    // 如果没配置（如纯内存模式），直接返回原始对象
    return value;
  }
}
