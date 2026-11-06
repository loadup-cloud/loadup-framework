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
package io.github.loadup.components.outbox;

import io.github.loadup.commons.log.LogUtil;
import io.github.loadup.commons.util.TenantUtil;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.springframework.context.SmartLifecycle;

public class OutboxWorker implements SmartLifecycle {

    private final OutboxDispatcher dispatcher;
    private final OutboxProperties properties;
    private final String tenantId;
    private volatile ScheduledExecutorService executor;

    public OutboxWorker(OutboxDispatcher dispatcher, OutboxProperties properties) {
        this(dispatcher, properties, null);
    }

    public OutboxWorker(OutboxDispatcher dispatcher, OutboxProperties properties, String tenantId) {
        if (dispatcher == null || properties == null) {
            throw new IllegalArgumentException("Outbox worker requires a dispatcher and settings");
        }
        if (tenantId != null
                && (tenantId.isBlank()
                        || !tenantId.equals(tenantId.trim())
                        || tenantId.length() > 64
                        || tenantId.chars().anyMatch(Character::isISOControl))) {
            throw new IllegalArgumentException("Invalid outbox tenant id");
        }
        this.dispatcher = dispatcher;
        this.properties = properties;
        this.tenantId = tenantId;
    }

    String tenantId() {
        return tenantId;
    }

    @Override
    public synchronized void start() {
        if (isRunning()) {
            return;
        }
        executor = Executors.newSingleThreadScheduledExecutor(runnable -> {
            Thread thread = new Thread(runnable, "loadup-outbox");
            thread.setDaemon(true);
            return thread;
        });
        executor.scheduleWithFixedDelay(
                () -> {
                    try {
                        if (tenantId == null) {
                            dispatcher.dispatchBatch();
                        } else {
                            TenantUtil.runWithTenant(tenantId, dispatcher::dispatchBatch);
                        }
                    } catch (RuntimeException failure) {
                        LogUtil.error(
                                OutboxWorker.class,
                                "Outbox poll failed errorType={}",
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
        return properties.isWorkerEnabled();
    }
}
