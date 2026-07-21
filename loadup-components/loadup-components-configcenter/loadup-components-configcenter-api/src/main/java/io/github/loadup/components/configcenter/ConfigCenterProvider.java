package io.github.loadup.components.configcenter;

import java.util.List;

public interface ConfigCenterProvider {
    String getConfig(String key);
    boolean setConfig(String key, String value);
    boolean removeConfig(String key);
    List<String> listKeys(String prefix);
    void addListener(String key, java.util.function.Consumer<String> listener);
    void removeListener(String key);
    String getBinderType();
}
