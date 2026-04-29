package io.github.loadup.components.configcenter.model;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * Spring application event fired when a config item changes.
 *
 * <p>Published by {@code DefaultConfigCenterBinding} whenever the underlying binder
 * detects a change. Applications can listen to this event to trigger refresh logic
 * (e.g. combined with {@code @EnableConfigAutoRefresh}).
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
