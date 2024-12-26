package com.github.loadup.components.scheduler.quartz.executer;

import com.github.loadup.components.scheduler.model.TaskWrapper;
import com.github.loadup.components.scheduler.quartz.core.JobContext;
import com.github.loadup.components.scheduler.quartz.invoker.JobInvoker;
import lombok.extern.slf4j.Slf4j;
import org.quartz.JobExecutionContext;

@Slf4j
public class SimpleJobExecutor extends AbstractJobExecutor {

    @Override
    public void executeJob(JobExecutionContext context) {
        TaskWrapper taskWrapper = quartzTaskWrapper.getTaskWrapper();
        String jobName = quartzTaskWrapper.getJobName();
        log.info("Simple job execute start. JobName: {}", jobName);
        try {
            JobInvoker jobInvoker = quartzTaskWrapper.getJobInvoker();
            jobInvoker.invoke(new JobContext(jobName, taskWrapper, null, context));
        } catch (Exception e) {
            log.error("Simple job execute error.", e);
        }
        log.info("Simple job execute end. JobName: {}", jobName);
    }
}
