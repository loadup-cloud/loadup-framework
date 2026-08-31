package io.github.loadup.commons.enums;

/*-
 * #%L
 * Loadup Common DTO
 * %%
 * Copyright (C) 2025 - 2026 loadup_cloud
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

/**
 * Supported database type enumeration.
 *
 * <p>Originally duplicated across {@code loadup-components-globalunique} and
 * {@code loadup-components-retrytask-facade}; consolidated here as the single source of truth.
 */
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

    public String getValue() {
        return this.value;
    }

    public String getDisplayName() {
        return this.displayName;
    }
}
