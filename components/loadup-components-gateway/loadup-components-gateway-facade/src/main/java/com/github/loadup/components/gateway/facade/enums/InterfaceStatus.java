package com.github.loadup.components.gateway.facade.enums;

/**
 *
 */
public enum InterfaceStatus {

    /**
     * valid status
     */
    VALID("VALID", "online, valid status"),

    /**
     * invalid status
     */
    INVALID("INVALID", "online, valid status"),

    ;

    /**
     * code
     */
    private String code;

    /**
     * description
     */
    private String desc;

    /**
     * get by name
     */
    public static InterfaceStatus getByName(String InterfaceStatus) {
        for (com.github.loadup.components.gateway.facade.enums.InterfaceStatus type : values()) {
            if (type.getCode().equals(InterfaceStatus)) {
                return type;
            }
        }
        return null;
    }

    /**
     * 构造函数
     */
    InterfaceStatus(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    /**
     * Getter method for property <tt>desc</tt>.
     */
    public String getDesc() {
        return desc;
    }

    /**
     * Setter method for property <tt>desc</tt>.
     */
    public void setDesc(String desc) {
        this.desc = desc;
    }

    /**
     * Getter method for property <tt>code</tt>.
     */
    public String getCode() {
        return code;
    }

    /**
     * Setter method for property <tt>code</tt>.
     */
    public void setCode(String code) {
        this.code = code;
    }
}