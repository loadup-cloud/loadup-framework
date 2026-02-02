package io.github.loadup.components.dfs.manager;

/*-
 * #%L
 * LoadUp Components DFS Starter
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

import io.github.loadup.components.dfs.binder.DfsBinder;
import io.github.loadup.components.dfs.binding.DfsBinding;
import io.github.loadup.components.dfs.properties.DfsGroupProperties;
import io.github.loadup.framework.api.manager.BindingManagerSupport;
import org.springframework.context.ApplicationContext;

/** DFS 绑定管理器 继承通用内核，指定驱动类型为 DfsBinder，业务接口为 Binding */
public class DfsBindingManager extends BindingManagerSupport<DfsBinder, DfsBinding> {

  private final DfsGroupProperties groupProps;

  public DfsBindingManager(DfsGroupProperties props, ApplicationContext context) {
    // 传入 Spring 上下文和配置前缀：loadup.dfs
    super(context, "loadup.dfs");
    this.groupProps = props;
  }

  /** 实现内核要求的钩子：获取默认 Binder 类型 */
  @Override
  protected String getDefaultBinderType() {
    return groupProps.getDefaultBinder().getValue();
  }

  /** 实现内核要求的钩子：指定驱动接口类型，用于容器查找原型 Bean */
  @Override
  public Class<DfsBinder> getBinderInterface() {
    return DfsBinder.class;
  }

  @Override
  public Class<DfsBinding> getBindingInterface() {
    return DfsBinding.class;
  }
}
