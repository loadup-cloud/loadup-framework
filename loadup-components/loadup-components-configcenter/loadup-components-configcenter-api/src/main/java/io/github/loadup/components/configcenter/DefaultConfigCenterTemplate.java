package io.github.loadup.components.configcenter;

import java.util.List;
import java.util.function.Consumer;

public class DefaultConfigCenterTemplate implements ConfigCenterTemplate {
    private final ConfigCenterProvider provider;

    public DefaultConfigCenterTemplate(ConfigCenterProvider provider) {
        this.provider = provider;
    }

    @Override public String getConfig(String key) { return provider.getConfig(key); }
    @Override public String getConfig(String key, String defaultValue) {
        String val = provider.getConfig(key);
        return val != null ? val : defaultValue;
    }
    @Override public boolean setConfig(String key, String value) { return provider.setConfig(key, value); }
    @Override public boolean removeConfig(String key) { return provider.removeConfig(key); }
    @Override public List<String> listKeys(String prefix) { return provider.listKeys(prefix); }
    @Override public void addListener(String key, Consumer<String> listener) { provider.addListener(key, listener); }
    @Override public void removeListener(String key) { provider.removeListener(key); }
}
