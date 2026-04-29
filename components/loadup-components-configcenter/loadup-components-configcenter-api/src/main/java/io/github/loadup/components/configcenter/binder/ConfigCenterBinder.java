package io.github.loadup.components.configcenter.binder;

import io.github.loadup.components.configcenter.cfg.ConfigCenterBinderCfg;
import io.github.loadup.components.configcenter.cfg.ConfigCenterBindingCfg;
import io.github.loadup.components.configcenter.model.ConfigChangeListener;
import io.github.loadup.framework.api.binder.Binder;
import java.util.Optional;

/**
 * Low-level driver interface for a config center backend.
 *
 * <p>Each binder implementation wraps a specific config-center SDK
 * (local file system / Nacos / Apollo, etc.) behind a uniform interface.
 *
 * @param <C> binder-level configuration (e.g. NacosConfigCenterBinderCfg)
 * @param <S> binding-level configuration (ConfigCenterBindingCfg or a subclass)
 */
public interface ConfigCenterBinder<C extends ConfigCenterBinderCfg, S extends ConfigCenterBindingCfg>
        extends Binder<C, S> {

    /**
     * Retrieves the config content.
     *
     * @param dataId   config item ID
     * @param group    config group
     * @param settings binding-level settings
     * @return config content, or {@link Optional#empty()} if not found
     */
    Optional<String> getConfig(String dataId, String group, S settings);

    /**
     * Publishes (writes) config content to the config center.
     *
     * @param dataId   config item ID
     * @param group    config group
     * @param content  config content
     * @param settings binding-level settings
     * @return {@code true} on success
     */
    boolean publishConfig(String dataId, String group, String content, S settings);

    /**
     * Removes a config item from the config center.
     *
     * @param dataId   config item ID
     * @param group    config group
     * @param settings binding-level settings
     * @return {@code true} on success
     */
    boolean removeConfig(String dataId, String group, S settings);

    /**
     * Registers a config-change listener.
     *
     * @param dataId   config item ID
     * @param group    config group
     * @param listener change callback
     * @param settings binding-level settings
     */
    void addListener(String dataId, String group, ConfigChangeListener listener, S settings);

    /**
     * 移除配置变更监听器。
     *
     * @param dataId   配置项 ID
     * @param group    配置分组
     * @param listener 要移除的回调
     * @param settings binding 级别配置
     */
    void removeListener(String dataId, String group, ConfigChangeListener listener, S settings);
}
