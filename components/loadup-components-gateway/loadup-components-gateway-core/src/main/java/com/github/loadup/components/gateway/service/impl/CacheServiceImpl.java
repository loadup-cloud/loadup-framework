package com.github.loadup.components.gateway.service.impl;

import com.github.loadup.components.gateway.cache.manager.CacheManager;
import com.github.loadup.components.gateway.facade.api.CacheService;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

/**
 * <p>
 * CacheServiceImpl.java
 * </p>
 */
@Component("cacheService")
public class CacheServiceImpl implements CacheService {

    @Resource
    @Qualifier("cacheManager")
    private CacheManager cacheManager;

    @Override
    public void refreshInterface(String interfaceId) {
        cacheManager.refreshByInterfaceId(interfaceId);
    }

    @Override
    public void refreshSecurity(String clientId) {
        cacheManager.refreshCert(clientId);
    }

    @Override
    public void refreshAll() {
        cacheManager.refresh();
    }
}