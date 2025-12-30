package com.github.loadup.components.scheduler.powerjob.binder;

/*-
 * #%L
 * loadup-components-scheduler-binder-powerjob
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

