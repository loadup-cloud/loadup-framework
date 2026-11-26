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
package io.github.loadup.components.configcenter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class DefaultConfigCenterTemplate implements ConfigCenterTemplate {
    private final ConfigCenterProvider provider;
    private final Map<String, List<Runnable>> listeners = new HashMap<>();

    public DefaultConfigCenterTemplate(ConfigCenterProvider provider) {
        this.provider = provider;
    }

    @Override
    public String getConfig(String key) {
        return provider.getConfig(key);
    }

    @Override
    public String getConfig(String key, String defaultValue) {
        String val = provider.getConfig(key);
        return val != null ? val : defaultValue;
    }

    @Override
    public boolean setConfig(String key, String value) {
        return provider.setConfig(key, value);
    }

    @Override
    public boolean removeConfig(String key) {
        return provider.removeConfig(key);
    }

    @Override
    public List<String> listKeys(String prefix) {
        return provider.listKeys(prefix);
    }

    @Override
    public synchronized void addListener(String key, Consumer<String> listener) {
        Runnable unsubscribe = provider.subscribe(key, listener);
        listeners.computeIfAbsent(key, ignored -> new ArrayList<>()).add(unsubscribe);
    }

    @Override
    public synchronized void removeListener(String key) {
        List<Runnable> subscriptions = listeners.remove(key);
        if (subscriptions != null) {
            subscriptions.forEach(Runnable::run);
        }
    }
}
