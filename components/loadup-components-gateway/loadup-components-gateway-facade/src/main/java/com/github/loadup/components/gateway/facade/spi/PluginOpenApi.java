package com.github.loadup.components.gateway.facade.spi;

import com.alibaba.cola.extension.Extension;
import com.github.loadup.components.gateway.facade.request.PluginAPIRequest;
import com.github.loadup.components.gateway.facade.response.PluginAPIResponse;

/**
 * plugin open api interface
 */
@Extension
public interface PluginOpenApi {

    /**
     * implemented by plugin, invoked by gateway runtime
     */
    PluginAPIResponse invoke(PluginAPIRequest request);
}