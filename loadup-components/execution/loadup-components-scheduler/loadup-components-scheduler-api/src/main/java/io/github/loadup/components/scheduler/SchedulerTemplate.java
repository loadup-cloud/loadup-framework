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
package io.github.loadup.components.scheduler;

import io.github.loadup.commons.enums.CommonResultCodeEnum;
import io.github.loadup.commons.error.CommonException;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import org.springframework.context.SmartLifecycle;

/**
 * Resolves registered scanners and suppresses overlapping calls within this application.
 */
public final class SchedulerTemplate implements SmartLifecycle {
    private final SchedulerProvider provider;
    private final Map<String, ScheduledTask> tasks;
    private final Map<String, AtomicBoolean> active = new java.util.concurrent.ConcurrentHashMap<>();
    private volatile boolean running;

    public SchedulerTemplate(SchedulerProvider provider, List<ScheduledTask> scanners) {
        this.provider = provider;
        var registry = new LinkedHashMap<String, ScheduledTask>();
        for (var task : scanners) {
            if (task.name() == null
                    || !task.name().matches("[A-Za-z][A-Za-z0-9_.-]{0,63}")
                    || registry.putIfAbsent(task.name(), task) != null)
                throw new CommonException(CommonResultCodeEnum.PARAM_ILLEGAL);
        }
        tasks = Collections.unmodifiableMap(registry);
    }

    public Set<String> taskNames() {
        return tasks.keySet();
    }

    public void trigger(String name) throws Exception {
        var task = tasks.get(name);
        if (task == null) throw new CommonException(CommonResultCodeEnum.NOT_FOUND);
        var guard = active.computeIfAbsent(name, ignored -> new AtomicBoolean());
        if (!guard.compareAndSet(false, true)) return;
        try {
            task.execute();
        } finally {
            guard.set(false);
        }
    }

    @Override
    public synchronized void start() {
        if (!running) {
            provider.start(this);
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
