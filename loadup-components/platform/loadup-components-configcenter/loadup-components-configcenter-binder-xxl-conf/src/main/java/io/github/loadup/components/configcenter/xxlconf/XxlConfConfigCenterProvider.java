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
package io.github.loadup.components.configcenter.xxlconf;

import io.github.loadup.components.configcenter.ConfigCenterListeners;
import io.github.loadup.components.configcenter.ConfigCenterProvider;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class XxlConfConfigCenterProvider implements ConfigCenterProvider, AutoCloseable {
    private static final Logger LOGGER = LoggerFactory.getLogger(XxlConfConfigCenterProvider.class);
    private final XxlConfOpenApi api;
    private final ConfigCenterListeners listeners = new ConfigCenterListeners();
    private final Map<String, WatchedValue> watched = new HashMap<>();
    private final ScheduledExecutorService monitor = Executors.newSingleThreadScheduledExecutor(
            Thread.ofVirtual().name("loadup-xxl-conf-monitor").factory());

    public XxlConfConfigCenterProvider(XxlConfConfigCenterProperties properties) {
        api = new XxlConfOpenApi(properties);
        monitor.scheduleWithFixedDelay(this::observe, 0, 1, TimeUnit.SECONDS);
    }

    @Override
    public String getConfig(String key) {
        return value(api.queryValues(List.of(key)).get(key));
    }

    @Override
    public boolean setConfig(String key, String value) {
        return false;
    }

    @Override
    public boolean removeConfig(String key) {
        return false;
    }

    @Override
    public List<String> listKeys(String prefix) {
        return api.queryKeys().stream()
                .filter(key -> key.startsWith(prefix))
                .sorted()
                .toList();
    }

    @Override
    public synchronized Runnable subscribe(String key, Consumer<String> listener) {
        watched.computeIfAbsent(key, name -> new WatchedValue(getConfig(name)));
        Runnable unsubscribe = listeners.subscribe(key, listener);
        return () -> unsubscribe(key, unsubscribe);
    }

    private synchronized void unsubscribe(String key, Runnable unsubscribe) {
        unsubscribe.run();
        if (!listeners.hasListeners(key)) {
            watched.remove(key);
        }
    }

    private void observe() {
        if (watchedSnapshot().isEmpty()) {
            return;
        }
        try {
            api.monitor();
        } catch (RuntimeException e) {
            logFailure("monitor", e);
        }

        // Read after push, normal timeout and monitor failure to reconcile missed notifications.
        Map<String, WatchedValue> previous = watchedSnapshot();
        if (previous.isEmpty() || monitor.isShutdown()) {
            return;
        }
        try {
            var values = api.queryValues(List.copyOf(previous.keySet()));
            previous.forEach((key, before) -> {
                String current = value(values.get(key));
                if (recordChange(key, before, current)) {
                    listeners.dispatch(key, current);
                }
            });
        } catch (RuntimeException e) {
            logFailure("refresh", e);
        }
    }

    private synchronized Map<String, WatchedValue> watchedSnapshot() {
        return Map.copyOf(watched);
    }

    private synchronized boolean recordChange(String key, WatchedValue before, String current) {
        if (watched.get(key) != before || Objects.equals(before.value(), current)) {
            return false;
        }
        watched.put(key, new WatchedValue(current));
        return true;
    }

    private void logFailure(String operation, RuntimeException e) {
        if (!monitor.isShutdown()) {
            LOGGER.warn("XXL-Conf {} failed ({})", operation, e.getClass().getSimpleName());
        }
    }

    private static String value(XxlConfOpenApi.Value entry) {
        return entry == null ? null : entry.value();
    }

    @Override
    public String getBinderType() {
        return "xxl-conf";
    }

    @Override
    public void close() {
        monitor.shutdownNow();
        api.close();
    }

    private record WatchedValue(String value) {}
}
