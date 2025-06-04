/* Copyright (C) LoadUp Cloud 2022-2025 */
package com.github.loadup.components.gateway.facade.config.model;

/*-
 * #%L
 * loadup-components-gateway-facade
 * %%
 * Copyright (C) 2022 - 2025 loadup_cloud
 * %%
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 * #L%
 */

import java.io.Serializable;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

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
     *
     */
    public Boolean getOpenLimit() {
        return openLimit;
    }

    /**
     *
     */
    public void setOpenLimit(Boolean openLimit) {
        this.openLimit = openLimit;
    }

    /**
     *
     */
    public String getLimitType() {
        return limitType;
    }

    /**
     *
     */
    public void setLimitType(String limitType) {
        this.limitType = limitType;
    }

    /**
     *
     */
    public String getInterfaceLimit() {
        return interfaceLimit;
    }

    /**
     *
     */
    public void setInterfaceLimit(String interfaceLimit) {
        this.interfaceLimit = interfaceLimit;
    }

    /**
     *
     */
    public String getTenantLimit() {
        return tenantLimit;
    }

    /**
     *
     */
    public void setTenantLimit(String tenantLimit) {
        this.tenantLimit = tenantLimit;
    }

    /**
     *
     */
    public String getClientLimit() {
        return clientLimit;
    }

    /**
     *
     */
    public void setClientLimit(String clientLimit) {
        this.clientLimit = clientLimit;
    }

    /**
     *
     */
    public String getDistributedLimitTimeRule() {
        return distributedLimitTimeRule;
    }

    /**
     *
     */
    public void setDistributedLimitTimeRule(String distributedLimitTimeRule) {
        this.distributedLimitTimeRule = distributedLimitTimeRule;
    }

    /**
     *
     */
    public String getDistributedFallbackStrategy() {
        return distributedFallbackStrategy;
    }

    /**
     *
     */
    public void setDistributedFallbackStrategy(String distributedFallbackStrategy) {
        this.distributedFallbackStrategy = distributedFallbackStrategy;
    }

    /**
     *
     */
    public String getDistributedFallbackStandaloneInterfaceLimit() {
        return distributedFallbackStandaloneInterfaceLimit;
    }

    /**
     *
     */
    public void setDistributedFallbackStandaloneInterfaceLimit(String distributedFallbackStandaloneInterfaceLimit) {
        this.distributedFallbackStandaloneInterfaceLimit = distributedFallbackStandaloneInterfaceLimit;
    }

    /**
     *
     */
    public String getDistributedFallbackStandaloneClientLimit() {
        return distributedFallbackStandaloneClientLimit;
    }

    /**
     *
     */
    public void setDistributedFallbackStandaloneClientLimit(String distributedFallbackStandaloneClientLimit) {
        this.distributedFallbackStandaloneClientLimit = distributedFallbackStandaloneClientLimit;
    }

    /**
     *
     */
    public String getDistributedFallbackStandaloneTenantLimit() {
        return distributedFallbackStandaloneTenantLimit;
    }

    /**
     *
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
