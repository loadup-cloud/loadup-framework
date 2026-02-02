package io.github.loadup.components.scheduler.binder;

/*-
 * #%L
 * Loadup Scheduler Api
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

import io.github.loadup.components.scheduler.cfg.SchedulerBinderCfg;
import io.github.loadup.components.scheduler.cfg.SchedulerBindingCfg;
import io.github.loadup.framework.api.binder.AbstractBinder;

public abstract class AbstractSchedulerBinder<
        C extends SchedulerBinderCfg, S extends SchedulerBindingCfg>
    extends AbstractBinder<C, S> implements SchedulerBinder<C, S> {
  @Override
  protected void afterConfigInjected(String name, C binderCfg, S bindingCfg) {
    // 自动从容器中根据配置的名称获取序列化器
    // 执行子类特有的初始化逻辑
    onInit();
  }

  protected abstract void onInit();
}
