/*
 * Copyright (C) 2025-2026 LoadUp Cloud
 *
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
 */
package io.github.loadup.components.scheduler.local;

import io.github.loadup.commons.log.LogUtil;
import io.github.loadup.components.scheduler.SchedulerProvider;
import io.github.loadup.components.scheduler.TaskSchedule;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ScheduledFuture;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.support.CronTrigger;

public class LocalSchedulerProvider implements SchedulerProvider {
    private final List<ScheduledFuture<?>> scheduled = new ArrayList<>();
    private ThreadPoolTaskScheduler scheduler;

    @Override
    public void start(List<TaskSchedule> schedules, TaskTrigger trigger) {
        if (schedules.isEmpty()) {
            return;
        }
        scheduler = new ThreadPoolTaskScheduler();
        scheduler.setThreadNamePrefix("loadup-scheduler-");
        scheduler.setWaitForTasksToCompleteOnShutdown(true);
        try {
            scheduler.initialize();
            for (var task : schedules) {
                var future = scheduler.schedule(() -> execute(task.name(), trigger), new CronTrigger(task.cron()));
                scheduled.add(Objects.requireNonNull(future, "No future cron occurrence for " + task.name()));
            }
        } catch (RuntimeException | Error failure) {
            stop();
            throw failure;
        }
    }

    @Override
    public void stop() {
        scheduled.forEach(future -> future.cancel(false));
        scheduled.clear();
        if (scheduler != null) {
            scheduler.shutdown();
            scheduler = null;
        }
    }

    private void execute(String name, TaskTrigger trigger) {
        try {
            trigger.trigger(name);
        } catch (Exception failure) {
            LogUtil.warn(
                    LocalSchedulerProvider.class,
                    "Scheduled task failed task={} type={}",
                    name,
                    failure.getClass().getSimpleName());
        }
    }
}
