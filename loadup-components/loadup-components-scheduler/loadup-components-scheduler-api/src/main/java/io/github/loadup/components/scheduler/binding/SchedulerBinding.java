package io.github.loadup.components.scheduler.binding;

/*-
 * #%L
 * loadup-components-scheduler-api
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

import io.github.loadup.components.scheduler.binder.SchedulerBinder;
import io.github.loadup.components.scheduler.cfg.SchedulerBindingCfg;
import io.github.loadup.components.scheduler.model.SchedulerTask;
import io.github.loadup.framework.api.binding.Binding;

/**
 * Scheduler binding interface providing unified API for scheduler operations. This interface
 * delegates to the appropriate binder implementation.
 */
public interface SchedulerBinding extends Binding<SchedulerBinder<?, SchedulerBindingCfg>, SchedulerBindingCfg> {
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
     * @param cron the new cron expression
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
