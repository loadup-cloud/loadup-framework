/* Copyright (C) LoadUp Cloud 2022-2025 */
package com.github.loadup.components.gateway.plugin.repository.file.config;

/*-
 * #%L
 * repository-file-plugin
 * %%
 * Copyright (C) 2022 - 2025 loadup_cloud
 * %%
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 * #L%
 */

import com.github.loadup.components.gateway.facade.model.MessageProcessConfigDto;
import com.github.loadup.components.gateway.facade.model.MessageReceiverConfigDto;
import com.github.loadup.components.gateway.plugin.repository.file.model.ApiConfigRepository;
import com.github.loadup.components.gateway.repository.common.AbstractInterfaceConfigBuilder;
import java.util.HashMap;
import java.util.Map;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * MessageProcessConfig builder
 */
@Component("gatewayFileMessageProcessConfigBuilder")
@Getter
@Setter
@Slf4j
public class MessageProcessConfigBuilder extends AbstractInterfaceConfigBuilder<MessageProcessConfigDto> {

    private Map<String, String> assembleTemplateCache = new HashMap<>();
    private Map<String, String> parseTemplateCache    = new HashMap<>();

    /**
     * generic config build from template map
     */
    public MessageProcessConfigDto build(ApiConfigRepository apiConfig) {
        return build(apiConfig.getOpenURl(), apiConfig.getSecurityStrategyCode(), apiConfig.getHeaderAssembler(),
                apiConfig.getBodyAssembler(), apiConfig.getResponseParser());
    }
    public MessageProcessConfigDto build(
            String url, String securityStrategyCode, String headerAssemble, String bodyAssemble, String parser) {
        if (!validate(url, securityStrategyCode) || StringUtils.isBlank(parser)) {
            return null;
        }

        MessageProcessConfigDto messageProcessConfigDto = new MessageProcessConfigDto();
        messageProcessConfigDto.setMessageProcessId(generateBizKey(url));
        // 1 integration_service_response_parser

        messageProcessConfigDto.setParserClassName(StringUtils.removeEnd(parser, ".java"));
        messageProcessConfigDto.setParserTemplate(parseTemplateCache.get(parser));

        // 2 integration_service_request_header_assemble 从缓存中获取
        messageProcessConfigDto.setHeaderTemplate(assembleTemplateCache.get(headerAssemble));

        // 3 integration_service_request_assemble 从缓存中获取
        messageProcessConfigDto.setAssembleTemplate(assembleTemplateCache.get(bodyAssemble));
        messageProcessConfigDto.setAssembleClassName(StringUtils.removeEnd(bodyAssemble, ".java"));
        return messageProcessConfigDto;
    }
}
