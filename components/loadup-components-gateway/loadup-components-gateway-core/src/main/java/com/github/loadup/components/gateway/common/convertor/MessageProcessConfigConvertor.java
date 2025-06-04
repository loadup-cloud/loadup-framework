/* Copyright (C) LoadUp Cloud 2022-2025 */
package com.github.loadup.components.gateway.common.convertor;

/*-
 * #%L
 * loadup-components-gateway-core
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

import com.github.loadup.components.gateway.core.model.MessageProcessConfig;
import com.github.loadup.components.gateway.facade.model.MessageProcessConfigDto;
import com.github.loadup.components.gateway.message.base.model.MessageStruct;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.collections4.CollectionUtils;

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
