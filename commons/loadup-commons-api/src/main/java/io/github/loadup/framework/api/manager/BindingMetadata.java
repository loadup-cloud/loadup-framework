package io.github.loadup.framework.api.manager;

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
import io.github.loadup.framework.api.factory.BindingFactory;

/** 内部类或独立类，用于保存 Binder 类型与具体实现类的映射关系 */
// 扩展后的元数据，持有双配置类型
public class BindingMetadata<B, C_BIND, C_BINDR, T extends Binding> {
    public final String type; // 新增：caffeine / redis
    public final Class<T> bindingClass;
    public final Class<? extends B> binderClass;
    public final Class<C_BIND> bindingCfgClass;
    public final Class<C_BINDR> binderCfgClass;
    public final BindingFactory<T> factory;

    // 构造函数强制要求类型对齐
    public BindingMetadata(
            String type,
            Class<T> bindingClass,
            Class<? extends B> binderClass,
            Class<C_BIND> bindingCfgClass,
            Class<C_BINDR> binderCfgClass,
            BindingFactory<T> factory) {
        this.type = type;
        this.bindingCfgClass = bindingCfgClass;
        this.binderCfgClass = binderCfgClass;
        this.binderClass = binderClass;
        this.bindingClass = bindingClass;
        this.factory = factory;
    }
}
