package io.github.loadup.modules.config.domain.event;

/*-
 * #%L
 * Loadup Modules Config Domain
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

import io.github.loadup.modules.config.domain.enums.ChangeType;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * Domain event published whenever a config item is created, updated, or deleted.
 *
 * <p>Listeners (e.g. {@code ConfigLocalCache}) subscribe via Spring's
 * {@code @EventListener} to react to these changes.
 */
@Getter
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
}
