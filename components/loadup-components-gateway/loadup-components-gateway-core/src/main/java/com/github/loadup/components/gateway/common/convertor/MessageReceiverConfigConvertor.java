package com.github.loadup.components.gateway.common.convertor;

import com.github.loadup.components.gateway.core.model.MessageReceiverConfig;
import com.github.loadup.components.gateway.facade.model.MessageReceiverConfigDto;
import org.apache.commons.collections4.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class MessageReceiverConfigConvertor {

    /**
     * convert dto model to domain model
     */
    public static MessageReceiverConfig dto2Model(MessageReceiverConfigDto item) {
        MessageReceiverConfig messageReceiverConfig = new MessageReceiverConfig();
        messageReceiverConfig.setMessageReceiverId(item.getMessageReceiverId());
        messageReceiverConfig.setMessageReceiverName(item.getMessageReceiverName());
        return messageReceiverConfig;
    }

    /**
     * convert dto models to domain models
     */
    public static List<MessageReceiverConfig> dtoList2ModelList(List<MessageReceiverConfigDto> messageReceiverConfigDtos) {
        List<MessageReceiverConfig> models = new ArrayList<>();
        if (CollectionUtils.isEmpty(messageReceiverConfigDtos)) {
            return models;
        }
        for (MessageReceiverConfigDto dto : messageReceiverConfigDtos) {
            models.add(dto2Model(dto));
        }
        return models;
    }
}