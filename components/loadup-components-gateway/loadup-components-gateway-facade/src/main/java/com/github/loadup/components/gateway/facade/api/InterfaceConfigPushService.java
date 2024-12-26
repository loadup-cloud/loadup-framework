package com.github.loadup.components.gateway.facade.api;

import com.github.loadup.components.gateway.facade.request.APIConfigRequest;
import com.github.loadup.components.gateway.facade.request.SPIConfigRequest;
import com.github.loadup.components.gateway.facade.response.APIConfigResponse;
import com.github.loadup.components.gateway.facade.response.SPIConfigResponse;

/**
 * interface push service
 */
public interface InterfaceConfigPushService {

    /**
     * push api config to gateway runtime
     */
    APIConfigResponse pushAPIConfig(APIConfigRequest request);

    /**
     * push spi config to gateway runtime
     */
    SPIConfigResponse pushSPIConfig(SPIConfigRequest request);

}