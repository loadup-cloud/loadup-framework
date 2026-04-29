package io.github.loadup.components.configcenter.binding;

import io.github.loadup.components.configcenter.cfg.ConfigCenterBindingCfg;
import io.github.loadup.components.configcenter.model.ConfigChangeListener;
import io.github.loadup.framework.api.binding.Binding;
import io.github.loadup.framework.api.binder.Binder;
import java.util.Optional;

/**
 * 配置中心高阶业务接口。
 *
 * <p>屏蔽 binder 细节，提供面向业务的配置读写与监听 API。
 * 通过 {@link io.github.loadup.components.configcenter.manager.ConfigCenterBindingManager} 获取实例。
 *
 * <pre>
 * // 获取默认 binding
 * ConfigCenterBinding binding = manager.getBinding();
 * Optional&lt;String&gt; value = binding.getConfig("feature.flag");
 *
 * // 监听变更
 * binding.addListener("feature.flag", event -> log.info("changed: {}", event.getNewContent()));
 * </pre>
 */
@SuppressWarnings("rawtypes")
public interface ConfigCenterBinding extends Binding<Binder, ConfigCenterBindingCfg> {

    /**
     * 查询配置（使用 binding 配置的 defaultGroup）。
     *
     * @param dataId 配置项 ID
     * @return 配置内容，不存在时为 empty
     */
    Optional<String> getConfig(String dataId);

    /**
     * 查询配置（指定 group）。
     *
     * @param dataId 配置项 ID
     * @param group  配置分组
     * @return 配置内容，不存在时为 empty
     */
    Optional<String> getConfig(String dataId, String group);

    /**
     * 发布配置（使用 binding 配置的 defaultGroup）。
     *
     * @param dataId  配置项 ID
     * @param content 配置内容
     */
    void publishConfig(String dataId, String content);

    /**
     * 发布配置（指定 group）。
     *
     * @param dataId  配置项 ID
     * @param group   配置分组
     * @param content 配置内容
     */
    void publishConfig(String dataId, String group, String content);

    /**
     * 删除配置（使用 binding 配置的 defaultGroup）。
     *
     * @param dataId 配置项 ID
     * @return true 表示成功
     */
    boolean removeConfig(String dataId);

    /**
     * 删除配置（指定 group）。
     *
     * @param dataId 配置项 ID
     * @param group  配置分组
     * @return true 表示成功
     */
    boolean removeConfig(String dataId, String group);

    /**
     * 注册配置变更监听器（使用 binding 配置的 defaultGroup）。
     *
     * @param dataId   配置项 ID
     * @param listener 变更回调
     */
    void addListener(String dataId, ConfigChangeListener listener);

    /**
     * 注册配置变更监听器（指定 group）。
     *
     * @param dataId   配置项 ID
     * @param group    配置分组
     * @param listener 变更回调
     */
    void addListener(String dataId, String group, ConfigChangeListener listener);

    /**
     * 移除配置变更监听器（使用 binding 配置的 defaultGroup）。
     *
     * @param dataId   配置项 ID
     * @param listener 要移除的回调
     */
    void removeListener(String dataId, ConfigChangeListener listener);

    /**
     * 移除配置变更监听器（指定 group）。
     *
     * @param dataId   配置项 ID
     * @param group    配置分组
     * @param listener 要移除的回调
     */
    void removeListener(String dataId, String group, ConfigChangeListener listener);
}
