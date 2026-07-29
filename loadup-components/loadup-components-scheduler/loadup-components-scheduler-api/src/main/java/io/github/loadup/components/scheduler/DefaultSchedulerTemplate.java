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

public class DefaultSchedulerTemplate implements SchedulerTemplate {
    private final SchedulerProvider provider;

    public DefaultSchedulerTemplate(SchedulerProvider provider) {
        this.provider = provider;
    }

    @Override
    public boolean registerTask(SchedulerTask task) {
        return provider.schedule(task);
    }

    @Override
    public boolean cancel(String taskName) {
        return provider.cancel(taskName);
    }

    @Override
    public boolean pauseTask(String taskName) {
        return provider.pauseTask(taskName);
    }

    @Override
    public boolean resumeTask(String taskName) {
        return provider.resumeTask(taskName);
    }

    @Override
    public boolean triggerTask(String taskName) {
        return provider.triggerTask(taskName);
    }

    @Override
    public boolean updateTaskCron(String taskName, String cron) {
        return provider.updateTaskCron(taskName, cron);
    }

    @Override
    public boolean taskExists(String taskName) {
        return provider.taskExists(taskName);
    }
}
