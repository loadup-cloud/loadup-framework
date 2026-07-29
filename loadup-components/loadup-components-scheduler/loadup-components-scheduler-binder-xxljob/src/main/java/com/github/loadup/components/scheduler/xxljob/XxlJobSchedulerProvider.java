package com.github.loadup.components.scheduler.xxljob;

/*-
 * #%L
 * Loadup Scheduler XXLJob Binder
 * %%
 * Copyright (C) 2025 - 2026 LoadUp Cloud
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

import io.github.loadup.components.scheduler.SchedulerProvider;
import io.github.loadup.components.scheduler.model.SchedulerTask;
import java.util.concurrent.ConcurrentHashMap;

public class XxlJobSchedulerProvider implements SchedulerProvider {
    private final ConcurrentHashMap<String, SchedulerTask> jobs = new ConcurrentHashMap<>();

    public XxlJobSchedulerProvider(XxlJobSchedulerConfig config) {}

    @Override
    public boolean schedule(SchedulerTask task) {
        jobs.put(task.getTaskName(), task);
        return true;
    }

    @Override
    public boolean cancel(String taskName) {
        return jobs.remove(taskName) != null;
    }

    @Override
    public boolean pauseTask(String taskName) {
        return jobs.containsKey(taskName);
    }

    @Override
    public boolean resumeTask(String taskName) {
        return jobs.containsKey(taskName);
    }

    @Override
    public boolean triggerTask(String taskName) {
        return jobs.containsKey(taskName);
    }

    @Override
    public boolean updateTaskCron(String taskName, String cron) {
        return jobs.containsKey(taskName);
    }

    @Override
    public boolean taskExists(String taskName) {
        return jobs.containsKey(taskName);
    }

    @Override
    public String getBinderType() {
        return "xxljob";
    }
}
