package com.github.loadup.framework.api.binding;

/*-
 * #%L
 * loadup-commons-api
 * %%
 * Copyright (C) 2025 LoadUp Cloud
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

import com.github.loadup.framework.api.binder.Binder;
import com.github.loadup.framework.api.cfg.BaseBindingCfg;
import com.github.loadup.framework.api.context.BindingContext;

public interface Binding<B extends Binder, C extends BaseBindingCfg> {
  String getBizTag();

  /**
   * 通用的初始化方法
   *
   * @param context 包含所有装配原材料的上下文
   */
  void init(BindingContext<B, C> context);
}
