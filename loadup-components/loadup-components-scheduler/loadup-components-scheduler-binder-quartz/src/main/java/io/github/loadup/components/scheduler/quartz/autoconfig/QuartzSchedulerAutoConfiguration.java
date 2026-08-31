package io.github.loadup.components.scheduler.quartz.autoconfig;

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

/**
 * Wires the Quartz binder on top of Spring Boot's {@code spring-boot-starter-quartz}
 * auto-configuration: facade implementation and processor registry. Quartz provides the embedded
 * or JDBC-backed clustered scheduler; business code only sees the scheduler facade.
 */
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
