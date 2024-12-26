package com.github.loadup.components.gateway.common.convertor;

import com.github.loadup.components.gateway.core.model.MessageSenderConfig;
import com.github.loadup.components.gateway.facade.model.MessageSenderConfigDto;
import org.apache.commons.collections4.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class MessageSenderConfigConvertor {

    /**
     * convert dto model to domain model
     */
    public static MessageSenderConfig dto2Model(MessageSenderConfigDto item) {
        MessageSenderConfig messageSenderConfig = new MessageSenderConfig();
        messageSenderConfig.setMessageSenderId(item.getMessageSenderId());
        messageSenderConfig.setMessageSenderName(item.getMessageSenderName());
        return messageSenderConfig;
    }

    /**
     * convert dto models to domain models
     */
    public static List<MessageSenderConfig> dtoList2ModelList(List<MessageSenderConfigDto> messageSenderConfigDtos) {
        List<MessageSenderConfig> models = new ArrayList<>();
        if (CollectionUtils.isEmpty(messageSenderConfigDtos)) {
            return models;
        }
        for (MessageSenderConfigDto dto : messageSenderConfigDtos) {
            models.add(dto2Model(dto));
        }
        return models;
    }
}