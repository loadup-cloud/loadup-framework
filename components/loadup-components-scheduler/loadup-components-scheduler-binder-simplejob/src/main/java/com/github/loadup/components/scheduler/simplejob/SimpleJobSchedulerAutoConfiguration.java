package com.github.loadup.components.scheduler.simplejob;

/*-
 * #%L
 * loadup-components-scheduler-binder-simplejob
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
import com.github.loadup.components.scheduler.config.SchedulerAutoConfiguration;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.TaskScheduler;

/**
 * Auto-configuration for SimpleJob scheduler binder.
 */
@Slf4j
@AutoConfiguration
@AutoConfigureBefore(SchedulerAutoConfiguration.class)
@ConditionalOnProperty(
    prefix = "loadup.scheduler",
    name = "type",
    havingValue = "simplejob",
    matchIfMissing = true
)
public class SimpleJobSchedulerAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(SchedulerBinder.class)
    public SchedulerBinder simpleJobSchedulerBinder(TaskScheduler taskScheduler) {
        log.info("Creating SimpleJob scheduler binder");
        return new SimpleJobSchedulerBinder(taskScheduler);
    }
}

