package com.github.loadup.components.gateway.facade.model;

import com.github.loadup.components.gateway.facade.enums.*;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 *
 */
public class LimitConfig {

    /**
     * 限流id
     */
    private String entryKeyId;

    /**
     * 限流值
     */
    private Integer                      limitValue;
    /**
     * 限流策略
     */
    private LimitTimeRuleEnum            limitTimeRule;
    /**
     * 限流隔离级别 单机\集群
     */
    private LimitTypeEnum                limitType;
    /**
     * 分布式限流降级策略
     */
    private DistributedExceptionStrategy distributedFallbackStrategy;
    /**
     * 降级后的限流大小
     */
    private Integer                      distributedFallbackStrategyLimitValue;
    /**
     * 是否开启限流
     */
    private Boolean                      enableLimit = Boolean.FALSE;

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
    public LimitTimeRuleEnum getLimitTimeRule() {
        return limitTimeRule;
    }

    /**
     * Setter method for property <tt>limitTimeRule</tt>.
     */
    public void setLimitTimeRule(LimitTimeRuleEnum limitTimeRule) {
        this.limitTimeRule = limitTimeRule;
    }

    /**
     * Getter method for property <tt>limitType</tt>.
     */
    public LimitTypeEnum getLimitType() {
        return limitType;
    }

    /**
     * Setter method for property <tt>limitType</tt>.
     */
    public void setLimitType(LimitTypeEnum limitType) {
        this.limitType = limitType;
    }

    /**
     * Getter method for property <tt>distributedFallbackStrategy</tt>.
     */
    public DistributedExceptionStrategy getDistributedFallbackStrategy() {
        return distributedFallbackStrategy;
    }

    /**
     * Setter method for property <tt>distributedFallbackStrategy</tt>.
     */
    public void setDistributedFallbackStrategy(DistributedExceptionStrategy distributedFallbackStrategy) {
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
     * Getter method for property <tt>entryKeyId</tt>.
     */
    public String getEntryKeyId() {
        return entryKeyId;
    }

    /**
     * Setter method for property <tt>entryKeyId</tt>.
     */
    public void setEntryKeyId(String entryKeyId) {
        this.entryKeyId = entryKeyId;
    }

    /**
     * Getter method for property <tt>enableLimit</tt>.
     */
    public Boolean isEnableLimit() {
        return enableLimit;
    }

    /**
     * Setter method for property <tt>enableLimit</tt>.
     */
    public void setEnableLimit(Boolean enableLimit) {
        this.enableLimit = enableLimit;
    }

    /**
     * @see Object#toString()
     */
    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}