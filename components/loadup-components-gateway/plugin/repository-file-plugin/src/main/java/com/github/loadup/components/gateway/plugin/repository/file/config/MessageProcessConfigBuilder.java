package com.github.loadup.components.gateway.plugin.repository.file.config;

import com.github.loadup.components.gateway.facade.model.MessageProcessConfigDto;
import com.github.loadup.components.gateway.repository.common.AbstractInterfaceConfigBuilder;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * MessageProcessConfig builder
 */
@Component("gatewayFileMessageProcessConfigBuilder")
public class MessageProcessConfigBuilder extends
        AbstractInterfaceConfigBuilder<MessageProcessConfigDto> {

    //组装脚本存在的路径
    private Map<String, String> assembleTemplateCache = new HashMap<String, String>();

    //groovy java 脚本存在的路径
    private Map<String, String> parseTemplateCache = new HashMap<String, String>();

    /**
     * logger
     */
    private static final Logger logger = LoggerFactory
            .getLogger(MessageProcessConfigBuilder.class);

    /**
     * generic config build from template map
     */
    public MessageProcessConfigDto build(String url, String securityStrategyCode,
                                         String headerAssemble, String bodyAssemble,
                                         String parser) {
        if (!validate(url, securityStrategyCode) || StringUtils.isBlank(parser)) {
            return null;
        }

        MessageProcessConfigDto messageProcessConfigDto = new MessageProcessConfigDto();
        messageProcessConfigDto.setMessageProcessId(generateBizKey(url));
        //1 integration_service_response_parser

        messageProcessConfigDto.setParserClassName(StringUtils.removeEnd(parser, ".java"));
        messageProcessConfigDto.setParserTemplate(parseTemplateCache.get(parser));

        //2 integration_service_request_header_assemble 从缓存中获取
        messageProcessConfigDto.setHeaderTemplate(assembleTemplateCache.get(headerAssemble));

        //3 integration_service_request_assemble 从缓存中获取
        messageProcessConfigDto.setAssembleTemplate(assembleTemplateCache.get(bodyAssemble));
        messageProcessConfigDto.setAssembleClassName(StringUtils.removeEnd(bodyAssemble, ".java"));
        return messageProcessConfigDto;
    }

    /**
     * Setter method for property <tt>assembleTemplateCache</tt>.
     */
    public void setAssembleTemplateCache(Map<String, String> assembleTemplateCache) {
        this.assembleTemplateCache = assembleTemplateCache;
    }

    /**
     * Setter method for property <tt>parseTemplateCache</tt>.
     */
    public void setParseTemplateCache(Map<String, String> parseTemplateCache) {
        this.parseTemplateCache = parseTemplateCache;
    }
}
