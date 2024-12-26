package com.github.loadup.components.scheduler.quartz.core;

import com.github.loadup.components.scheduler.model.TaskWrapper;
import com.github.loadup.components.scheduler.quartz.executer.SimpleJobExecutor;
import com.github.loadup.components.scheduler.quartz.invoker.JobInvoker;
import com.github.loadup.components.scheduler.quartz.invoker.SimpleJobInvoker;
import lombok.extern.slf4j.Slf4j;
import org.quartz.CronScheduleBuilder;
import org.quartz.CronTrigger;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.TriggerBuilder;

@Slf4j
public class SimpleScheduler extends AbstractScheduler {

    public SimpleScheduler(String jobName, TaskWrapper taskWrapper, String instanceId, String tenantId) {
        super(jobName, taskWrapper, instanceId, tenantId);
    }

    private CronTrigger createTrigger(final String cron) {
        return TriggerBuilder
                .newTrigger()
                .withIdentity("xx")
                .withSchedule(
                        CronScheduleBuilder.cronSchedule(cron).withMisfireHandlingInstructionDoNothing())
                .build();
    }

    @Override
    public JobDetail newJob() {
        return JobBuilder.newJob(SimpleJobExecutor.class).withIdentity(this.jobName).build();
    }

    @Override
    public JobInvoker newInvoker() {
        return new SimpleJobInvoker();
    }
}
