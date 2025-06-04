/* Copyright (C) LoadUp Cloud 2022-2025 */
package com.github.loadup.components.gateway.plugin.repository.database.config;

/*-
 * #%L
 * repository-database-plugin
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
public class MessageProcessConfigBuilder extends AbstractInterfaceConfigBuilder<MessageProcessConfigDto> {

    /**
     * logger
     */
    private static final Logger logger = LoggerFactory.getLogger(MessageProcessConfigBuilder.class);

    /**
     * generic config build with template content, not template map
     */
    public MessageProcessConfigDto buildByTemplateContent(
            String interfaceId,
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
