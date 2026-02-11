package io.github.loadup.retrytask.facade.enums;

/*-
 * #%L
 * Loadup Components Retrytask Facade
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
 * Database type enumeration.
 */
public enum DbType {
    MYSQL("mysql"),
    PGSQL("pgsql"),
    ORACLE("oracle");

    private final String value;

    DbType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static DbType fromValue(String value) {
        if (value == null) {
            return MYSQL;
        }
        for (DbType type : DbType.values()) {
            if (type.value.equalsIgnoreCase(value)) {
                return type;
            }
        }
        return MYSQL; // Default to MySQL
    }
}
