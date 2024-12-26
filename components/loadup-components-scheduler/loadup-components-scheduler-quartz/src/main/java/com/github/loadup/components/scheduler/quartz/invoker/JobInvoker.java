package com.github.loadup.components.scheduler.quartz.invoker;

import com.github.loadup.components.scheduler.quartz.core.JobContext;

public interface JobInvoker {
    void invoke(JobContext jobContext);
}
