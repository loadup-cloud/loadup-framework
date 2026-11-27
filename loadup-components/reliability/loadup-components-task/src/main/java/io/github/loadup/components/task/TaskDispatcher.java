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

import io.github.loadup.commons.enums.CommonResultCodeEnum;
import io.github.loadup.commons.error.CommonException;
import io.github.loadup.commons.log.LogUtil;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationRegistry;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import org.slf4j.MDC;

public final class TaskDispatcher {
    private static final int MAX_CONCURRENT_TASKS = 4;

    private final JdbcTaskRepository repository;
    private final TaskProperties properties;
    private final Map<String, TaskHandler> handlers;
    private final List<String> taskTypes;
    private final MeterRegistry metrics;
    private final ObservationRegistry observations;

    public TaskDispatcher(
            JdbcTaskRepository repository,
            TaskProperties properties,
            List<TaskHandler> handlers,
            MeterRegistry metrics) {
        this(repository, properties, handlers, metrics, ObservationRegistry.NOOP);
    }

    public TaskDispatcher(
            JdbcTaskRepository repository,
            TaskProperties properties,
            List<TaskHandler> handlers,
            MeterRegistry metrics,
            ObservationRegistry observations) {
        this.repository = repository;
        this.properties = properties;
        this.metrics = metrics;
        this.observations = observations;
        Map<String, TaskHandler> registry = new HashMap<>();
        for (TaskHandler handler : handlers) {
            String type = TaskSubmission.required(handler.taskType(), "taskType", 128);
            if (registry.putIfAbsent(type, handler) != null) {
                throw new IllegalArgumentException("Duplicate task type: " + type);
            }
        }
        this.handlers = Map.copyOf(registry);
        String configuredType = properties.getTaskType();
        this.taskTypes = this.handlers.keySet().stream()
                .filter(type -> configuredType == null || configuredType.equals(type))
                .sorted()
                .toList();
    }

    boolean hasHandlers() {
        return !taskTypes.isEmpty();
    }

    private void dispatchBatch() {
        var slots = new Semaphore(MAX_CONCURRENT_TASKS);
        var threadFactory =
                Thread.ofVirtual().inheritInheritableThreadLocals(false).factory();
        try (var executor = Executors.newThreadPerTaskExecutor(threadFactory)) {
            for (int i = 0;
                    i < properties.getBatchSize() && !Thread.currentThread().isInterrupted();
                    i++) {
                try {
                    slots.acquire();
                } catch (InterruptedException failure) {
                    Thread.currentThread().interrupt();
                    return;
                }
                var next = repository.claim(taskTypes);
                if (next.isEmpty()) {
                    return;
                }
                var claim = next.orElseThrow();
                var handler = handlers.get(claim.event().taskType());
                if (handler.supportsConcurrentExecution()) {
                    executor.execute(() -> {
                        try {
                            dispatch(claim, handler);
                        } finally {
                            slots.release();
                        }
                    });
                } else {
                    dispatch(claim, handler);
                    slots.release();
                }
            }
        }
    }

    private void dispatch(JdbcTaskRepository.Claim claim, TaskHandler handler) {
        Map<String, String> previous = MDC.getCopyOfContextMap();
        long started = System.nanoTime();
        String outcome = "failure";
        Observation observation = Observation.createNotStarted("loadup.task.handle", observations);
        try {
            MDC.clear();
            if (claim.event().traceId() != null) {
                MDC.put("originTraceId", claim.event().traceId());
            }
            observation.start();
            try (Observation.Scope unusedScope = observation.openScope()) {
                try {
                    handler.handle(claim.event());
                } catch (Exception exception) {
                    throw new HandlerFailure(exception);
                }
                outcome = repository.complete(claim) ? "success" : "lease_lost";
            }
        } catch (Exception exception) {
            Exception failure = exception instanceof HandlerFailure wrapper ? wrapper.failure : exception;
            observation.error(new IllegalStateException(
                    "Task handler failed: " + failure.getClass().getSimpleName()));
            outcome = repository.fail(claim, failure) ? "failure" : "lease_lost";
            LogUtil.warn(
                    TaskDispatcher.class,
                    "Task delivery failed eventId={} attempt={} errorType={}",
                    claim.event().id(),
                    claim.attempt(),
                    failure.getClass().getSimpleName());
            if (failure instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
        } finally {
            try {
                observation.stop();
                if (metrics != null) {
                    metrics.timer("loadup.task.delivery", "outcome", outcome)
                            .record(System.nanoTime() - started, TimeUnit.NANOSECONDS);
                }
            } finally {
                if (previous == null) {
                    MDC.clear();
                } else {
                    MDC.setContextMap(previous);
                }
            }
        }
    }

    public void execute() {
        if (hasHandlers()) {
            repository.withoutTransaction(this::dispatchBatch);
        }
    }

    private static final class HandlerFailure extends CommonException {
        private final Exception failure;

        HandlerFailure(Exception failure) {
            super(CommonResultCodeEnum.SYS_ERROR, failure);
            this.failure = failure;
        }
    }
}
