package com.github.loadup.components.gateway.common.convertor;

import com.github.loadup.components.gateway.facade.config.model.APIConditionGroup;
import com.github.loadup.components.gateway.facade.config.model.SPIConditionGroup;
import com.github.loadup.components.gateway.message.base.model.AssembleTemplate;
import com.github.loadup.components.gateway.message.base.model.MessageStruct;

/**
 *
 */
public class AssembleTemplateConvertor {

    /**
     * convert APIConditionGroup item model to sender domain model
     */
    public static AssembleTemplate convertToSenderConfig(APIConditionGroup item) {
        if (item == null) {
            return null;
        }
        AssembleTemplate senderResponseTemplate = new AssembleTemplate();
        senderResponseTemplate.setMessageProcessId(item.getUrl());
        senderResponseTemplate.setMainTemplate(item.getInterfaceResponseBodyAssemble());
        senderResponseTemplate.setHeaderTemplate(item.getInterfaceResponseHeaderAssemble());
        senderResponseTemplate.setMessageStruct(MessageStruct.TEXT);
        return senderResponseTemplate;
    }

    /**
     * convert APIConditionGroup item model to receiver domain model
     */
    public static AssembleTemplate convertToReceiverConfig(APIConditionGroup item) {
        if (item == null) {
            return null;
        }
        AssembleTemplate receiverResponseTemplate = new AssembleTemplate();
        receiverResponseTemplate.setMessageProcessId(item.getIntegrationUrl());
        receiverResponseTemplate.setMainTemplate(item.getIntegrationRequestBodyAssemble());
        receiverResponseTemplate.setHeaderTemplate(item.getIntegrationRequestHeaderAssemble());
        receiverResponseTemplate.setMessageStruct(MessageStruct.TEXT);
        return receiverResponseTemplate;
    }

    /**
     * convert SPIConditionGroup item model to receiver domain model
     */
    public static AssembleTemplate convertToReceiverConfig(SPIConditionGroup item) {
        if (item == null) {
            return null;
        }
        AssembleTemplate receiverResponseTemplate = new AssembleTemplate();
        receiverResponseTemplate.setMessageProcessId(item.getIntegrationUrl());
        receiverResponseTemplate.setMainTemplate(item.getIntegrationRequestBodyAssemble());
        receiverResponseTemplate.setHeaderTemplate(item.getIntegrationRequestHeaderAssemble());
        receiverResponseTemplate.setMessageStruct(MessageStruct.TEXT);
        return receiverResponseTemplate;
    }
}