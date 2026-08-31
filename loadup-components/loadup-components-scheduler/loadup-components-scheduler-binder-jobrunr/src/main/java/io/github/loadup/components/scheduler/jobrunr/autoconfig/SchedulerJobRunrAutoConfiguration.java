package io.github.loadup.components.scheduler.jobrunr.autoconfig;

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

/**
 * Wires the JobRunr binder on top of the official {@code jobrunr-spring-boot-4-starter}
 * auto-configuration: facade implementation, processor registry and the JobRequest handler that
 * dispatches recurring runs to business processors. Shares the same JobRunr engine (storage,
 * background job server, dashboard) as the retry task binder.
 */
@AutoConfiguration(after = JobRunrAutoConfiguration.class)
@ConditionalOnClass({JobRequestScheduler.class, JobRequest.class})
public class SchedulerJobRunrAutoConfiguration {

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
