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

import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Wrapper that holds a single configuration entry retrieved from the config center.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConfigEntry {

    /**
     * Config item ID (key).
     */
    private String dataId;

    /**
     * Config group.
     */
    private String group;

    /**
     * Namespace / tenant.
     */
    private String namespace;

    /**
     * Config content (plain text).
     */
    private String content;

    /**
     * Content type, e.g. yaml / properties / json / text.
     */
    private String contentType;

    /**
     * Version returned by the config center; null if not supported.
     */
    private String version;

    /**
     * Last-modified timestamp returned by the config center; null if not supported.
     */
    private Instant lastModified;
}
