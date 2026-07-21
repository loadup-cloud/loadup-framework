package io.github.loadup.components.configcenter.local.cfg;

/*-
 * #%L
 * LoadUp ConfigCenter Binder Local
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

import io.github.loadup.components.configcenter.cfg.ConfigCenterBinderCfg;
import java.time.Duration;
import lombok.Getter;
import lombok.Setter;

/**
 * Configuration for the local-file binder.
 *
 * <pre>
 * loadup:
 *   configcenter:
 *     binders:
 *       local:
 *         base-path: config/        # root directory for local config files (relative or absolute)
 *         refresh-interval: 30s     # polling interval for file-change detection
 * </pre>
 */
@Getter
@Setter
public class LocalConfigCenterBinderCfg extends ConfigCenterBinderCfg {

    /**
     * Root directory for local config files.
     * File path rule: {basePath}/{group}/{dataId}
     * Falls back to Spring {@code Environment.getProperty(dataId)} when the file does not exist.
     */
    private String basePath = "config";

    /**
     * Polling interval for detecting file changes
     * (used to trigger {@link io.github.loadup.components.configcenter.model.ConfigChangeListener}).
     */
    private Duration refreshInterval = Duration.ofSeconds(30);

    @Override
    public Object getIdentity() {
        return basePath;
    }
}
