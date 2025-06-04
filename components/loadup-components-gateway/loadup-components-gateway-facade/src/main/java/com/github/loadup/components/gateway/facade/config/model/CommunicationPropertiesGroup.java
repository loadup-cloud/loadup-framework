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
 *
 */
public class CommunicationPropertiesGroup implements Serializable {

    private static final long serialVersionUID = -4878291431746947060L;

    /**
     * 接口的id
     * 在配置中心中，OPENAPI为uri，SPI为url
     */
    private String url;

    /**
     * 限流值
     */
    private Integer limitValue;
    /**
     * 限流策略 QPS\QPM\CONCURRENCY
     */
    private String limitTimeRule;
    /**
     * 限流隔离级别 单机\集群
     */
    private String limitType;
    /**
     * 分布式限流降级策略 error\standalone\pass
     */
    private String distributedFallbackStrategy;
    /**
     * 降级后的限流大小
     */
    private Integer distributedFallbackStrategyLimitValue;

    /**
     * 连接超时时长
     */
    private Integer connectTimeout;

    /**
     * 读取超时时长
     */
    private Integer readTimeout;
    /**
     * 需要过滤的敏感字段 逗号分隔
     */
    private String shieldKeys;

    /**
     *
     */
    public Integer getLimitValue() {
        return limitValue;
    }

    /**
     *
     */
    public void setLimitValue(Integer limitValue) {
        this.limitValue = limitValue;
    }

    /**
     *
     */
    public String getLimitTimeRule() {
        return limitTimeRule;
    }

    /**
     *
     */
    public void setLimitTimeRule(String limitTimeRule) {
        this.limitTimeRule = limitTimeRule;
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
    public Integer getDistributedFallbackStrategyLimitValue() {
        return distributedFallbackStrategyLimitValue;
    }

    /**
     *
     */
    public void setDistributedFallbackStrategyLimitValue(Integer distributedFallbackStrategyLimitValue) {
        this.distributedFallbackStrategyLimitValue = distributedFallbackStrategyLimitValue;
    }

    /**
     *
     */
    public Integer getConnectTimeout() {
        return connectTimeout;
    }

    /**
     *
     */
    public void setConnectTimeout(Integer connectTimeout) {
        this.connectTimeout = connectTimeout;
    }

    /**
     *
     */
    public Integer getReadTimeout() {
        return readTimeout;
    }

    /**
     *
     */
    public void setReadTimeout(Integer readTimeout) {
        this.readTimeout = readTimeout;
    }

    /**
     *
     */
    public String getShieldKeys() {
        return shieldKeys;
    }

    /**
     *
     */
    public void setShieldKeys(String shieldKeys) {
        this.shieldKeys = shieldKeys;
    }

    /**
     *
     */
    public String getUrl() {
        return url;
    }

    /**
     *
     */
    public void setUrl(String url) {
        this.url = url;
    }

    /**
     * @see Object#toString()
     */
    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}
