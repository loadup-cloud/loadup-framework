package io.github.loadup.commons.enums;

/*-
 * #%L
 * Loadup Common DTO
 * %%
 * Copyright (C) 2025 - 2026 loadup_cloud
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

/**
 * Supported database type enumeration.
 *
 * <p>Originally duplicated across {@code loadup-components-globalunique} and
 * {@code loadup-components-retrytask-facade}; consolidated here as the single source of truth.
 */
@Getter
public enum DbType {

    MYSQL("mysql", "MySQL"),
    POSTGRESQL("pgsql", "PostgreSQL"),
    ORACLE("oracle", "Oracle");

    private final String value;
    private final String displayName;

    DbType(String value, String displayName) {
        this.value = value;
        this.displayName = displayName;
    }

    /** Resolve by value (case-insensitive); defaults to {@link #MYSQL} when null or unknown. */
    public static DbType fromValue(String value) {
        if (value == null) {
            return MYSQL;
        }
        for (DbType type : DbType.values()) {
            if (type.value.equalsIgnoreCase(value)) {
                return type;
            }
        }
        return MYSQL;
    }
}
