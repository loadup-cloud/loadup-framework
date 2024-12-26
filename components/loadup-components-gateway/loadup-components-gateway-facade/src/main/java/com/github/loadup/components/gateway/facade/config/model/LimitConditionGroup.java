package com.github.loadup.components.gateway.facade.config.model;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.io.Serializable;

/**
 * 网关限流产品基础配置条件组
 */

public class LimitConditionGroup implements Serializable {

    private static final long serialVersionUID = -3736482110642580114L;

    /**
     * open gatewaylite intern limit capability
     */
    private Boolean openLimit = Boolean.FALSE;

    /**
     * limit type
     */
    private String limitType;

    /**
     * interfaceLimit number
     */
    private String interfaceLimit;

    /**
     * tenantLimit number
     */
    private String tenantLimit;

    /**
     * clientLimit number
     */
    private String clientLimit;

    /**
     * time rule for distributed limit, QPS or QPM
     */
    private String distributedLimitTimeRule;

    /**
     * fallback strategy for distributed limit error, standalone or pass or block
     */
    private String distributedFallbackStrategy;

    /**
     * interfaceLimit number for fallbacking to standalone limit when distributed limit error
     */
    private String distributedFallbackStandaloneInterfaceLimit;

    /**
     * clientLimit number for fallbacking to standalone limit when distributed limit error
     */
    private String distributedFallbackStandaloneClientLimit;

    /**
     * tenantLimit number for fallbacking to standalone limit when distributed limit error
     */
    private String distributedFallbackStandaloneTenantLimit;

    /**
     * Getter method for property <tt>openLimit</tt>.
     */
    public Boolean getOpenLimit() {
        return openLimit;
    }

    /**
     * Setter method for property <tt>openLimit</tt>.
     */
    public void setOpenLimit(Boolean openLimit) {
        this.openLimit = openLimit;
    }

    /**
     * Getter method for property <tt>limitType</tt>.
     */
    public String getLimitType() {
        return limitType;
    }

    /**
     * Setter method for property <tt>limitType</tt>.
     */
    public void setLimitType(String limitType) {
        this.limitType = limitType;
    }

    /**
     * Getter method for property <tt>interfaceLimit</tt>.
     */
    public String getInterfaceLimit() {
        return interfaceLimit;
    }

    /**
     * Setter method for property <tt>interfaceLimit</tt>.
     */
    public void setInterfaceLimit(String interfaceLimit) {
        this.interfaceLimit = interfaceLimit;
    }

    /**
     * Getter method for property <tt>tenantLimit</tt>.
     */
    public String getTenantLimit() {
        return tenantLimit;
    }

    /**
     * Setter method for property <tt>tenantLimit</tt>.
     */
    public void setTenantLimit(String tenantLimit) {
        this.tenantLimit = tenantLimit;
    }

    /**
     * Getter method for property <tt>clientLimit</tt>.
     */
    public String getClientLimit() {
        return clientLimit;
    }

    /**
     * Setter method for property <tt>clientLimit</tt>.
     */
    public void setClientLimit(String clientLimit) {
        this.clientLimit = clientLimit;
    }

    /**
     * Getter method for property <tt>distributedLimitTimeRule</tt>.
     */
    public String getDistributedLimitTimeRule() {
        return distributedLimitTimeRule;
    }

    /**
     * Setter method for property <tt>distributedLimitTimeRule</tt>.
     */
    public void setDistributedLimitTimeRule(String distributedLimitTimeRule) {
        this.distributedLimitTimeRule = distributedLimitTimeRule;
    }

    /**
     * Getter method for property <tt>distributedFallbackStrategy</tt>.
     */
    public String getDistributedFallbackStrategy() {
        return distributedFallbackStrategy;
    }

    /**
     * Setter method for property <tt>distributedFallbackStrategy</tt>.
     */
    public void setDistributedFallbackStrategy(String distributedFallbackStrategy) {
        this.distributedFallbackStrategy = distributedFallbackStrategy;
    }

    /**
     * Getter method for property <tt>distributedFallbackStandaloneInterfaceLimit</tt>.
     */
    public String getDistributedFallbackStandaloneInterfaceLimit() {
        return distributedFallbackStandaloneInterfaceLimit;
    }

    /**
     * Setter method for property <tt>distributedFallbackStandaloneInterfaceLimit</tt>.
     */
    public void setDistributedFallbackStandaloneInterfaceLimit(String distributedFallbackStandaloneInterfaceLimit) {
        this.distributedFallbackStandaloneInterfaceLimit = distributedFallbackStandaloneInterfaceLimit;
    }

    /**
     * Getter method for property <tt>distributedFallbackStandaloneClientLimit</tt>.
     */
    public String getDistributedFallbackStandaloneClientLimit() {
        return distributedFallbackStandaloneClientLimit;
    }

    /**
     * Setter method for property <tt>distributedFallbackStandaloneClientLimit</tt>.
     */
    public void setDistributedFallbackStandaloneClientLimit(String distributedFallbackStandaloneClientLimit) {
        this.distributedFallbackStandaloneClientLimit = distributedFallbackStandaloneClientLimit;
    }

    /**
     * Getter method for property <tt>distributedFallbackStandaloneTenantLimit</tt>.
     */
    public String getDistributedFallbackStandaloneTenantLimit() {
        return distributedFallbackStandaloneTenantLimit;
    }

    /**
     * Setter method for property <tt>distributedFallbackStandaloneTenantLimit</tt>.
     */
    public void setDistributedFallbackStandaloneTenantLimit(String distributedFallbackStandaloneTenantLimit) {
        this.distributedFallbackStandaloneTenantLimit = distributedFallbackStandaloneTenantLimit;
    }

    /**
     * @see Object#toString()
     */
    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}