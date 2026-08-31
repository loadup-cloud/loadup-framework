package io.github.loadup.components.configcenter.model;

/*-
 * #%L
 * LoadUp ConfigCenter Components API
 * %%
 * Copyright (C) 2025 - 2026 LoadUp Cloud
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import org.springframework.context.ApplicationEvent;

/**
 * Spring application event fired when a config item changes.
 *
 * <p>Published by {@code DefaultConfigCenterBinding} whenever the underlying binder
 * detects a change. Applications can listen to this event to trigger refresh logic
 * (e.g. combined with {@code @EnableConfigAutoRefresh}).
 */
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

    public String getDataId() {
        return this.dataId;
    }

    public String getGroup() {
        return this.group;
    }

    public String getNamespace() {
        return this.namespace;
    }

    public String getOldContent() {
        return this.oldContent;
    }

    public String getNewContent() {
        return this.newContent;
    }

    public ConfigChangeType getChangeType() {
        return this.changeType;
    }
}
