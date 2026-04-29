package io.github.loadup.components.configcenter.binding;

import io.github.loadup.components.configcenter.cfg.ConfigCenterBindingCfg;
import io.github.loadup.components.configcenter.model.ConfigChangeListener;
import io.github.loadup.framework.api.binding.Binding;
import io.github.loadup.framework.api.binder.Binder;
import java.util.Optional;

/**
 * High-level business interface for the config center.
 *
 * <p>Hides binder details and provides a business-oriented API for reading,
 * writing, and listening to configuration changes.
 * Obtain an instance via
 * {@link io.github.loadup.components.configcenter.manager.ConfigCenterBindingManager}.
 *
 * <pre>
 * // Get the default binding
 * ConfigCenterBinding binding = manager.getBinding();
 * Optional&lt;String&gt; value = binding.getConfig("feature.flag");
 *
 * // Listen for changes
 * binding.addListener("feature.flag", event -> log.info("changed: {}", event.getNewContent()));
 * </pre>
 */
@SuppressWarnings("rawtypes")
public interface ConfigCenterBinding extends Binding<Binder, ConfigCenterBindingCfg> {

    /**
     * Retrieves config using the binding-level default group.
     *
     * @param dataId config item ID
     * @return config content, or empty if not found
     */
    Optional<String> getConfig(String dataId);

    /**
     * Retrieves config for the given group.
     *
     * @param dataId config item ID
     * @param group  config group
     * @return config content, or empty if not found
     */
    Optional<String> getConfig(String dataId, String group);

    /**
     * Publishes config using the binding-level default group.
     *
     * @param dataId  config item ID
     * @param content config content
     */
    void publishConfig(String dataId, String content);

    /**
     * Publishes config for the given group.
     *
     * @param dataId  config item ID
     * @param group   config group
     * @param content config content
     */
    void publishConfig(String dataId, String group, String content);

    /**
     * Removes config using the binding-level default group.
     *
     * @param dataId config item ID
     * @return {@code true} on success
     */
    boolean removeConfig(String dataId);

    /**
     * Removes config for the given group.
     *
     * @param dataId config item ID
     * @param group  config group
     * @return {@code true} on success
     */
    boolean removeConfig(String dataId, String group);

    /**
     * Registers a change listener using the binding-level default group.
     *
     * @param dataId   config item ID
     * @param listener change callback
     */
    void addListener(String dataId, ConfigChangeListener listener);

    /**
     * Registers a change listener for the given group.
     *
     * @param dataId   config item ID
     * @param group    config group
     * @param listener change callback
     */
    void addListener(String dataId, String group, ConfigChangeListener listener);

    /**
     * Removes a change listener using the binding-level default group.
     *
     * @param dataId   config item ID
     * @param listener the callback to remove
     */
    void removeListener(String dataId, ConfigChangeListener listener);

    /**
     * Removes a change listener for the given group.
     *
     * @param dataId   config item ID
     * @param group    config group
     * @param listener the callback to remove
     */
    void removeListener(String dataId, String group, ConfigChangeListener listener);
}
