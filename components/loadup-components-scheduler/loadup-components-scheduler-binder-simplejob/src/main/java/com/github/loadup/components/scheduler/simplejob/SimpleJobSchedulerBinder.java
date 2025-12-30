package com.github.loadup.components.scheduler.simplejob;

/*-
 * #%L
 * loadup-components-scheduler-binder-simplejob
 * %%
 * Copyright (C) 2022 - 2023 loadup_cloud
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

import com.github.loadup.components.scheduler.api.SchedulerBinder;
import com.github.loadup.components.scheduler.model.SchedulerTask;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.support.CronTrigger;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;

/**
 * Simple job scheduler binder implementation using Spring's TaskScheduler.
 * This is a lightweight implementation suitable for single-instance applications.
 */
@Slf4j
public class SimpleJobSchedulerBinder implements SchedulerBinder {

    private static final String BINDER_NAME = "simplejob";

    private final TaskScheduler                   taskScheduler;
    private final Map<String, ScheduledFuture<?>> scheduledTasks = new ConcurrentHashMap<>();

    public SimpleJobSchedulerBinder() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(10);
        scheduler.setThreadNamePrefix("simplejob-");
        scheduler.setWaitForTasksToCompleteOnShutdown(true);
        scheduler.setAwaitTerminationSeconds(60);
        scheduler.initialize();
        this.taskScheduler = scheduler;
    }

    public SimpleJobSchedulerBinder(TaskScheduler taskScheduler) {
        this.taskScheduler = taskScheduler;
    }

    @Override
    public String getName() {
        return BINDER_NAME;
    }

    @Override
    public void init() {
        log.info("Initializing SimpleJob scheduler binder");
    }

    @Override
    public void destroy() {
        log.info("Destroying SimpleJob scheduler binder");
        scheduledTasks.values().forEach(future -> future.cancel(false));
        scheduledTasks.clear();
    }

    @Override
    public boolean registerTask(SchedulerTask task) {
        try {
            String taskName = task.getTaskName();
            if (scheduledTasks.containsKey(taskName)) {
                log.warn("Task '{}' already registered, unregistering first", taskName);
                unregisterTask(taskName);
            }

            ScheduledFuture<?> future = taskScheduler.schedule(
                () -> executeTask(task),
                new CronTrigger(task.getCron())
            );

            scheduledTasks.put(taskName, future);
            log.info("Successfully registered task: {}", taskName);
            return true;
        } catch (Exception e) {
            log.error("Failed to register task: {}", task.getTaskName(), e);
            return false;
        }
    }

    @Override
    public boolean unregisterTask(String taskName) {
        ScheduledFuture<?> future = scheduledTasks.remove(taskName);
        if (future != null) {
            future.cancel(false);
            log.info("Successfully unregistered task: {}", taskName);
            return true;
        }
        log.warn("Task '{}' not found for unregistration", taskName);
        return false;
    }

    @Override
    public boolean pauseTask(String taskName) {
        // SimpleJob doesn't support pause, use unregister instead
        log.warn("SimpleJob doesn't support pause operation, use unregister instead");
        return unregisterTask(taskName);
    }

    @Override
    public boolean resumeTask(String taskName) {
        // SimpleJob doesn't support resume, would need to re-register
        log.warn("SimpleJob doesn't support resume operation, use registerTask instead");
        return false;
    }

    @Override
    public boolean triggerTask(String taskName) {
        log.warn("SimpleJob doesn't support manual trigger, task must wait for next scheduled time");
        return false;
    }

    @Override
    public boolean updateTaskCron(String taskName, String cron) {
        log.warn("SimpleJob doesn't support cron update, use unregister and re-register instead");
        return false;
    }

    @Override
    public boolean taskExists(String taskName) {
        return scheduledTasks.containsKey(taskName);
    }

    private void executeTask(SchedulerTask task) {
        String taskName = task.getTaskName();
        try {
            log.debug("Executing task: {}", taskName);
            Method method = task.getMethod();
            method.setAccessible(true);  // Allow access to methods in nested classes
            method.invoke(task.getTargetBean());
            log.debug("Task executed successfully: {}", taskName);
        } catch (IllegalAccessException | InvocationTargetException e) {
            log.error("Error executing task: {}", taskName, e);
        }
    }
}

