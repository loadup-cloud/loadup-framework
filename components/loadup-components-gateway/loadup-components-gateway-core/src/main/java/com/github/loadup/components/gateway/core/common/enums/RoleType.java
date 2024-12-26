package com.github.loadup.components.gateway.core.common.enums;

/**
 *
 */
public enum RoleType {

    /**
     * sender
     */
    SENDER("SENDER", "sender"),

    /**
     * receiver
     */
    RECEIVER("RECEIVER", "receiver"),

    ;

    /**
     * 代码
     */
    private final String code;

    /**
     * 描述
     */
    private final String message;

    RoleType(String code, String message) {
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

    public static RoleType getEnumByCode(String code) {
        for (RoleType status : RoleType.values()) {
            if (status.getCode().equalsIgnoreCase(code)) {
                return status;
            }
        }
        return null;
    }
}