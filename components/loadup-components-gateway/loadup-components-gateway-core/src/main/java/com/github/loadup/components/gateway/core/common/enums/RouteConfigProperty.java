package com.github.loadup.components.gateway.core.common.enums;

/**
 * 分流配置属性
 */
public enum RouteConfigProperty implements PropertyName {

    /**
     * 自动剔除阈值
     */
    ROUTE_KICK_THRESHOLD("ROUTE_KICK_THRESHOLD", "kicked threshold", "3"),

    /**
     * 是否需要自动加回
     */
    ROUTE_AUTO_REVERT("ROUTE_AUTO_REVERT", "auto revert", "false");

    private final String name;

    private final String description;

    private final String defaultValue;

    private RouteConfigProperty(String name, String description, String defaultValue) {
        this.name = name;
        this.description = description;
        this.defaultValue = defaultValue;
    }

    /**
     *
     */
    @Override
    public String getDefaultValue() {
        return defaultValue;
    }

    /**
     *
     */
    @Override
    public String getDescription() {
        return description;
    }

    /**
     *
     */
    @Override
    public String getName() {
        return name;
    }
}
