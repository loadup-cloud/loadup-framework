package com.github.loadup.components.gateway.core.service.impl;

import com.github.loadup.components.gateway.cache.manager.CacheManager;
import com.github.loadup.components.gateway.core.model.*;
import com.github.loadup.components.gateway.core.service.InterfaceConfigInnerService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 *
 */
@Component("gatewayInterfaceConfigInnerService")
public class InterfaceConfigInnerServiceImpl implements InterfaceConfigInnerService {

    /**
     * cache manager service
     */
    @Resource
    private CacheManager cacheManager;

    /**
     * @see InterfaceConfigInnerService#putApiConfigsToCache(java.util.List, java.util.List, java.util.List, java.util.List, java.util.List)
     */
    @Override
    public void putApiConfigsToCache(List<InterfaceConfig> interfaceConfigList,
                                     List<MessageSenderConfig> messageSenderConfigList,
                                     List<MessageReceiverConfig> messageReceiverConfigList,
                                     List<MessageProcessConfig> messageProcessConfigList,
                                     List<CommunicationConfig> communicationConfigList) {
        // put to cache
        cacheManager.pushToCache(interfaceConfigList, messageProcessConfigList,
                communicationConfigList, messageReceiverConfigList, messageSenderConfigList, null,
                null, null);
    }

    /**
     * @see InterfaceConfigInnerService#putSpiConfigsToCache(java.util.List, java.util.List, java.util.List, java.util.List, java.util.List)
     */
    @Override
    public void putSpiConfigsToCache(List<InterfaceConfig> interfaceConfigList,
                                     List<MessageSenderConfig> messageSenderConfigList,
                                     List<MessageReceiverConfig> messageReceiverConfigList,
                                     List<MessageProcessConfig> messageProcessConfigList,
                                     List<CommunicationConfig> communicationConfigList) {
        // put to cache
        cacheManager.pushToCache(interfaceConfigList, messageProcessConfigList,
                communicationConfigList, messageReceiverConfigList, messageSenderConfigList, null,
                null, null);

    }

}