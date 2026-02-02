package io.github.loadup.framework.api.binder;

import io.github.loadup.framework.api.cfg.BaseBinderCfg;
import io.github.loadup.framework.api.cfg.BaseBindingCfg;
import org.springframework.beans.factory.DisposableBean;

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

/**
 * @param <C> Binder 自身的配置基类 (如 RedisBinderCfg)
 * @param <C> 关联的业务 Binding 配置基类 (如 RedisBindingCfg)
 */
public interface Binder<C extends BaseBinderCfg, S extends BaseBindingCfg> extends DisposableBean {
    String getBinderType();

    default void binderInit() {}

    default void binderDestroy() {}

    /**
     * 初始化入口：注入双重配置
     *
     * @param name 实例名称 (bizType)
     * @param binderCfg 中间件层级的通用配置
     * @param bindingCfg 业务层级的定制配置
     */
    void injectConfigs(String name, C binderCfg, S bindingCfg);
}
