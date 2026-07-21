package io.github.loadup.commons.enums;

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
