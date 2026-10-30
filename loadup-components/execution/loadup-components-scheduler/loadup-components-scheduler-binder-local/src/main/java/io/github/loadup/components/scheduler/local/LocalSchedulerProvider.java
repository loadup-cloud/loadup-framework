/*
 * #%L
 * LoadUp Components Scheduler
 * %%
 * Copyright (C) 2025 - 2026 LoadUp Cloud
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
package io.github.loadup.components.scheduler.local;

import io.github.loadup.commons.log.LogUtil;
import io.github.loadup.components.scheduler.SchedulerProvider;
import io.github.loadup.components.scheduler.SchedulerTemplate;
import org.springframework.scheduling.annotation.Scheduled;

/**
 * Uses Spring's scheduler thread; failures in one scanner do not suppress other scanners.
 */
public class LocalSchedulerProvider implements SchedulerProvider {
    private volatile SchedulerTemplate scheduler;

    @Override
    public void start(SchedulerTemplate scheduler) {
        this.scheduler = scheduler;
    }

    @Override
    public void stop() {
        this.scheduler = null;
    }

    @Scheduled(
            fixedDelayString = "${loadup.scheduler.local.interval:PT1M}",
            initialDelayString = "${loadup.scheduler.local.initial-delay:PT1M}")
    public void wakeUp() {
        var current = scheduler;
        if (current == null) {
            return;
        }
        for (String task : current.taskNames()) {
            try {
                current.trigger(task);
            } catch (Exception failure) {
                LogUtil.warn(
                        LocalSchedulerProvider.class,
                        "Scanner failed task={} type={}",
                        task,
                        failure.getClass().getSimpleName());
            }
        }
    }
}
