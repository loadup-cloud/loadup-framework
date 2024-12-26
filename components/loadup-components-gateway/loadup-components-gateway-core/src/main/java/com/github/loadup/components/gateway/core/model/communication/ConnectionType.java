package com.github.loadup.components.gateway.core.model.communication;

/**
 * 通讯组件的服务方式：服务端或客户端
 */
public enum ConnectionType {

    /**
     * 服务端
     */
    SERVER,

    /**
     * 客户端
     */
    CLIENT;

    /**
     * 获取枚举代码
     */
    public String getCode() {
        return this.name();
    }

    /**
     * 根据枚举代码，获取枚举
     */
    public static ConnectionType getEnumByCode(String code) {
        for (ConnectionType type : ConnectionType.values()) {
            if (type.getCode().equalsIgnoreCase(code)) {
                return type;
            }
        }

        return CLIENT;
    }
}
