package io.github.loadup.components.cache.starter.manager;

/*-
 * #%L
 * Loadup Cache Starter
 * %%
 * Copyright (C) 2025 - 2026 LoadUp Cloud
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

import io.github.loadup.components.cache.binder.CacheBinder;
import io.github.loadup.components.cache.binding.CacheBinding;
import io.github.loadup.components.cache.starter.properties.CacheGroupProperties;
import io.github.loadup.framework.api.manager.BindingManagerSupport;
import org.springframework.context.ApplicationContext;

/** Cache 绑定管理器 继承通用内核，指定驱动类型为 CacheBinder，业务接口为 Binding */
public class CacheBindingManager extends BindingManagerSupport<CacheBinder, CacheBinding> {

  private final CacheGroupProperties groupProps;

  public CacheBindingManager(CacheGroupProperties props, ApplicationContext context) {
    // 传入 Spring 上下文和配置前缀：loadup.cache
    super(context, "loadup.cache");
    this.groupProps = props;
  }

  /** 实现内核要求的钩子：获取默认 Binder 类型 */
  @Override
  protected String getDefaultBinderType() {
    return groupProps.getDefaultBinder().getValue();
  }

  @Override
  public Class<CacheBinding> getBindingInterface() {
    return CacheBinding.class;
  }

  @Override
  public Class<CacheBinder> getBinderInterface() {
    return CacheBinder.class;
  }
}
