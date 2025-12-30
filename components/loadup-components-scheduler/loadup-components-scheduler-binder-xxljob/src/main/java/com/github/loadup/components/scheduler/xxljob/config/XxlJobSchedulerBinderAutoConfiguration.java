package com.github.loadup.components.scheduler.xxljob.config;

/*-
 * #%L
 * loadup-components-scheduler-binder-xxljob
 * %%
 * Copyright (C) 2022 - 2023 loadup_cloud
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

import com.github.loadup.components.scheduler.api.SchedulerBinder;
import com.github.loadup.components.scheduler.xxljob.binder.XxlJobSchedulerBinder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.*;
import org.springframework.context.annotation.Bean;

/**
 * Auto-configuration for XXL-Job scheduler binder.
 */
@Slf4j
@AutoConfiguration
@ConditionalOnClass(name = "com.xxl.job.core.executor.XxlJobExecutor")
@ConditionalOnProperty(
    prefix = "loadup.scheduler",
    name = "type",
    havingValue = "xxljob"
)
public class XxlJobSchedulerBinderAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(SchedulerBinder.class)
    public SchedulerBinder xxlJobSchedulerBinder() {
        log.info("Creating XXL-Job scheduler binder");
        return new XxlJobSchedulerBinder();
    }
}

