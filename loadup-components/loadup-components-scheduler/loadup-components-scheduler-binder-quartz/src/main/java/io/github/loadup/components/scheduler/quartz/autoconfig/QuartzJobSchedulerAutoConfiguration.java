package io.github.loadup.components.scheduler.quartz.autoconfig;

/*-
 * #%L
 * Loadup Scheduler Quartz Binder
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

import io.github.loadup.components.scheduler.SchedulerProvider;
import io.github.loadup.components.scheduler.quartz.QuartzSchedulerConfig;
import io.github.loadup.components.scheduler.quartz.QuartzSchedulerProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
@ConditionalOnProperty(prefix = "loadup.scheduler", name = "binder-type", havingValue = "quartz")
@EnableConfigurationProperties(QuartzSchedulerConfig.class)
public class QuartzJobSchedulerAutoConfiguration {
    @Bean
    @ConditionalOnMissingBean
    public SchedulerProvider quartzSchedulerProvider(QuartzSchedulerConfig config) {
        return new QuartzSchedulerProvider(config);
    }
}
