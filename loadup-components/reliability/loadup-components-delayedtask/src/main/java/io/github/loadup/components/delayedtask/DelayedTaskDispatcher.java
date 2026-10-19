/*
 * #%L
 * LoadUp Components DelayedTask
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
package io.github.loadup.components.delayedtask;

import io.github.loadup.commons.context.*;
import io.github.loadup.commons.error.CommonException;
import io.github.loadup.commons.log.LogUtil;
import io.github.loadup.components.scheduler.ScheduledTask;
import io.micrometer.observation.*;
import java.time.*;
import java.util.*;
import org.springframework.transaction.annotation.*;

/** Calls business code outside claim transactions, with at-least-once delivery semantics. */
public class DelayedTaskDispatcher implements ScheduledTask {
    private final DelayedTaskStore store;
    private final DelayedTaskConverter converter;
    private final DelayedTaskProperties properties;
    private final Clock clock;
    private final ObservationRegistry observations;
    private final Map<String, DelayedTaskHandler> handlers;

    public DelayedTaskDispatcher(
            DelayedTaskStore store,
            DelayedTaskConverter converter,
            DelayedTaskProperties props,
            Clock clock,
            ObservationRegistry observations,
            List<DelayedTaskHandler> handlers) {
        this(store, converter, props, clock, observations, indexHandlers(handlers));
    }

    private DelayedTaskDispatcher(
            DelayedTaskStore store,
            DelayedTaskConverter converter,
            DelayedTaskProperties props,
            Clock clock,
            ObservationRegistry observations,
            Map<String, DelayedTaskHandler> handlers) {
        this.store = store;
        this.converter = converter;
        this.properties = props;
        this.clock = clock;
        this.observations = observations;
        this.handlers = handlers;
    }

    // Validate before constructor delegation reaches Object initialization; retain CGLIB support.
    private static Map<String, DelayedTaskHandler> indexHandlers(List<DelayedTaskHandler> handlers) {
        var map = new HashMap<String, DelayedTaskHandler>();
        for (var handler : handlers) {
            if (handler.taskType() == null || map.putIfAbsent(handler.taskType(), handler) != null)
                throw new CommonException(DelayedTaskResultCodeEnum.INVALID_ARGUMENT);
        }
        return Map.copyOf(map);
    }

    public String name() {
        return "delayedtask.scan";
    }

    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public void execute() {
        store.recoverExhausted(LocalDateTime.now(clock));
        for (int i = 0; i < properties.getBatchSize() && !Thread.currentThread().isInterrupted(); i++) {
            var next = store.claim(LocalDateTime.now(clock), properties);
            if (next.isEmpty()) break;
            var claimed = next.orElseThrow();
            var task = converter.toTask(claimed);
            boolean success = false;
            var handler = handlers.get(task.taskType());
            Observation observation = Observation.createNotStarted("loadup.delayedtask.execute", observations)
                    .lowCardinalityKeyValue("task.type", handler == null ? "unregistered" : task.taskType())
                    .start();
            try (var _ = observation.openScope()) {
                if (handler == null) throw new CommonException(DelayedTaskResultCodeEnum.HANDLER_MISSING);
                ContextHolder.callWith(ExecutionContext.empty().with(ContextKeys.TENANT_ID, task.tenantId()), () -> {
                    handler.handle(task);
                    return null;
                });
                success = true;
            } catch (Exception failure) {
                observation.error(new CommonException(DelayedTaskResultCodeEnum.HANDLER_FAILED));
                LogUtil.warn(
                        DelayedTaskDispatcher.class,
                        "Delayed task failed id={} attempt={} type={}",
                        task.id(),
                        task.attempts(),
                        failure.getClass().getSimpleName());
                if (failure instanceof InterruptedException)
                    Thread.currentThread().interrupt();
            } finally {
                try {
                    boolean acknowledged = store.finish(claimed, LocalDateTime.now(clock), success, properties);
                    observation.lowCardinalityKeyValue(
                            "outcome", acknowledged ? (success ? "success" : "failure") : "lease_lost");
                } finally {
                    observation.stop();
                }
            }
        }
    }
}
