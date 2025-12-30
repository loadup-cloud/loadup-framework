/* Copyright (C) LoadUp Cloud 2022-2025 */
package com.github.loadup.components.scheduler.powerjob.binder;

/*-
 * #%L
 * loadup-components-scheduler-binder-powerjob
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

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * PowerJob scheduler binder implementation.
 * Note: PowerJob is a distributed task scheduling framework that typically
 * uses server-side configuration. This binder provides local task tracking.
 */
@Slf4j
public class PowerJobSchedulerBinder implements SchedulerBinder {

    private static final String                     BINDER_NAME  = "powerjob";
    private final        Map<String, SchedulerTask> taskRegistry = new ConcurrentHashMap<>();

    @Override
    public String getName() {
        return BINDER_NAME;
    }

    @Override
    public void init() {
        log.info("Initializing PowerJob scheduler binder");
        log.info("PowerJob tasks are typically managed via PowerJob server console");
    }

    @Override
    public void destroy() {
        log.info("Destroying PowerJob scheduler binder");
        taskRegistry.clear();
    }

    @Override
    public boolean registerTask(SchedulerTask task) {
        // PowerJob uses server-side configuration and @PowerJobHandler annotation
        // We store the task for reference but cannot dynamically register
        String taskName = task.getTaskName();
        taskRegistry.put(taskName, task);
        log.info("Task '{}' registered in local registry. Configure on PowerJob server for execution", taskName);
        return true;
    }

    @Override
    public boolean unregisterTask(String taskName) {
        SchedulerTask removed = taskRegistry.remove(taskName);
        if (removed != null) {
            log.info("Task '{}' removed from local registry", taskName);
            log.warn("Task management should be done via PowerJob server console");
            return true;
        }
        return false;
    }

    @Override
    public boolean pauseTask(String taskName) {
        log.warn("PowerJob task pause must be done via server console");
        return false;
    }

    @Override
    public boolean resumeTask(String taskName) {
        log.warn("PowerJob task resume must be done via server console");
        return false;
    }

    @Override
    public boolean triggerTask(String taskName) {
        log.warn("PowerJob task trigger must be done via server console or API");
        return false;
    }

    @Override
    public boolean updateTaskCron(String taskName, String cron) {
        log.warn("PowerJob task cron update must be done via server console or API");
        return false;
    }

    @Override
    public boolean taskExists(String taskName) {
        return taskRegistry.containsKey(taskName);
    }
}

