package com.github.loadup.components.gateway.facade.config.model;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.io.Serializable;

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
    private String  limitTimeRule;
    /**
     * 限流隔离级别 单机\集群
     */
    private String  limitType;
    /**
     * 分布式限流降级策略 error\standalone\pass
     */
    private String  distributedFallbackStrategy;
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
    private String  shieldKeys;

    /**
     * Getter method for property <tt>limitValue</tt>.
     */
    public Integer getLimitValue() {
        return limitValue;
    }

    /**
     * Setter method for property <tt>limitValue</tt>.
     */
    public void setLimitValue(Integer limitValue) {
        this.limitValue = limitValue;
    }

    /**
     * Getter method for property <tt>limitTimeRule</tt>.
     */
    public String getLimitTimeRule() {
        return limitTimeRule;
    }

    /**
     * Setter method for property <tt>limitTimeRule</tt>.
     */
    public void setLimitTimeRule(String limitTimeRule) {
        this.limitTimeRule = limitTimeRule;
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
     * Getter method for property <tt>distributedFallbackStrategyLimitValue</tt>.
     */
    public Integer getDistributedFallbackStrategyLimitValue() {
        return distributedFallbackStrategyLimitValue;
    }

    /**
     * Setter method for property <tt>distributedFallbackStrategyLimitValue</tt>.
     */
    public void setDistributedFallbackStrategyLimitValue(Integer distributedFallbackStrategyLimitValue) {
        this.distributedFallbackStrategyLimitValue = distributedFallbackStrategyLimitValue;
    }

    /**
     * Getter method for property <tt>connectTimeout</tt>.
     */
    public Integer getConnectTimeout() {
        return connectTimeout;
    }

    /**
     * Setter method for property <tt>connectTimeout</tt>.
     */
    public void setConnectTimeout(Integer connectTimeout) {
        this.connectTimeout = connectTimeout;
    }

    /**
     * Getter method for property <tt>readTimeout</tt>.
     */
    public Integer getReadTimeout() {
        return readTimeout;
    }

    /**
     * Setter method for property <tt>readTimeout</tt>.
     */
    public void setReadTimeout(Integer readTimeout) {
        this.readTimeout = readTimeout;
    }

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
     * Getter method for property <tt>url</tt>.
     */
    public String getUrl() {
        return url;
    }

    /**
     * Setter method for property <tt>url</tt>.
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