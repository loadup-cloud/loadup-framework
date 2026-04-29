package io.github.loadup.components.configcenter.binder;

import io.github.loadup.components.configcenter.cfg.ConfigCenterBinderCfg;
import io.github.loadup.components.configcenter.cfg.ConfigCenterBindingCfg;
import io.github.loadup.components.configcenter.model.ConfigChangeListener;
import io.github.loadup.framework.api.binder.Binder;
import java.util.Optional;

/**
 * 配置中心底层驱动接口。
 *
 * <p>每个 binder 实现封装一种具体的配置中心 SDK（Local 文件系统 / Nacos / Apollo 等），
 * 通过统一接口屏蔽底层差异。
 *
 * @param <C> binder 级别配置（如 NacosConfigCenterBinderCfg）
 * @param <S> binding 级别配置（ConfigCenterBindingCfg 或其子类）
 */
public interface ConfigCenterBinder<C extends ConfigCenterBinderCfg, S extends ConfigCenterBindingCfg>
        extends Binder<C, S> {

    /**
     * 查询配置内容。
     *
     * @param dataId   配置项 ID
     * @param group    配置分组
     * @param settings binding 级别配置
     * @return 配置内容，不存在时返回 {@link Optional#empty()}
     */
    Optional<String> getConfig(String dataId, String group, S settings);

    /**
     * 发布（写入）配置内容到配置中心。
     *
     * @param dataId   配置项 ID
     * @param group    配置分组
     * @param content  配置内容
     * @param settings binding 级别配置
     * @return true 表示成功
     */
    boolean publishConfig(String dataId, String group, String content, S settings);

    /**
     * 从配置中心删除配置项。
     *
     * @param dataId   配置项 ID
     * @param group    配置分组
     * @param settings binding 级别配置
     * @return true 表示成功
     */
    boolean removeConfig(String dataId, String group, S settings);

    /**
     * 注册配置变更监听器。
     *
     * @param dataId   配置项 ID
     * @param group    配置分组
     * @param listener 变更回调
     * @param settings binding 级别配置
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
