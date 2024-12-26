/**
 * Alipy.com Inc.
 * Copyright (c) 2004-2013 All Rights Reserved.
 */
package com.github.loadup.components.gateway.core.common.enums;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.*;

/**
 * <P>文件加工处理单元</P>
 */
public enum ProcessUnit {

    /**
     * COMPRESS
     */
    COMPRESS("COMPRESS", "压缩处理"),

    /**
     * DECOMPRESS
     */
    DECOMPRESS("DECOMPRESS", "解压处理"),

    /**
     * ENCRYPT
     */
    ENCRYPT("ENCRYPT", "加密处理"),

    /**
     * DECRYPT
     */
    DECRYPT("DECRYPT", "解密处理"),

    /**
     * SIGN
     */
    SIGN("SIGN", "加签处理"),

    /**
     * VERIFY
     */
    VERIFY("VERIFY", "验签处理"),

    /**
     * DIGEST
     */
    DIGEST("DIGEST", "摘要处理");

    /**
     * code
     */
    private String code;

    /**
     * desc
     */
    private String desc;

    /**
     * 配置分隔符
     */
    private static final String SEPARATOR = ",";

    /**
     * 构造器
     */
    private ProcessUnit(String code, String desc) {
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
    public static ProcessUnit getProcessUnitByCode(String code) {
        for (ProcessUnit unit : ProcessUnit.values()) {
            if (StringUtils.equalsIgnoreCase(unit.getCode(), code)) {
                return unit;
            }
        }
        return null;
    }

    /**
     * 通过codes获取枚举列表
     */
    public static List<ProcessUnit> getProcessUnitsByCodes(List<String> codes) {

        if (CollectionUtils.isEmpty(codes)) {
            return null;
        }

        List<ProcessUnit> result = new ArrayList<ProcessUnit>();

        for (String code : codes) {
            result.add(getProcessUnitByCode(code));
        }

        return result;
    }

    /**
     * 通过codes 字符串获取枚举列表
     */
    public static List<ProcessUnit> getProcessUnitsByCodesString(String codes) {

        if (StringUtils.isBlank(codes)) {
            return null;
        }

        return getProcessUnitsByCodes(Arrays.asList(codes.split(SEPARATOR)));
    }
}
