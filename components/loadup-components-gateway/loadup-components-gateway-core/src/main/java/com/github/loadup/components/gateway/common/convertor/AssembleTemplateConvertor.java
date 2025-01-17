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
