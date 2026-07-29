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

import com.github.loadup.components.scheduler.powerjob.PowerJobSchedulerConfig;
import com.github.loadup.components.scheduler.powerjob.PowerJobSchedulerProvider;
import io.github.loadup.components.scheduler.SchedulerProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
@ConditionalOnProperty(prefix = "loadup.scheduler", name = "binder-type", havingValue = "powerjob")
@EnableConfigurationProperties(PowerJobSchedulerConfig.class)
public class PowerJobSchedulerAutoConfiguration {
    @Bean
    @ConditionalOnMissingBean
    public SchedulerProvider powerJobSchedulerProvider(PowerJobSchedulerConfig config) {
        return new PowerJobSchedulerProvider(config);
    }
}
