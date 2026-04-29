package io.github.loadup.components.configcenter.model;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * 配置变更 Spring 事件。
 *
 * <p>当配置中心检测到某个配置项发生变化时，由 {@code DefaultConfigCenterBinding} 发布此事件。
 * 应用可监听此事件并触发相应的刷新逻辑（如结合 {@code @EnableConfigAutoRefresh}）。
 */
@Getter
public class ConfigChangeEvent extends ApplicationEvent {

    private final String dataId;
    private final String group;
    private final String namespace;
    private final String oldContent;
    private final String newContent;
    private final ConfigChangeType changeType;

    public ConfigChangeEvent(
            Object source,
            String dataId,
            String group,
            String namespace,
            String oldContent,
            String newContent,
            ConfigChangeType changeType) {
        super(source);
        this.dataId = dataId;
        this.group = group;
        this.namespace = namespace;
        this.oldContent = oldContent;
        this.newContent = newContent;
        this.changeType = changeType;
    }
}
