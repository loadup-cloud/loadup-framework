package com.github.loadup.components.scheduler.quartz.core;

import com.github.loadup.components.scheduler.model.TaskWrapper;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.quartz.JobExecutionContext;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class JobContext {
    private String jobName;

    private TaskWrapper taskWrapper;

    private String clusterExecuter;

    private JobExecutionContext context;
}
