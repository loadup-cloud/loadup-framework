package com.github.loadup.components.gateway.common.convertor;

import com.github.loadup.components.gateway.core.model.MessageProcessConfig;
import com.github.loadup.components.gateway.facade.model.MessageProcessConfigDto;
import com.github.loadup.components.gateway.message.base.model.MessageStruct;
import org.apache.commons.collections4.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class MessageProcessConfigConvertor {

    /**
     * convert dto model to domain model
     */
    public static MessageProcessConfig dto2Model(MessageProcessConfigDto item) {
        MessageProcessConfig messageProcessConfig = new MessageProcessConfig();
        messageProcessConfig.setMessageProcessId(item.getMessageProcessId());
        messageProcessConfig.setAssembleClassName(item.getAssembleClassName());
        messageProcessConfig.setAssembleTemplate(item.getAssembleTemplate());
        messageProcessConfig.setHeaderTemplate(item.getHeaderTemplate());
        messageProcessConfig.setParserClassName(item.getParserClassName());
        messageProcessConfig.setParserTemplate(item.getParserTemplate());
        messageProcessConfig.setAssembleType(MessageStruct.TEXT.name());
        return messageProcessConfig;
    }

    /**
     * convert dto models to domain models
     */
    public static List<MessageProcessConfig> dtoList2ModelList(List<MessageProcessConfigDto> messageProcessConfigDtos) {
        List<MessageProcessConfig> models = new ArrayList<>();
        if (CollectionUtils.isEmpty(messageProcessConfigDtos)) {
            return models;
        }
        for (MessageProcessConfigDto dto : messageProcessConfigDtos) {
            models.add(dto2Model(dto));
        }
        return models;
    }
}