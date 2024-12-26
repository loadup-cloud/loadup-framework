package com.github.loadup.components.gateway.facade.api;

/**
 * <p>Cache Service</p>
 */
public interface CacheService {

    /**
     * refresh an interface cache
     */
    void refreshInterface(String interfaceId);

    /**
     * refresh a security cache
     */
    void refreshSecurity(String clientId);

    /**
     * <p>
     * after you modify your configuration, you can call this method to refresh the cache
     * </p>
     */
    public void refreshAll();
}