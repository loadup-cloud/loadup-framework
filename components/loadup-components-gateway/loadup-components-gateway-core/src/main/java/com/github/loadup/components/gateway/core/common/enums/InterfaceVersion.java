package com.github.loadup.components.gateway.core.common.enums;

/**
 *
 */
public enum InterfaceVersion {

    /**
     * V1
     */
    V1("V1", "V2", "v1"),

    /**
     * V2
     */
    V2("V2", "V3", "v2"),

    /**
     * V3
     */
    V3("V3", "V4", "v2"),

    /**
     * V4
     */
    V4("V4", null, "v2"),

    ;

    /**
     * 代码
     */
    private String code;

    /**
     * next version
     */
    private String nextVersion;

    /**
     * 描述
     */
    private String message;

    /**
     * 构造方法
     */
    private InterfaceVersion(String code, String nextVersion, String message) {
        this.code = code;
        this.nextVersion = nextVersion;
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
     * Getter method for property <tt>nextVersion</tt>.
     */
    public String getNextVersion() {
        return nextVersion;
    }

    /**
     * 根据代码获取枚举
     */
    public static InterfaceVersion getEnumByCode(String code) {
        for (InterfaceVersion status : InterfaceVersion.values()) {
            if (status.getCode().equalsIgnoreCase(code)) {
                return status;
            }
        }
        return null;
    }
}