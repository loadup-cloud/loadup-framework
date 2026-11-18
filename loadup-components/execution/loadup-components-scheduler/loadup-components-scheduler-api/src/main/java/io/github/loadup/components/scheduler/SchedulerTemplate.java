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
package io.github.loadup.components.scheduler;

import io.github.loadup.commons.enums.CommonResultCodeEnum;
import io.github.loadup.commons.error.CommonException;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import org.springframework.context.SmartLifecycle;

public final class SchedulerTemplate implements SmartLifecycle {
    private final SchedulerProvider provider;
    private final Map<String, ScheduledTask> tasks;
    private final List<TaskSchedule> schedules;
    private final Set<String> scheduledNames;
    private final Map<String, AtomicBoolean> active = new ConcurrentHashMap<>();
    private volatile boolean running;

    public SchedulerTemplate(SchedulerProvider provider, List<ScheduledTask> tasks) {
        this(provider, tasks, List.of());
    }

    public SchedulerTemplate(SchedulerProvider provider, List<ScheduledTask> tasks, List<TaskSchedule> schedules) {
        this.provider = provider;
        var registry = new LinkedHashMap<String, ScheduledTask>();
        for (var task : tasks) {
            String name = task.name();
            if (!TaskSchedule.isValidName(name) || registry.putIfAbsent(name, task) != null) {
                throw new CommonException(CommonResultCodeEnum.PARAM_ILLEGAL);
            }
        }
        this.tasks = Collections.unmodifiableMap(registry);
        this.schedules = List.copyOf(schedules);
        var names = new HashSet<String>();
        for (var schedule : this.schedules) {
            if (!registry.containsKey(schedule.name())) {
                throw new IllegalArgumentException("No ScheduledTask bean for '" + schedule.name() + "'");
            }
            if (!names.add(schedule.name())) {
                throw new IllegalArgumentException("Duplicate scheduler configuration for '" + schedule.name() + "'");
            }
        }
        scheduledNames = Set.copyOf(names);
    }

    public Set<String> taskNames() {
        return tasks.keySet();
    }

    public void trigger(String name) throws Exception {
        var task = tasks.get(name);
        if (task == null) {
            throw new CommonException(CommonResultCodeEnum.NOT_FOUND);
        }
        var guard = active.computeIfAbsent(name, ignored -> new AtomicBoolean());
        if (!guard.compareAndSet(false, true)) {
            return;
        }
        try {
            task.execute();
        } finally {
            guard.set(false);
        }
    }

    @Override
    public synchronized void start() {
        if (!running) {
            provider.start(schedules, name -> {
                if (name == null || !scheduledNames.contains(name)) {
                    throw new CommonException(CommonResultCodeEnum.NOT_FOUND);
                }
                trigger(name);
            });
            running = true;
        }
    }

    @Override
    public synchronized void stop() {
        if (running) {
            provider.stop();
            running = false;
        }
    }

    @Override
    public boolean isRunning() {
        return running;
    }
}
