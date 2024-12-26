package com.github.loadup.components.gateway.facade.spi;

import com.github.loadup.components.gateway.facade.request.OpenApiTransRequest;
import com.github.loadup.components.gateway.facade.response.OpenApiTransResponse;

/**
 * openApi service
 */
public interface OpenApi {

    /**
     * Invoke openApi .
     */
    OpenApiTransResponse invoke(OpenApiTransRequest openApiTransRequest);
}