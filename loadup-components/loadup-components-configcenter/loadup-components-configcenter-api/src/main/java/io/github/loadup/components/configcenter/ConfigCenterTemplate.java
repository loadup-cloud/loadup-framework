package io.github.loadup.components.configcenter;

import java.util.List;
import java.util.function.Consumer;

public interface ConfigCenterTemplate {
    String getConfig(String key);
    String getConfig(String key, String defaultValue);
    boolean setConfig(String key, String value);
    boolean removeConfig(String key);
    List<String> listKeys(String prefix);
    void addListener(String key, Consumer<String> listener);
    void removeListener(String key);
}
