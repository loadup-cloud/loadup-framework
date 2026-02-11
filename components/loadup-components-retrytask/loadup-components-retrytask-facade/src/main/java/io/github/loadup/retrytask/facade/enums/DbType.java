package io.github.loadup.retrytask.facade.enums;

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

