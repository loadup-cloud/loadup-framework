package io.github.loadup.components.scheduler.quartz.autoconfig;

/*-
 * #%L
 * Loadup Scheduler Quartz Binder
 * %%
 * Copyright (C) 2025 - 2026 LoadUp Cloud
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
