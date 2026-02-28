package com.github.loadup.components.scheduler.xxljob.autoconfig;

/*-
 * #%L
 * Loadup Scheduler XXLJob Binder
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

import com.github.loadup.components.scheduler.xxljob.cfg.XxlJobSchedulerBinderCfg;
import io.github.loadup.components.scheduler.binding.DefaultSchedulerBinding;
import io.github.loadup.components.scheduler.cfg.SchedulerBindingCfg;
import io.github.loadup.components.scheduler.xxljob.binder.XxlJobSchedulerBinder;
import io.github.loadup.framework.api.manager.BindingMetadata;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
@ConditionalOnClass(XxlJobSchedulerBinder.class)
public class XxlJobSchedulerAutoConfiguration {

    @Bean
    public BindingMetadata<?, ?, ?, ?> simpleJobMetadata() {
        return new BindingMetadata<>(
                "xxljob",
                DefaultSchedulerBinding.class,
                XxlJobSchedulerBinder.class,
                SchedulerBindingCfg.class,
                XxlJobSchedulerBinderCfg.class,
                ctx -> new DefaultSchedulerBinding());
    }
}
