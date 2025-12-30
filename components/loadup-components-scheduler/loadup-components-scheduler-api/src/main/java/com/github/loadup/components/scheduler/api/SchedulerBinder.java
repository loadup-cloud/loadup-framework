/* Copyright (C) LoadUp Cloud 2022-2025 */
package com.github.loadup.components.scheduler.api;

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

import com.github.loadup.components.scheduler.model.SchedulerTask;
import com.github.loadup.framework.api.binder.BaseBinder;

/**
 * Base scheduler binder interface providing common scheduler operations.
 * Implementations should provide concrete logic for different scheduler frameworks.
 */
public interface SchedulerBinder extends BaseBinder {

    /**
     * Register a scheduled task.
     *
     * @param task the scheduler task to register
     * @return true if registration successful, false otherwise
     */
    default boolean registerTask(SchedulerTask task) {
        return false;
    }

    /**
     * Unregister a scheduled task by task name.
     *
     * @param taskName the name of the task to unregister
     * @return true if unregistration successful, false otherwise
     */
    default boolean unregisterTask(String taskName) {
        return false;
    }

    /**
     * Pause a scheduled task by task name.
     *
     * @param taskName the name of the task to pause
     * @return true if pause successful, false otherwise
     */
    default boolean pauseTask(String taskName) {
        return false;
    }

    /**
     * Resume a paused scheduled task by task name.
     *
     * @param taskName the name of the task to resume
     * @return true if resume successful, false otherwise
     */
    default boolean resumeTask(String taskName) {
        return false;
    }

    /**
     * Trigger a scheduled task to run immediately.
     *
     * @param taskName the name of the task to trigger
     * @return true if trigger successful, false otherwise
     */
    default boolean triggerTask(String taskName) {
        return false;
    }

    /**
     * Update the cron expression of an existing scheduled task.
     *
     * @param taskName the name of the task
     * @param cron     the new cron expression
     * @return true if update successful, false otherwise
     */
    default boolean updateTaskCron(String taskName, String cron) {
        return false;
    }

    /**
     * Check if a task exists in the scheduler.
     *
     * @param taskName the name of the task
     * @return true if task exists, false otherwise
     */
    default boolean taskExists(String taskName) {
        return false;
    }
}

