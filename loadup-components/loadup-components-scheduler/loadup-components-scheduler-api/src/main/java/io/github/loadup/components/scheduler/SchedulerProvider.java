package io.github.loadup.components.scheduler;

import io.github.loadup.components.scheduler.model.SchedulerTask;

public interface SchedulerProvider {
    boolean schedule(SchedulerTask task);
    boolean cancel(String taskName);
    boolean pauseTask(String taskName);
    boolean resumeTask(String taskName);
    boolean triggerTask(String taskName);
    boolean updateTaskCron(String taskName, String cron);
    boolean taskExists(String taskName);
    String getBinderType();
}
