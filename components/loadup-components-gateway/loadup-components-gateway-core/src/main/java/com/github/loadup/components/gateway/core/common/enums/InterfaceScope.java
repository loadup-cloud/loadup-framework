package com.github.loadup.components.gateway.core.common.enums;

/**
 *
 */
public enum InterfaceScope {

    /**
     * INBOUND
     */
    INBOUND("INBOUND", "INBOUND"),

    /**
     * OUTBOUND
     */
    OUTBOUND("OUTBOUND", "OUTBOUND"),

    ;

    /**
     * 代码
     */
    private String code;

    /**
     * 描述
     */
    private String message;

    /**
     * 构造方法
     */
    private InterfaceScope(String code, String message) {
        this.code = code;
        this.message = message;
    }

    /**
     * Getter method for property <tt>code</tt>.
     */
    public String getCode() {
        return code;
    }

    /**
     * Getter method for property <tt>message</tt>.
     */
    public String getMessage() {
        return message;
    }

    /**
     * 根据代码获取枚举
     */
    public static InterfaceScope getEnumByCode(String code) {
        for (InterfaceScope status : InterfaceScope.values()) {
            if (status.getCode().equalsIgnoreCase(code)) {
                return status;
            }
        }
        return null;
    }
}