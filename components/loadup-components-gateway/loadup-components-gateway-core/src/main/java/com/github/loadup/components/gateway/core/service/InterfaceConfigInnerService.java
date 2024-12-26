package com.github.loadup.components.gateway.core.service;

import com.github.loadup.components.gateway.core.model.*;

import java.util.List;

/**
 *
 */
public interface InterfaceConfigInnerService {

    /**
     *
     */
    void putApiConfigsToCache(List<InterfaceConfig> interfaceConfigList,
                              List<MessageSenderConfig> messageSenderConfigList,
                              List<MessageReceiverConfig> messageReceiverConfigList,
                              List<MessageProcessConfig> messageProcessConfigList,
                              List<CommunicationConfig> communicationConfigList);

    /**
     *
     */
    void putSpiConfigsToCache(List<InterfaceConfig> interfaceConfigList,
                              List<MessageSenderConfig> messageSenderConfigList,
                              List<MessageReceiverConfig> messageReceiverConfigList,
                              List<MessageProcessConfig> messageProcessConfigList,
                              List<CommunicationConfig> communicationConfigList);
}