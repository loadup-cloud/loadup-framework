package io.github.loadup.modules.config.domain.event;

/*-
 * #%L
 * Loadup Modules Config Domain
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

import io.github.loadup.modules.config.domain.enums.ChangeType;
import org.springframework.context.ApplicationEvent;

/**
 * Domain event published whenever a config item is created, updated, or deleted.
 *
 * <p>Listeners (e.g. {@code ConfigLocalCache}) subscribe via Spring's
 * {@code @EventListener} to react to these changes.
 */
public class ConfigChangedEvent extends ApplicationEvent {

    private final String configKey;
    private final String newValue;
    private final ChangeType changeType;
    private final String operator;

    public ConfigChangedEvent(
            Object source, String configKey, String newValue, ChangeType changeType, String operator) {
        super(source);
        this.configKey = configKey;
        this.newValue = newValue;
        this.changeType = changeType;
        this.operator = operator;
    }

    public String getConfigKey() {
        return this.configKey;
    }

    public String getNewValue() {
        return this.newValue;
    }

    public ChangeType getChangeType() {
        return this.changeType;
    }

    public String getOperator() {
        return this.operator;
    }
}
