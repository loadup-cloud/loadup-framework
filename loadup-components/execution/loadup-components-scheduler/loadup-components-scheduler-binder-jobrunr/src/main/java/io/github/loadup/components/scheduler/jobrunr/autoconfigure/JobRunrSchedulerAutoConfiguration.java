/*
 * #%L
 * Loadup Scheduler Binder JobRunr
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
package io.github.loadup.components.scheduler.jobrunr.autoconfigure;

import io.github.loadup.components.scheduler.DefaultSchedulerProcessorRegistry;
import io.github.loadup.components.scheduler.SchedulerProcessor;
import io.github.loadup.components.scheduler.SchedulerProcessorRegistry;
import io.github.loadup.components.scheduler.SchedulerTemplate;
import io.github.loadup.components.scheduler.jobrunr.JobRunrSchedulerTemplate;
import io.github.loadup.components.scheduler.jobrunr.SchedulerJobRequestHandler;
import java.util.List;
import org.jobrunr.jobs.lambdas.JobRequest;
import org.jobrunr.scheduling.JobRequestScheduler;
import org.jobrunr.spring.autoconfigure.JobRunrAutoConfiguration;
import org.jobrunr.storage.StorageProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

@AutoConfiguration(after = JobRunrAutoConfiguration.class)
@ConditionalOnClass({JobRequestScheduler.class, JobRequest.class})
public class JobRunrSchedulerAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public SchedulerProcessorRegistry schedulerProcessorRegistry(List<SchedulerProcessor> processors) {
        return new DefaultSchedulerProcessorRegistry(processors);
    }

    @Bean
    @ConditionalOnMissingBean
    public SchedulerJobRequestHandler schedulerJobRequestHandler(SchedulerProcessorRegistry processorRegistry) {
        return new SchedulerJobRequestHandler(processorRegistry);
    }

    @Bean
    @ConditionalOnMissingBean
    public SchedulerTemplate schedulerTemplate(JobRequestScheduler scheduler, StorageProvider storageProvider) {
        return new JobRunrSchedulerTemplate(scheduler, storageProvider);
    }
}
