package com.github.loadup.components.gateway.core.common.enums;

/**
 *
 */
public enum InterfaceStatus {

    /**
     * valid
     */
    VALID("VALID", "Y"),

    /**
     * invalid
     */
    INVALID("INVALID", "N"),

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
    private InterfaceStatus(String code, String message) {
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
    public static InterfaceStatus getEnumByCode(String code) {
        for (InterfaceStatus status : InterfaceStatus.values()) {
            if (status.getCode().equalsIgnoreCase(code)) {
                return status;
            }
        }
        return null;
    }
}