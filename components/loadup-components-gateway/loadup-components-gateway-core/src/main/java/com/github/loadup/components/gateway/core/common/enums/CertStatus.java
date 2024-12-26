package com.github.loadup.components.gateway.core.common.enums;

/**
 *
 */
public enum CertStatus {

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
     * code
     */
    private String code;

    /**
     * desc
     */
    private String message;

    /**
     * constructor
     */
    private CertStatus(String code, String message) {
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
     * get by code
     */
    public static CertStatus getEnumByCode(String code) {
        for (CertStatus status : CertStatus.values()) {
            if (status.getCode().equalsIgnoreCase(code)) {
                return status;
            }
        }
        return null;
    }
}