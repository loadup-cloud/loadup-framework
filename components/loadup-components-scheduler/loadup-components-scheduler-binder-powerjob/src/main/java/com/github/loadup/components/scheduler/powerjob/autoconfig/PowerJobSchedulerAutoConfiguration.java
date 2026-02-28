package com.github.loadup.components.scheduler.powerjob.autoconfig;

/*-
 * #%L
 * Loadup Scheduler Powerjob Binder
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

import com.github.loadup.components.scheduler.powerjob.cfg.PowerJobSchedulerBinderCfg;
import io.github.loadup.components.scheduler.binding.DefaultSchedulerBinding;
import io.github.loadup.components.scheduler.binding.SchedulerBinding;
import io.github.loadup.components.scheduler.powerjob.binder.PowerJobSchedulerBinder;
import io.github.loadup.framework.api.manager.BindingMetadata;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import tech.powerjob.worker.core.processor.sdk.BasicProcessor;

@AutoConfiguration
@ConditionalOnClass(BasicProcessor.class)
public class PowerJobSchedulerAutoConfiguration {

    /**
     * S3 模块的自动注册逻辑 只有当 classpath 下存在 S3DfsBinding 类时（即引入了 binder-s3 依赖），这段逻辑才生效
     */
    @Bean
    public BindingMetadata<?, ?, ?, ?> s3Metadata() {
        return new BindingMetadata<>(
                "powerjob",
                DefaultSchedulerBinding.class,
                PowerJobSchedulerBinder.class,
                SchedulerBinding.class,
                PowerJobSchedulerBinderCfg.class,
                ctx -> new DefaultSchedulerBinding());
    }
}
