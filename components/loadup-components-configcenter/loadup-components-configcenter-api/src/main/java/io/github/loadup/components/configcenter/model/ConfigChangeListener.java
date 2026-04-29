package io.github.loadup.components.configcenter.model;

/**
 * 配置变更监听器。
 *
 * <p>调用方实现此接口并通过 {@code ConfigCenterBinding#addListener} 注册，
 * 配置中心在检测到变更时回调 {@link #onChange(ConfigChangeEvent)}。
 */
@FunctionalInterface
public interface ConfigChangeListener {

    /**
     * 配置变更回调。
     *
     * @param event 变更事件，包含 dataId / group / 新旧内容 / 变更类型
     */
    void onChange(ConfigChangeEvent event);
}
