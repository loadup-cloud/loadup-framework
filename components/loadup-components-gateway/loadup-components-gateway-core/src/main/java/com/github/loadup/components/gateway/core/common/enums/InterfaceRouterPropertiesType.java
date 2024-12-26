package com.github.loadup.components.gateway.core.common.enums;

/**
 * support type of interface properties
 */
public enum InterfaceRouterPropertiesType {

    /**
     * contain
     */
    IN("==", "in"),

    /**
     * not contain
     */
    NOTIN("!=", "not in"),
    ;

    /**
     * code
     */
    private String code;

    /**
     * 描述
     */
    private String desc;

    /**
     * 构造器
     */
    InterfaceRouterPropertiesType(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    /**
     * get name
     */
    public String getCode() {
        return this.code;
    }

    /**
     * 根据枚举代码，获取枚举
     */
    public static InterfaceRouterPropertiesType getEnumByCode(String code) {
        for (InterfaceRouterPropertiesType type : InterfaceRouterPropertiesType.values()) {
            if (type.getCode().equalsIgnoreCase(code)) {
                return type;
            }
        }
        return null;
    }
}