package com.github.loadup.components.gateway.facade.enums;

import org.apache.commons.lang3.StringUtils;

/**
 *
 */
public enum DistributedExceptionStrategy {

    /**
     * Fallback to standalone limit service
     */
    STANDALONE("STANDALONE", "standalone strategy"),

    /**
     * Block this request
     */
    BLOCK("BLOCK", "block strategy"),

    /**
     * Let it pass
     */
    PASS("PASS", "pass strategy");

    private String code;
    private String desc;

    DistributedExceptionStrategy(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public String getCode() {
        return code;
    }

    public static DistributedExceptionStrategy getByCode(String n) {
        for (DistributedExceptionStrategy des : DistributedExceptionStrategy.values()) {
            if (StringUtils.equalsIgnoreCase(n, des.getCode())) {
                return des;
            }
        }
        return null;
    }
}
