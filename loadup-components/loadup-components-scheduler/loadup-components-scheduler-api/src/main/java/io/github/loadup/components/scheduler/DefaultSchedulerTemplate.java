package io.github.loadup.components.scheduler;

import io.github.loadup.components.scheduler.model.SchedulerTask;

public class DefaultSchedulerTemplate implements SchedulerTemplate {
    private final SchedulerProvider provider;

    public DefaultSchedulerTemplate(SchedulerProvider provider) { this.provider = provider; }

    @Override public boolean registerTask(SchedulerTask task) { return provider.schedule(task); }
    @Override public boolean cancel(String taskName) { return provider.cancel(taskName); }
    @Override public boolean pauseTask(String taskName) { return provider.pauseTask(taskName); }
    @Override public boolean resumeTask(String taskName) { return provider.resumeTask(taskName); }
    @Override public boolean triggerTask(String taskName) { return provider.triggerTask(taskName); }
    @Override public boolean updateTaskCron(String taskName, String cron) { return provider.updateTaskCron(taskName, cron); }
    @Override public boolean taskExists(String taskName) { return provider.taskExists(taskName); }
}
