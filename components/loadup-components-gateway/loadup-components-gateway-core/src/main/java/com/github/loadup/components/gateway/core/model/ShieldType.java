package com.github.loadup.components.gateway.core.model;

import org.apache.commons.lang3.StringUtils;

/**
 * 敏感数据隐藏类型
 */
public enum ShieldType {

    //private data，全部隐藏
    ALL;

    /**
     * 根据编码查询枚举。
     */
    public static ShieldType getByCode(String code) {
        for (ShieldType value : ShieldType.values()) {
            if (StringUtils.equals(code, value.name())) {
                return value;
            }
        }
        return null;
    }

}