package com.github.loadup.components.gateway.core.common.enums;

/**
 *
 */
public enum InterfaceType {

    /**
     * openapi
     */
    OPENAPI("OPENAPI", "openapi"),

    /**
     * spi
     */
    SPI("SPI", "spi"),

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
    private InterfaceType(String code, String message) {
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
    public static InterfaceType getEnumByCode(String code) {
        for (InterfaceType status : InterfaceType.values()) {
            if (status.getCode().equalsIgnoreCase(code)) {
                return status;
            }
        }
        return null;
    }
}