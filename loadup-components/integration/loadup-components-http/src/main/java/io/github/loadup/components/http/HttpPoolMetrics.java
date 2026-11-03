/*
 * #%L
 * LoadUp Components HTTP
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
package io.github.loadup.components.http;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.ToDoubleFunction;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;

final class HttpPoolMetrics implements AutoCloseable {
    private final MeterRegistry registry;
    private final String scope;
    private final Map<String, State> states = new HashMap<>();

    HttpPoolMetrics(MeterRegistry registry, String scope) {
        this.registry = registry;
        this.scope = scope;
    }

    State candidate(String client, PoolingHttpClientConnectionManager pool) {
        return new State(client, pool);
    }

    // Called under the template write lock, after all replacement transports were built.
    void activate(Map<String, State> replacement) {
        if (registry == null) {
            return;
        }
        synchronized (registry) {
            List<State> added = new ArrayList<>();
            try {
                for (var entry : replacement.entrySet()) {
                    if (!states.containsKey(entry.getKey())) {
                        State state = entry.getValue();
                        added.add(state);
                        state.register();
                    }
                }
            } catch (RuntimeException failure) {
                added.forEach(State::remove);
                throw failure;
            }
            states.entrySet().removeIf(entry -> {
                if (replacement.containsKey(entry.getKey())) {
                    return false;
                }
                entry.getValue().remove();
                return true;
            });
            replacement.forEach((name, candidate) -> {
                State retained = states.get(name);
                if (retained == null) {
                    states.put(name, candidate);
                } else {
                    retained.pool = candidate.pool;
                    candidate.timers = retained.timers;
                }
            });
        }
    }

    @Override
    public void close() {
        if (registry != null) {
            synchronized (registry) {
                states.values().forEach(State::remove);
                states.clear();
            }
        }
    }

    final class State {
        private final String client;
        private final List<Meter> owned = new ArrayList<>();
        private volatile PoolingHttpClientConnectionManager pool;
        private Map<String, Timer> timers = Map.of();

        private State(String client, PoolingHttpClientConnectionManager pool) {
            this.client = client;
            this.pool = pool;
        }

        private void register() {
            for (String suffix : List.of("max", "leased", "available", "pending", "route.max", "acquire")) {
                if (!registry.find("loadup.http.pool." + suffix)
                        .tags("scope", scope, "client", client)
                        .meters()
                        .isEmpty()) {
                    throw new IllegalArgumentException("HTTP pool metric scope/client is already in use");
                }
            }
            gauge("max", p -> p.getTotalStats().getMax());
            gauge("leased", p -> p.getTotalStats().getLeased());
            gauge("available", p -> p.getTotalStats().getAvailable());
            gauge("pending", p -> p.getTotalStats().getPending());
            gauge("route.max", PoolingHttpClientConnectionManager::getDefaultMaxPerRoute);
            Map<String, Timer> registered = new HashMap<>();
            for (String outcome : List.of("success", "timeout", "interrupted", "failure")) {
                Timer timer = Timer.builder("loadup.http.pool.acquire")
                        .description("Connection lease acquisition, excluding TCP connect and HTTP exchange")
                        .tags("scope", scope, "client", client, "outcome", outcome)
                        .register(registry);
                owned.add(timer);
                registered.put(outcome, timer);
            }
            timers = Map.copyOf(registered);
        }

        private void gauge(String suffix, ToDoubleFunction<PoolingHttpClientConnectionManager> value) {
            owned.add(Gauge.builder("loadup.http.pool." + suffix, this, state -> {
                        var current = state.pool;
                        return current == null ? 0 : value.applyAsDouble(current);
                    })
                    .tags("scope", scope, "client", client)
                    .register(registry));
        }

        long start() {
            return registry == null ? 0 : registry.config().clock().monotonicTime();
        }

        void record(long start, String outcome) {
            Timer timer = timers.get(outcome);
            if (timer != null) {
                timer.record(registry.config().clock().monotonicTime() - start, TimeUnit.NANOSECONDS);
            }
        }

        private void remove() {
            pool = null;
            owned.forEach(registry::remove);
            owned.clear();
        }
    }
}
