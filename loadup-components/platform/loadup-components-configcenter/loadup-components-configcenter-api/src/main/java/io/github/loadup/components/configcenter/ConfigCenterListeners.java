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
package io.github.loadup.components.configcenter;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Stores independently removable subscriptions for binders with a shared SDK or OpenAPI listener.
 */
public final class ConfigCenterListeners {
    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigCenterListeners.class);

    private final ConcurrentHashMap<String, List<Consumer<String>>> subscriptions = new ConcurrentHashMap<>();

    public Runnable subscribe(String key, Consumer<String> listener) {
        Consumer<String> subscription = listener::accept;
        subscriptions.compute(key, (ignored, current) -> {
            List<Consumer<String>> listeners = current == null ? new CopyOnWriteArrayList<>() : current;
            listeners.add(subscription);
            return listeners;
        });
        return () -> subscriptions.computeIfPresent(key, (ignored, listeners) -> {
            listeners.remove(subscription);
            return listeners.isEmpty() ? null : listeners;
        });
    }

    public boolean hasListeners(String key) {
        return subscriptions.containsKey(key);
    }

    public void dispatch(String key, String value) {
        for (Consumer<String> listener : subscriptions.getOrDefault(key, List.of())) {
            try {
                listener.accept(value);
            } catch (RuntimeException e) {
                LOGGER.warn(
                        "Configuration listener failed for key {} ({})",
                        key,
                        e.getClass().getSimpleName());
            }
        }
    }
}
