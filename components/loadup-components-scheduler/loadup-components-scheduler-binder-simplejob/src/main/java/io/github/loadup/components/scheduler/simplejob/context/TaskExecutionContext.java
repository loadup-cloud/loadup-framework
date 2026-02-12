package io.github.loadup.components.scheduler.simplejob.context;

import io.github.loadup.components.scheduler.model.SchedulerTask;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.concurrent.ScheduledFuture;

@Data
@AllArgsConstructor
public class TaskExecutionContext {
    private SchedulerTask task;
    private ScheduledFuture<?> future;
    private boolean paused;
}
