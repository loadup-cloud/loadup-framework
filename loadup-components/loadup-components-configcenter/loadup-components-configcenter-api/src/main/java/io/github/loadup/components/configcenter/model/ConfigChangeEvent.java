package io.github.loadup.components.configcenter.model;

/*-
 * #%L
 * LoadUp ConfigCenter Components API
 * %%
 * Copyright (C) 2025 - 2026 LoadUp Cloud
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

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
