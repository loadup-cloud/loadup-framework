package io.github.loadup.components.scheduler;

/*-
 * #%L
 * Loadup Scheduler Api
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
