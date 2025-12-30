/* Copyright (C) LoadUp Cloud 2022-2025 */
package com.github.loadup.components.scheduler.xxljob.binder;

/*-
 * #%L
 * loadup-components-scheduler-binder-xxljob
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
 * XXL-Job scheduler binder implementation.
 * Note: XXL-Job uses annotation-based task registration (@XxlJob),
 * so this binder provides limited runtime management capabilities.
 */
@Slf4j
public class XxlJobSchedulerBinder implements SchedulerBinder {

    private static final String                     BINDER_NAME  = "xxljob";
    private final        Map<String, SchedulerTask> taskRegistry = new ConcurrentHashMap<>();

    @Override
    public String getName() {
        return BINDER_NAME;
    }

    @Override
    public void init() {
        log.info("Initializing XXL-Job scheduler binder");
        log.warn("XXL-Job uses annotation-based registration. Runtime task management is limited.");
    }

    @Override
    public void destroy() {
        log.info("Destroying XXL-Job scheduler binder");
        taskRegistry.clear();
    }

    @Override
    public boolean registerTask(SchedulerTask task) {
        // XXL-Job uses @XxlJob annotation for registration
        // We store the task for reference but cannot dynamically register
        String taskName = task.getTaskName();
        taskRegistry.put(taskName, task);
        log.info("Task '{}' registered in local registry. Note: XXL-Job requires @XxlJob annotation", taskName);
        log.warn("XXL-Job tasks must be registered via admin console and use @XxlJob annotation");
        return true;
    }

    @Override
    public boolean unregisterTask(String taskName) {
        SchedulerTask removed = taskRegistry.remove(taskName);
        if (removed != null) {
            log.info("Task '{}' removed from local registry", taskName);
            log.warn("XXL-Job tasks must be managed via admin console");
            return true;
        }
        return false;
    }

    @Override
    public boolean pauseTask(String taskName) {
        log.warn("XXL-Job task pause must be done via admin console");
        return false;
    }

    @Override
    public boolean resumeTask(String taskName) {
        log.warn("XXL-Job task resume must be done via admin console");
        return false;
    }

    @Override
    public boolean triggerTask(String taskName) {
        log.warn("XXL-Job task trigger must be done via admin console");
        return false;
    }

    @Override
    public boolean updateTaskCron(String taskName, String cron) {
        log.warn("XXL-Job task cron update must be done via admin console");
        return false;
    }

    @Override
    public boolean taskExists(String taskName) {
        return taskRegistry.containsKey(taskName);
    }
}

