package com.github.loadup.components.gateway.core.common.enums;

/**
 * enum of interface router type
 */
public enum InterfaceRouterType {

    /**
     * route by weight
     */
    WEIGHT,

    /**
     * route by param
     */
    PARAM,
    ;

    /**
     * get name
     */
    public String getCode() {
        return this.name();
    }

    /**
     * 根据枚举代码，获取枚举
     */
    public static InterfaceRouterType getEnumByCode(String code) {
        for (InterfaceRouterType type : InterfaceRouterType.values()) {
            if (type.getCode().equalsIgnoreCase(code)) {
                return type;
            }
        }

        return null;
    }
}