package io.github.loadup.components.globalunique.enums;

import lombok.Getter;

/**
 * 数据库类型枚举
 *
 * @author loadup
 */
@Getter
public enum DbType {

    /**
     * MySQL
     */
    MYSQL("MySQL"),

    /**
     * PostgreSQL
     */
    POSTGRESQL("PostgreSQL"),

    /**
     * Oracle
     */
    ORACLE("Oracle");

    private final String displayName;

    DbType(String displayName) {
        this.displayName = displayName;
    }
}

