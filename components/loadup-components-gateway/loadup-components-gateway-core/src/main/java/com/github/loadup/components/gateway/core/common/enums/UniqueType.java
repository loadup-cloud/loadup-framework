/**
 * Alipy.com Inc.
 * Copyright (c) 2004-2013 All Rights Reserved.
 */
package com.github.loadup.components.gateway.core.common.enums;

import org.apache.commons.lang3.StringUtils;

/**
 * <P>幂等类型枚举类</P>
 */
public enum UniqueType {

    /**
     * SERIAL_UNIQUE
     */
    SERIAL_UNIQUE("SERIAL_UNIQUE", "流水号幂等"),

    /**
     * FILE_SRC_PATH_UNIQUE
     */
    FILE_SRC_PATH_UNIQUE("FILE_SRC_PATH_UNIQUE", "源文件路径幂等"),

    /**
     * FILE_DEST_PATH_UNIQUE
     */
    FILE_DEST_PATH_UNIQUE("FILE_DEST_PATH_UNIQUE", "目标文件路径幂等"),

    ;

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
    private UniqueType(String code, String desc) {
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

    /**
     * 通过code获取枚举类
     */
    public static UniqueType getByCode(String code) {

        for (UniqueType type : UniqueType.values()) {
            if (StringUtils.equalsIgnoreCase(type.getCode(), code)) {
                return type;
            }
        }
        return null;
    }
}
