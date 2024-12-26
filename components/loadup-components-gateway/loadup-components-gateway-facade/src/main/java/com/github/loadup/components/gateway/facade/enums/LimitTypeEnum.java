package com.github.loadup.components.gateway.facade.enums;

import org.apache.commons.lang3.StringUtils;

/**
 *
 */
public enum LimitTypeEnum {
    /**
     * Standalone mode limit service
     */
    STANDALONE("standalone"),

    /**
     * Distributed mode limit service
     */
    DISTRIBUTED("distributed");

    public String getType() {
        return type;
    }

    private String type;

    private LimitTypeEnum(String type) {
        this.type = type;
    }

    public static LimitTypeEnum getLimitType(String s) {
        for (LimitTypeEnum type : LimitTypeEnum.values()) {
            if (StringUtils.equals(type.getType(), s)) {
                return type;
            }
        }
        return null;
    }
}
