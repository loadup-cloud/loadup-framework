package io.github.loadup.components.scheduler.quartz;

import io.github.loadup.components.scheduler.SchedulerProcessor;
import io.github.loadup.components.scheduler.SchedulerProcessorRegistry;
import io.github.loadup.components.scheduler.model.SchedulerContext;
import java.util.Map;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.scheduling.quartz.QuartzJobBean;

/**
 * Quartz job that dispatches a run to the {@link SchedulerProcessor} registered for the task name
 * stored in the job data map. The registry is resolved from the application context, which Spring's
 * {@code SpringBeanJobFactory} injects into any job implementing {@link ApplicationContextAware}.
 */
public class SchedulerTaskJob extends QuartzJobBean implements ApplicationContextAware {

    private ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Override
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
        try {
            String taskName = context.getMergedJobDataMap().getString("taskName");
            @SuppressWarnings("unchecked")
            Map<String, String> args =
                    (Map<String, String>) context.getMergedJobDataMap().get("args");
            SchedulerProcessorRegistry processorRegistry = applicationContext.getBean(SchedulerProcessorRegistry.class);
            SchedulerProcessor processor = processorRegistry.getProcessor(taskName);
            processor.process(new SchedulerContext(taskName, args == null ? Map.of() : args));
        } catch (Exception e) {
            throw new JobExecutionException(e);
        }
    }
}
