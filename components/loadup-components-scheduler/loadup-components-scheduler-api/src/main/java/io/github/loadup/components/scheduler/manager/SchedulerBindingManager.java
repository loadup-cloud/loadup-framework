package io.github.loadup.components.scheduler.manager;

/*-
 * #%L
 * Loadup Scheduler Starter
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

import io.github.loadup.components.scheduler.binder.SchedulerBinder;
import io.github.loadup.components.scheduler.binding.SchedulerBinding;
import io.github.loadup.components.scheduler.properties.SchedulerGroupProperties;
import io.github.loadup.framework.api.manager.BindingManagerSupport;
import org.springframework.context.ApplicationContext;

public class SchedulerBindingManager extends BindingManagerSupport<SchedulerBinder, SchedulerBinding> {
    private final SchedulerGroupProperties groupProps;

    public SchedulerBindingManager(SchedulerGroupProperties props, ApplicationContext context) {
        // 传入 Spring 上下文和配置前缀：loadup.scheduler
        super(context, "loadup.scheduler");
        this.groupProps = props;
    }

    @Override
    protected String getDefaultBinderType() {
        return groupProps.getDefaultBinder().getValue();
    }

    @Override
    public Class<SchedulerBinding> getBindingInterface() {
        return SchedulerBinding.class;
    }

    @Override
    public Class<SchedulerBinder> getBinderInterface() {
        return SchedulerBinder.class;
    }
}
