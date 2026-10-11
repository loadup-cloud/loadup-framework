/*
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
package io.github.loadup.components.scheduler.quartz.autoconfigure;

import io.github.loadup.components.scheduler.DefaultSchedulerProcessorRegistry;
import io.github.loadup.components.scheduler.SchedulerProcessor;
import io.github.loadup.components.scheduler.SchedulerProcessorRegistry;
import io.github.loadup.components.scheduler.SchedulerTemplate;
import io.github.loadup.components.scheduler.quartz.QuartzSchedulerTemplate;
import java.util.List;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.quartz.autoconfigure.QuartzAutoConfiguration;
import org.springframework.context.annotation.Bean;

@AutoConfiguration(after = QuartzAutoConfiguration.class)
@ConditionalOnClass({Scheduler.class, JobDetail.class})
public class QuartzSchedulerAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public SchedulerProcessorRegistry schedulerProcessorRegistry(List<SchedulerProcessor> processors) {
        return new DefaultSchedulerProcessorRegistry(processors);
    }

    @Bean
    @ConditionalOnMissingBean
    public SchedulerTemplate schedulerTemplate(Scheduler scheduler) {
        return new QuartzSchedulerTemplate(scheduler);
    }
}
