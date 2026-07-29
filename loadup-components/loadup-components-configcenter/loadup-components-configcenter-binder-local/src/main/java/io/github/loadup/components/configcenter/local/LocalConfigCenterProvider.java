package io.github.loadup.components.configcenter.local;

/*-
 * #%L
 * LoadUp ConfigCenter Binder Local
 * %%
 * Copyright (C) 2025 - 2026 loadup_cloud
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

import io.github.loadup.components.configcenter.ConfigCenterProvider;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

public class LocalConfigCenterProvider implements ConfigCenterProvider {
    private final ConcurrentHashMap<String, String> configs = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, List<Consumer<String>>> listeners = new ConcurrentHashMap<>();

    @Override
    public String getConfig(String key) {
        return configs.get(key);
    }

    @Override
    public boolean setConfig(String key, String value) {
        configs.put(key, value);
        List<Consumer<String>> ls = listeners.get(key);
        if (ls != null) ls.forEach(l -> l.accept(value));
        return true;
    }

    @Override
    public boolean removeConfig(String key) {
        configs.remove(key);
        return true;
    }

    @Override
    public List<String> listKeys(String prefix) {
        return configs.keySet().stream().filter(k -> k.startsWith(prefix)).toList();
    }

    @Override
    public void addListener(String key, Consumer<String> listener) {
        listeners.computeIfAbsent(key, k -> new CopyOnWriteArrayList<>()).add(listener);
    }

    @Override
    public void removeListener(String key) {
        listeners.remove(key);
    }

    @Override
    public String getBinderType() {
        return "local";
    }
}
