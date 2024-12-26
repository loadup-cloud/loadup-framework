package com.github.loadup.components.gateway.facade.spi;

import com.github.loadup.components.gateway.facade.model.LimitConfig;

/**
 * <p>
 * LimitRuleService.java
 * </p>
 */
public interface LimitRuleService {

    /**
     * apply token by multi-condition
     */
    boolean applyToken(LimitConfig limitConfig);

    /**
     * reset token
     */
    void resetToken(LimitConfig limitConfig);

}