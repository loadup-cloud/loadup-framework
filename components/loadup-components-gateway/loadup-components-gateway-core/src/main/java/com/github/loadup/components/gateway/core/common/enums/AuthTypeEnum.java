package com.github.loadup.components.gateway.core.common.enums;

import org.apache.commons.lang3.StringUtils;

/**
 * 鉴权类型
 */
public enum AuthTypeEnum {

    /**
     * 基本鉴权
     */
    BASE("BASE", "use the property of app and interface relationship to auth"),

    /**
     * oauth client
     */
    OAUTH_CLIENT("OAUTH_CLIENT", "use oauth client to auth"),

    /**
     * oauth token
     */
    OAUTH_TOKEN("OAUTH_TOKEN", "use oauth token to auth");

    /**
     * 类型码
     */
    private String code;

    /**
     * 类型值
     */
    private String desc;

    /**
     *
     */
    private AuthTypeEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    /**
     * 根据code获取AuthTypeEnum
     */
    public static AuthTypeEnum getByCode(String code) {
        AuthTypeEnum[] values = AuthTypeEnum.values();
        for (AuthTypeEnum authTypeEnum : values) {
            if (StringUtils.equals(authTypeEnum.getCode(), code)) {
                return authTypeEnum;
            }
        }
        return null;
    }

    /**
     * Getter method for property <tt>code</tt>.
     */
    public String getCode() {
        return code;
    }

    /**
     * Getter method for property <tt>desc</tt>.
     */
    public String getDesc() {
        return desc;
    }
}
