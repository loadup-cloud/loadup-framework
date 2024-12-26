package com.github.loadup.components.gateway.core.common.enums;

/**
 *
 */
public enum RepositoryType {

    /**
     * file
     */
    FILE("FILE", true, "file repository, config in gatewaylite local cache"),

    /**
     * database
     */
    DATABASE("DATABASE", true, "database repository, config in gatewaylite local cache"),

    /**
     * product center
     */
    PRODCENTER("PRODCENTER", false, "product center repository, config in prodcenter query client"),

    ;

    /**
     * 代码
     */
    private String code;

    /**
     * whether config in gatewaylite internal cache
     */
    private boolean configInInternalCache;

    /**
     * 描述
     */
    private String message;

    /**
     * 构造方法
     */
    private RepositoryType(String code, boolean configInInternalCache, String message) {
        this.code = code;
        this.configInInternalCache = configInInternalCache;
        this.message = message;
    }

    /**
     * Getter method for property <tt>code</tt>.
     */
    public String getCode() {
        return code;
    }

    /**
     * Getter method for property <tt>configInInternalCache</tt>.
     */
    public boolean isConfigInInternalCache() {
        return configInInternalCache;
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
    public static RepositoryType getEnumByCode(String code) {
        for (RepositoryType status : RepositoryType.values()) {
            if (status.getCode().equalsIgnoreCase(code)) {
                return status;
            }
        }
        return null;
    }

}