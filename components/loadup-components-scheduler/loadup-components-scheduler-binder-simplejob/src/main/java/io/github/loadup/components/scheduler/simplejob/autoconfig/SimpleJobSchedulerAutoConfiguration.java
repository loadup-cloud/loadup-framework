package io.github.loadup.components.scheduler.simplejob.autoconfig;

/*-
 * #%L
 * Loadup Scheduler Simplejob Binder
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

import io.github.loadup.components.scheduler.binding.DefaultSchedulerBinding;
import io.github.loadup.components.scheduler.cfg.SchedulerBindingCfg;
import io.github.loadup.components.scheduler.simplejob.binder.SimpleJobSchedulerBinder;
import io.github.loadup.components.scheduler.simplejob.cfg.SimpleJobSchedulerBinderCfg;
import io.github.loadup.framework.api.manager.BindingMetadata;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
@ConditionalOnClass(SimpleJobSchedulerBinder.class)
public class SimpleJobSchedulerAutoConfiguration {

    @Bean
    public BindingMetadata<?, ?, ?, ?> simpleJobMetadata() {
        return new BindingMetadata<>(
                "simplejob",
                DefaultSchedulerBinding.class,
                SimpleJobSchedulerBinder.class,
                SchedulerBindingCfg.class,
                SimpleJobSchedulerBinderCfg.class,
                ctx -> new DefaultSchedulerBinding());
    }
}
