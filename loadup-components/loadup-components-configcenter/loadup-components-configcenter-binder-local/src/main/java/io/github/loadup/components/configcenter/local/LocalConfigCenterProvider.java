package io.github.loadup.components.configcenter.local;

import io.github.loadup.components.configcenter.ConfigCenterProvider;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

public class LocalConfigCenterProvider implements ConfigCenterProvider {
    private final ConcurrentHashMap<String, String> configs = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, List<Consumer<String>>> listeners = new ConcurrentHashMap<>();

    @Override
    public String getConfig(String key) { return configs.get(key); }

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
    public void removeListener(String key) { listeners.remove(key); }

    @Override
    public String getBinderType() { return "local"; }
}
