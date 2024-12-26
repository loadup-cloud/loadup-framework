package com.github.loadup.components.gateway.plugin.repository.database.config;

import com.github.loadup.components.gateway.common.util.MD5Util;
import com.github.loadup.components.gateway.facade.model.MessageProcessConfigDto;
import com.github.loadup.components.gateway.facade.util.LogUtil;
import com.github.loadup.components.gateway.repository.common.AbstractInterfaceConfigBuilder;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * MessageProcessConfig builder
 */
@Component("databaseMessageProcessConfigBuilder")
public class MessageProcessConfigBuilder extends
        AbstractInterfaceConfigBuilder<MessageProcessConfigDto> {

    /**
     * logger
     */
    private static final Logger logger = LoggerFactory
            .getLogger(MessageProcessConfigBuilder.class);

    /**
     * generic config build with template content, not template map
     */
    public MessageProcessConfigDto buildByTemplateContent(String interfaceId,
                                                          String requestHeaderAssembleTemplate,
                                                          String requestAssembleTemplate,
                                                          String responseParserTemplate) {
        if (StringUtils.isBlank(responseParserTemplate)) {
            return null;
        }

        MessageProcessConfigDto messageProcessConfigDto = new MessageProcessConfigDto();
        messageProcessConfigDto.setMessageProcessId(interfaceId);
        try {
            String parserClassName = MD5Util.getMD5String(responseParserTemplate);
            messageProcessConfigDto.setParserClassName(parserClassName);
        } catch (Exception ex) {
            LogUtil.error(logger, ex, "Generate MD5 for parser class error.");
        }
        messageProcessConfigDto.setParserTemplate(responseParserTemplate);
        messageProcessConfigDto.setHeaderTemplate(requestHeaderAssembleTemplate);
        messageProcessConfigDto.setAssembleTemplate(requestAssembleTemplate);
        return messageProcessConfigDto;
    }
}
