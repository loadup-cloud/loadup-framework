package io.github.loadup.components.scheduler.binder;

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

import io.github.loadup.components.scheduler.cfg.SchedulerBinderCfg;
import io.github.loadup.components.scheduler.cfg.SchedulerBindingCfg;
import io.github.loadup.components.scheduler.model.SchedulerTask;
import io.github.loadup.framework.api.binder.Binder;

/**
 * Base scheduler binder interface providing common scheduler operations. Implementations should
 * provide concrete logic for different scheduler frameworks.
 */
public interface SchedulerBinder<C extends SchedulerBinderCfg, S extends SchedulerBindingCfg> extends Binder<C, S> {
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
     * @param cron the new cron expression
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
