package com.github.loadup.components.gateway.core.model.communication;

/**
 *
 */
public enum ProtocolType {
    /**
     * HTTP
     */
    HTTP,

    /**
     * HTTPS
     */
    HTTPS,

    /**
     * TR
     */
    TR,

    /**
     * SPRINGBEAN
     */
    SPRINGBEAN,

    /**
     * PLUGINBEAN
     */
    PLUGINBEAN;

    /**
     * 获取枚举代码
     */
    public String getCode() {
        return this.name();
    }

    /**
     * 根据枚举代码，获取枚举
     */
    public static ProtocolType getEnumByCode(String code) {
        for (ProtocolType type : ProtocolType.values()) {
            if (type.getCode().equalsIgnoreCase(code)) {
                return type;
            }
        }

        //default HTTP
        return HTTP;
    }
}
