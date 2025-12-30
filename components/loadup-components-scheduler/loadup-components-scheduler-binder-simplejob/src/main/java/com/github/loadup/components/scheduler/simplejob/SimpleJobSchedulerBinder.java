/* Copyright (C) LoadUp Cloud 2022-2025 */
package com.github.loadup.components.scheduler.simplejob;

/*-
 * #%L
 * loadup-components-scheduler-binder-simplejob
 * %%
 * Copyright (C) 2022 - 2023 loadup_cloud
 * %%
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
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

