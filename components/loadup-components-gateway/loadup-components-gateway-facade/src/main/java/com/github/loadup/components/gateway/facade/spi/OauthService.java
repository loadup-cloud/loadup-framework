package com.github.loadup.components.gateway.facade.spi;

import com.github.loadup.components.gateway.facade.model.AuthRequest;

/**
 * <p>
 * OauthService.java
 * </p>
 */
public interface OauthService {

    /**
     * verify oauth client
     */
    public boolean validateClient(AuthRequest authRequest);

    /**
     * verify oauth token
     */
    public boolean validateToken(AuthRequest authRequest);

}