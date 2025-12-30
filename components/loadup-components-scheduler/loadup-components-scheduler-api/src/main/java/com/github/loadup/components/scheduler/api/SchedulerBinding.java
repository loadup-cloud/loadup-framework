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
import com.github.loadup.framework.api.binding.BaseBinding;

/**
 * Scheduler binding interface providing unified API for scheduler operations.
 * This interface delegates to the appropriate binder implementation.
 */
public interface SchedulerBinding extends BaseBinding {

    /**
     * Register a scheduled task.
     *
     * @param task the scheduler task to register
     * @return true if registration successful, false otherwise
     */
    boolean registerTask(SchedulerTask task);

    /**
     * Unregister a scheduled task by task name.
     *
     * @param taskName the name of the task to unregister
     * @return true if unregistration successful, false otherwise
     */
    boolean unregisterTask(String taskName);

    /**
     * Pause a scheduled task by task name.
     *
     * @param taskName the name of the task to pause
     * @return true if pause successful, false otherwise
     */
    boolean pauseTask(String taskName);

    /**
     * Resume a paused scheduled task by task name.
     *
     * @param taskName the name of the task to resume
     * @return true if resume successful, false otherwise
     */
    boolean resumeTask(String taskName);

    /**
     * Trigger a scheduled task to run immediately.
     *
     * @param taskName the name of the task to trigger
     * @return true if trigger successful, false otherwise
     */
    boolean triggerTask(String taskName);

    /**
     * Update the cron expression of an existing scheduled task.
     *
     * @param taskName the name of the task
     * @param cron     the new cron expression
     * @return true if update successful, false otherwise
     */
    boolean updateTaskCron(String taskName, String cron);

    /**
     * Check if a task exists in the scheduler.
     *
     * @param taskName the name of the task
     * @return true if task exists, false otherwise
     */
    boolean taskExists(String taskName);
}

