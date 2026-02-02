package io.github.loadup.framework.api.binder;

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

import io.github.loadup.framework.api.cfg.BaseBinderCfg;
import io.github.loadup.framework.api.cfg.BaseBindingCfg;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

public abstract class AbstractBinder<C extends BaseBinderCfg, S extends BaseBindingCfg> implements Binder<C, S> {
    protected C binderCfg;
    protected S bindingCfg;
    protected String name;
    // 关键：动态创建时，Spring 会自动扫描并注入此字段
    @Autowired
    protected ApplicationContext context;

    public C getBinderCfg() {
        return binderCfg;
    }

    public void setBinderCfg(C binderCfg) {
        this.binderCfg = binderCfg;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public final void injectConfigs(String name, C binderCfg, S bindingCfg) {
        this.name = name;
        this.binderCfg = binderCfg;
        this.bindingCfg = bindingCfg;

        // 执行初始化前的准备工作
        this.afterConfigInjected(name, binderCfg, bindingCfg);
    }

    /** 钩子方法：配置注入完成后，子类可以在这里初始化 SDK 客户端 */
    protected void afterConfigInjected(String name, C binderCfg, S bindingCfg) {}

    protected void afterDestroy() {}

    public void destroy() throws Exception {
        try {
            this.afterDestroy();
        } catch (Exception e) {
            throw e;
        }
    }
}
