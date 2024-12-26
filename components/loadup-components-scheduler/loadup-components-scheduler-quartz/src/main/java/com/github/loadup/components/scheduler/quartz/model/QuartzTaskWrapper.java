package com.github.loadup.components.scheduler.quartz.model;

import com.github.loadup.components.scheduler.model.TaskWrapper;
import com.github.loadup.components.scheduler.quartz.invoker.JobInvoker;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class QuartzTaskWrapper {
    private String jobName;

    private TaskWrapper taskWrapper;

    private JobInvoker jobInvoker;
}
