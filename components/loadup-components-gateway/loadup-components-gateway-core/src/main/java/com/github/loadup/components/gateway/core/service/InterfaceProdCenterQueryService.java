package com.github.loadup.components.gateway.core.service;

import com.github.loadup.components.gateway.facade.config.model.*;

/**
 *
 */
public interface InterfaceProdCenterQueryService {

    /**
     * query api condition group config
     */
    APIConditionGroup queryAPIConditionGroup(String url, String integrationUrl);

    /**
     * query api condition group config
     */
    SPIConditionGroup querySPIConditionGroup(String integrationUrl);

    /**
     * query communication properties group
     */
    CommunicationPropertiesGroup queryCommunicationPropertiesGroup(String url);

}