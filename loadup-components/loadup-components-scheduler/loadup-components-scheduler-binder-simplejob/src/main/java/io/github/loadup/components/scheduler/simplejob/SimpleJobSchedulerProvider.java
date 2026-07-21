package io.github.loadup.components.scheduler.simplejob;

import io.github.loadup.components.scheduler.SchedulerProvider;
import io.github.loadup.components.scheduler.model.SchedulerTask;
import java.util.concurrent.ConcurrentHashMap;

public class SimpleJobSchedulerProvider implements SchedulerProvider {
    private final ConcurrentHashMap<String, SchedulerTask> jobs = new ConcurrentHashMap<>();

    public SimpleJobSchedulerProvider(SimpleJobSchedulerConfig config) {
        // SimpleJob 仅内存注册，不实际执行
    }

    @Override
    public boolean schedule(SchedulerTask task) {
        jobs.put(task.getTaskName(), task);
        return true;
    }

    @Override
    public boolean cancel(String taskName) {
        return jobs.remove(taskName) != null;
    }

    @Override public boolean pauseTask(String taskName) { return jobs.containsKey(taskName); }
    @Override public boolean resumeTask(String taskName) { return jobs.containsKey(taskName); }
    @Override public boolean triggerTask(String taskName) { return jobs.containsKey(taskName); }
    @Override public boolean updateTaskCron(String taskName, String cron) { return jobs.containsKey(taskName); }
    @Override public boolean taskExists(String taskName) { return jobs.containsKey(taskName); }
    @Override public String getBinderType() { return "simplejob"; }
}
