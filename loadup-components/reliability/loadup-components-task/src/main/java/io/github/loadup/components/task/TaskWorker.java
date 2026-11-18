/*
 * Copyright (C) 2026 LoadUp Cloud
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
package io.github.loadup.components.task;

import io.github.loadup.commons.log.LogUtil;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.springframework.context.SmartLifecycle;

public class TaskWorker implements SmartLifecycle {

    private final TaskDispatcher dispatcher;
    private final TaskProperties properties;
    private volatile ScheduledExecutorService executor;

    public TaskWorker(TaskDispatcher dispatcher, TaskProperties properties) {
        if (dispatcher == null || properties == null) {
            throw new IllegalArgumentException("Task worker requires a dispatcher and settings");
        }
        this.dispatcher = dispatcher;
        this.properties = properties;
    }

    @Override
    public synchronized void start() {
        if (isRunning() || !isAutoStartup()) {
            return;
        }
        executor = Executors.newSingleThreadScheduledExecutor(runnable -> {
            Thread thread = new Thread(runnable, "loadup-task");
            thread.setDaemon(true);
            return thread;
        });
        executor.scheduleWithFixedDelay(
                () -> {
                    try {
                        dispatcher.execute();
                    } catch (RuntimeException failure) {
                        LogUtil.error(
                                TaskWorker.class,
                                "Task poll failed errorType={}",
                                failure.getClass().getSimpleName());
                    }
                },
                properties.getPollInterval().toMillis(),
                properties.getPollInterval().toMillis(),
                TimeUnit.MILLISECONDS);
    }

    @Override
    public synchronized void stop() {
        if (executor != null) {
            executor.shutdownNow();
        }
        executor = null;
    }

    @Override
    public boolean isRunning() {
        return executor != null && !executor.isShutdown();
    }

    @Override
    public boolean isAutoStartup() {
        return properties.isWorkerEnabled()
                && properties.getTrigger() == TaskProperties.Trigger.LOCAL
                && dispatcher.hasHandlers();
    }
}
