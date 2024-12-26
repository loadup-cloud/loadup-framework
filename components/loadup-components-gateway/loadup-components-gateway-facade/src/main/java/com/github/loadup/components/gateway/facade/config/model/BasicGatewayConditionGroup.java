package com.github.loadup.components.gateway.facade.config.model;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.io.Serializable;

/**
 * 网关通用产品基础配置条件组
 */

public class BasicGatewayConditionGroup implements Serializable {

    private static final long serialVersionUID = 4099359934469430937L;

    /**
     * keys use to be shielded
     */
    private String shieldKeys = "mobile,bankcard,id";

    /**
     * default result struct in gatewaylite response if result not exist
     */
    private Boolean needFillDefaultACResult = Boolean.TRUE;

    /**
     * Getter method for property <tt>shieldKeys</tt>.
     */
    public String getShieldKeys() {
        return shieldKeys;
    }

    /**
     * Setter method for property <tt>shieldKeys</tt>.
     */
    public void setShieldKeys(String shieldKeys) {
        this.shieldKeys = shieldKeys;
    }

    /**
     * Getter method for property <tt>needFillDefaultACResult</tt>.
     */
    public Boolean getNeedFillDefaultACResult() {
        return needFillDefaultACResult;
    }

    /**
     * Setter method for property <tt>needFillDefaultACResult</tt>.
     */
    public void setNeedFillDefaultACResult(Boolean needFillDefaultACResult) {
        this.needFillDefaultACResult = needFillDefaultACResult;
    }

    /**
     * @see Object#toString()
     */
    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}