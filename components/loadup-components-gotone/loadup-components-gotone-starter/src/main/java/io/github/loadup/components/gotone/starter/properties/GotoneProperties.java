package io.github.loadup.components.gotone.starter.properties;

/*-
 * #%L
 * loadup-components-gotone-starter
 * %%
 * Copyright (C) 2026 LoadUp Cloud
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

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Gotone Configuration Properties.
 */
@Data
@ConfigurationProperties(prefix = "loadup.gotone")
public class GotoneProperties {

    /**
     * Whether gotone is enabled.
     */
    private boolean enabled = true;

    /**
     * Whether async sending is enabled.
     */
    private boolean asyncEnabled = true;

    /**
     * Template cache TTL in seconds.
     */
    private int templateCacheTtl = 3600;
}
