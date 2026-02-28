package io.github.loadup.modules.config.domain.enums;

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

/**
 * Value type of a config item.
 *
 * @author LoadUp Framework
 */
public enum ValueType {
    STRING,
    INTEGER,
    LONG,
    DOUBLE,
    BOOLEAN,
    JSON;

    public static ValueType of(String name) {
        for (ValueType t : values()) {
            if (t.name().equalsIgnoreCase(name)) {
                return t;
            }
        }
        throw new IllegalArgumentException("Unknown ValueType: " + name);
    }
}
