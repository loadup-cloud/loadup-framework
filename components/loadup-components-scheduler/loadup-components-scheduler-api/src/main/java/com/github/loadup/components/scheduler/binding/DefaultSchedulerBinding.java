/* Copyright (C) LoadUp Cloud 2022-2025 */
package com.github.loadup.components.scheduler.binding;

/*-
 * #%L
 * loadup-components-scheduler-api
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
import com.github.loadup.components.scheduler.api.SchedulerBinding;
import com.github.loadup.components.scheduler.model.SchedulerTask;
import lombok.extern.slf4j.Slf4j;

/**
 * Default implementation of SchedulerBinding that delegates to a SchedulerBinder.
 */
@Slf4j
public class DefaultSchedulerBinding implements SchedulerBinding {

    private final SchedulerBinder schedulerBinder;

    public DefaultSchedulerBinding(SchedulerBinder schedulerBinder) {
        this.schedulerBinder = schedulerBinder;
        log.info("Initialized SchedulerBinding with binder: {}", schedulerBinder.getName());
    }

    @Override
    public boolean registerTask(SchedulerTask task) {
        log.debug("Registering task: {}", task.getTaskName());
        return schedulerBinder.registerTask(task);
    }

    @Override
    public boolean unregisterTask(String taskName) {
        log.debug("Unregistering task: {}", taskName);
        return schedulerBinder.unregisterTask(taskName);
    }

    @Override
    public boolean pauseTask(String taskName) {
        log.debug("Pausing task: {}", taskName);
        return schedulerBinder.pauseTask(taskName);
    }

    @Override
    public boolean resumeTask(String taskName) {
        log.debug("Resuming task: {}", taskName);
        return schedulerBinder.resumeTask(taskName);
    }

    @Override
    public boolean triggerTask(String taskName) {
        log.debug("Triggering task: {}", taskName);
        return schedulerBinder.triggerTask(taskName);
    }

    @Override
    public boolean updateTaskCron(String taskName, String cron) {
        log.debug("Updating task {} cron to: {}", taskName, cron);
        return schedulerBinder.updateTaskCron(taskName, cron);
    }

    @Override
    public boolean taskExists(String taskName) {
        return schedulerBinder.taskExists(taskName);
    }

    @Override
    public void init() {
        log.info("Initializing scheduler binding");
        schedulerBinder.init();
    }

    @Override
    public void destroy() {
        log.info("Destroying scheduler binding");
        schedulerBinder.destroy();
    }
}

