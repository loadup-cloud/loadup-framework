package com.github.loadup.components.scheduler.config;

/*-
 * #%L
 * loadup-components-scheduler-api
 * %%
 * Copyright (C) 2026 LoadUp Cloud
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
import com.github.loadup.components.scheduler.api.SchedulerBinding;
import com.github.loadup.components.scheduler.binding.DefaultSchedulerBinding;
import com.github.loadup.components.scheduler.core.SchedulerTaskRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnSingleCandidate;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Auto-configuration for scheduler component.
 */
@Slf4j
@AutoConfiguration
@EnableAsync
@EnableScheduling
public class SchedulerAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public SchedulerTaskRegistry schedulerTaskRegistry() {
        log.info("Creating SchedulerTaskRegistry");
        return new SchedulerTaskRegistry();
    }

    @Bean
    @ConditionalOnSingleCandidate(SchedulerBinder.class)
    @ConditionalOnMissingBean
    public SchedulerBinding schedulerBinding(SchedulerBinder schedulerBinder) {
    log.info("Creating SchedulerBinding with binder: {}", schedulerBinder.getBinderType());
        return new DefaultSchedulerBinding(schedulerBinder);
    }
}
