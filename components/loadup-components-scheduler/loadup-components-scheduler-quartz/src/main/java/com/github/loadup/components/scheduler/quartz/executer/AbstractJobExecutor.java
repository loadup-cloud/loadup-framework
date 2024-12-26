package com.github.loadup.components.scheduler.quartz.executer;

import com.github.loadup.components.scheduler.quartz.model.QuartzTaskWrapper;
import lombok.Getter;
import lombok.Setter;
import org.quartz.Job;
import org.quartz.JobExecutionContext;

@Getter
@Setter
public abstract class AbstractJobExecutor implements Job {
    protected QuartzTaskWrapper quartzTaskWrapper;

    @Override
    public void execute(final JobExecutionContext context) {
        executeJob(context);
    }

    /**
     * execute job
     */
    public abstract void executeJob(final JobExecutionContext context);

}
