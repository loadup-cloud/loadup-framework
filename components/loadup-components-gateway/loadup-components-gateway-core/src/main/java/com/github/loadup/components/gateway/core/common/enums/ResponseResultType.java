/**
 * Alipy.com Inc.
 * Copyright (c) 2004-2013 All Rights Reserved.
 */
package com.github.loadup.components.gateway.core.common.enums;

/**
 * <P>通知结果类型</P>
 */
public enum ResponseResultType {

    /**
     * ALL_SC
     */
    ALL_SC("1", "所有系统通知成功"),

    /**
     * PARTIAL_SC
     */
    PARTIAL_SC("2", "部分系统通知成功"),

    /**
     * ALL_FAIL
     */
    ALL_FAIL("0", "所有系统通知失败");

    /**
     * code
     */
    private String code;

    /**
     * desc
     */
    private String desc;

    /**
     * 构造器
     */
    private ResponseResultType(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    /**
     * getter
     */
    public String getCode() {
        return code;
    }

    /**
     * getter
     */
    public String getDesc() {
        return desc;
    }
}
