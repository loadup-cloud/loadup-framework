package io.github.loadup.components.scheduler;

/*-
 * #%L
 * Loadup Scheduler Api
 * %%
 * Copyright (C) 2025 - 2026 loadup_cloud
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

import io.github.loadup.components.scheduler.model.SchedulerTask;

public interface SchedulerTemplate {
    boolean registerTask(SchedulerTask task);
    boolean cancel(String taskName);
    boolean pauseTask(String taskName);
    boolean resumeTask(String taskName);
    boolean triggerTask(String taskName);
    boolean updateTaskCron(String taskName, String cron);
    boolean taskExists(String taskName);
}
