package io.github.loadup.components.configcenter.properties;

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

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Global properties for the config center.
 *
 * <pre>
 * loadup:
 *   configcenter:
 *     default-binder: local    # local | nacos | apollo
 * </pre>
 */
@ConfigurationProperties(prefix = "loadup.configcenter")
public class ConfigCenterGroupProperties {

    /**
     * Default binder type used when none is specified at the binding level.
     */
    private ConfigCenterBinderType defaultBinder = ConfigCenterBinderType.LOCAL;

    public ConfigCenterBinderType getDefaultBinder() {
        return this.defaultBinder;
    }

    public void setDefaultBinder(ConfigCenterBinderType defaultBinder) {
        this.defaultBinder = defaultBinder;
    }
}
