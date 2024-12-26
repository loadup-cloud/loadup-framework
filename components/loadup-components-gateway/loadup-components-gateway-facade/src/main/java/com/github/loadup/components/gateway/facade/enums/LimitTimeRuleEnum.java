package com.github.loadup.components.gateway.facade.enums;

import org.apache.commons.lang3.StringUtils;

/**
 *
 */
public enum LimitTimeRuleEnum {
    /**
     * QPS level
     */
    QPS("QPS"),

    /**
     * QPM level
     */
    QPM("QPM"),

    /**
     *
     */
    CONCURRENCY("CONCURRENCY");

    public String getRuleType() {
        return ruleType;
    }

    public void setRuleType(String ruleType) {
        this.ruleType = ruleType;
    }

    private String ruleType;

    private LimitTimeRuleEnum(String rule) {
        this.ruleType = rule;
    }

    public static LimitTimeRuleEnum getTimeRuleByString(String rule) {
        for (LimitTimeRuleEnum limitTimeRuleEnum : LimitTimeRuleEnum.values()) {
            if (StringUtils.equals(limitTimeRuleEnum.getRuleType(), rule)) {
                return limitTimeRuleEnum;
            }
        }
        return null;
    }
}
