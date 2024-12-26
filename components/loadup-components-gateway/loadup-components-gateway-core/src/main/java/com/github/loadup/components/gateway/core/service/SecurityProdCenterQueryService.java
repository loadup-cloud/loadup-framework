package com.github.loadup.components.gateway.core.service;

import com.github.loadup.components.gateway.facade.config.model.SecurityConditionGroup;

/**
 *
 */
public interface SecurityProdCenterQueryService {

    /**
     * query security condition group config
     */
    public SecurityConditionGroup querySecurityConditionGroup(String securityStrategyCode,
                                                              String operateType, String algorithm,
                                                              String clientId);

}