package io.github.loadup.framework.api.factory;

/*-
 * #%L
 * Loadup Common Api
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

import io.github.loadup.framework.api.binding.Binding;
import io.github.loadup.framework.api.context.BindingContext;

/**
 * Binding 工厂：负责将 Binder 和 Config 组装成业务 Binding
 *
 * @param <T> 具体 Binding 的类型
 */
@FunctionalInterface
public interface BindingFactory<T extends Binding> {

  /**
   * 创建 Binding 实例的核心方法
   *
   * @param context 包含配置和驱动的上下文
   * @return 组装好的 Binding 实例
   */
  T create(BindingContext<?, ?> context);
}
