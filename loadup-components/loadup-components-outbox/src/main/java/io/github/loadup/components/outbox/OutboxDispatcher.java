/*
 * #%L
 * LoadUp Outbox
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
package io.github.loadup.components.outbox;

import io.github.loadup.commons.log.LogUtil;
import io.github.loadup.commons.util.TenantUtil;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationRegistry;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.MDC;

/** Runs handlers outside claim transactions and restores all per-thread context. */
public class OutboxDispatcher {

    private final JdbcOutboxRepository repository;
    private final OutboxProperties properties;
    private final Map<String, OutboxHandler> handlers;
    private final MeterRegistry metrics;
    private final ObservationRegistry observations;

    public OutboxDispatcher(
            JdbcOutboxRepository repository,
            OutboxProperties properties,
            List<OutboxHandler> handlers,
            MeterRegistry metrics) {
        this(repository, properties, handlers, metrics, ObservationRegistry.NOOP);
    }

    public OutboxDispatcher(
            JdbcOutboxRepository repository,
            OutboxProperties properties,
            List<OutboxHandler> handlers,
            MeterRegistry metrics,
            ObservationRegistry observations) {
        this.repository = repository;
        this.properties = properties;
        this.metrics = metrics;
        this.observations = observations;
        Map<String, OutboxHandler> registry = new HashMap<>();
        for (OutboxHandler handler : handlers) {
            String type = OutboxMessage.required(handler.eventType(), "eventType", 128);
            if (registry.putIfAbsent(type, handler) != null)
                throw new IllegalArgumentException("Duplicate outbox event type: " + type);
        }
        this.handlers = Map.copyOf(registry);
    }

    public int dispatchBatch() {
        int count = 0;
        for (int i = 0; i < properties.getBatchSize() && !Thread.currentThread().isInterrupted(); i++) {
            var next = repository.claim();
            if (next.isEmpty()) break;
            var claim = next.orElseThrow();
            Map<String, String> previous = MDC.getCopyOfContextMap();
            long started = System.nanoTime();
            String outcome = "failure";
            Observation observation = Observation.createNotStarted("loadup.outbox.handle", observations);
            try {
                MDC.clear();
                if (claim.event().traceId() != null)
                    MDC.put("originTraceId", claim.event().traceId());
                observation.start();
                try (Observation.Scope scope = observation.openScope()) {
                    OutboxHandler handler = handlers.get(claim.event().message().type());
                    if (handler == null) throw new IllegalStateException("No handler registered");
                    TenantUtil.runWithTenant(claim.event().message().tenantId(), () -> {
                        try {
                            handler.handle(claim.event());
                        } catch (Exception exception) {
                            throw new HandlerFailure(exception);
                        }
                    });
                    outcome = repository.complete(claim) ? "success" : "lease_lost";
                }
            } catch (Exception exception) {
                Exception failure = exception instanceof HandlerFailure wrapper ? wrapper.failure : exception;
                observation.error(new IllegalStateException(
                        "Outbox handler failed: " + failure.getClass().getSimpleName()));
                repository.fail(claim, failure);
                LogUtil.warn(
                        OutboxDispatcher.class,
                        "Outbox delivery failed eventId={} attempt={} errorType={}",
                        claim.event().id(),
                        claim.attempt(),
                        failure.getClass().getSimpleName());
                if (failure instanceof InterruptedException)
                    Thread.currentThread().interrupt();
            } finally {
                try {
                    observation.stop();
                    if (metrics != null)
                        metrics.timer("loadup.outbox.delivery", "outcome", outcome)
                                .record(System.nanoTime() - started, java.util.concurrent.TimeUnit.NANOSECONDS);
                } finally {
                    if (previous == null) MDC.clear();
                    else MDC.setContextMap(previous);
                }
            }
            count++;
        }
        return count;
    }

    private static final class HandlerFailure extends RuntimeException {
        private final Exception failure;

        HandlerFailure(Exception failure) {
            super("Outbox handler failed");
            this.failure = failure;
        }
    }
}
