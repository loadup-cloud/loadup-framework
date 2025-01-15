package com.github.loadup.components.gateway.facade.extpoint;

import com.alibaba.cola.extension.ExtensionPointI;
import com.github.loadup.components.gateway.facade.model.CommunicationConfiguration;

/**
 * Extension communication client which can be implemented to support different communication
 * protocol.
 */
public interface CommunicationProxyExtPt extends ExtensionPointI {

    /**
     * send message based on different communication configurations, such as http, SPRINGBean, etc.
     *
     *
     *
     * For example, message for SpringBean call should be in "JSON" format (As the plugin code use json tools to convert message to java
     */
    String sendMessage(CommunicationConfiguration configuration, String messageContent);
}