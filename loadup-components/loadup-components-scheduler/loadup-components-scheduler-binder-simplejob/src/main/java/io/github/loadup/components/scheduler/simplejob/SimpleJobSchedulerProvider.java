package io.github.loadup.components.scheduler.simplejob;

/*-
 * #%L
 * Loadup Scheduler Simplejob Binder
 * %%
 * Copyright (C) 2025 - 2026 loadup_cloud
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import io.github.loadup.components.scheduler.SchedulerProvider;
import io.github.loadup.components.scheduler.model.SchedulerTask;
import java.util.concurrent.ConcurrentHashMap;

public class SimpleJobSchedulerProvider implements SchedulerProvider {
    private final ConcurrentHashMap<String, SchedulerTask> jobs = new ConcurrentHashMap<>();

    public SimpleJobSchedulerProvider(SimpleJobSchedulerConfig config) {
        // SimpleJob 仅内存注册，不实际执行
    }

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
        return "simplejob";
    }
}
