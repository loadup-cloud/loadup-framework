package com.github.loadup.components.gateway.core.common.enums;

/**
 *
 */
public enum AppInterfaceProperty implements PropertyName {

    /**
     * 应用和接口关系是否有效
     */
    VALID("VALID", "the relationship of app and interface is valid", "TRUE"),

    /**
     * 最大限流数
     */
    LIMIT_CONN("LIMIT_CONN", "max connections", ""),

    /**
     * 限流规则ID
     */
    LIMIT_ID("LIMIT_ID", "limit id", null);

    private final String name;

    private final String description;

    private final String defaultValue;

    private AppInterfaceProperty(String name, String description, String defaultValue) {
        this.name = name;
        this.description = description;
        this.defaultValue = defaultValue;
    }

    @Override
    public String getDefaultValue() {
        return defaultValue;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public String getName() {
        return name;
    }

}
