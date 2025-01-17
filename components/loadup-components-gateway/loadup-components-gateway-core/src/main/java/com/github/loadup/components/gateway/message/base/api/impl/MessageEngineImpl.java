package com.github.loadup.components.gateway.message.base.api.impl;

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

import com.github.loadup.commons.error.CommonException;
import com.github.loadup.components.gateway.core.common.enums.RoleType;
import com.github.loadup.components.gateway.core.ctrl.context.GatewayRuntimeProcessContext;
import com.github.loadup.components.gateway.core.model.common.MessageEnvelope;
import com.github.loadup.components.gateway.message.base.api.GroovyEngine;
import com.github.loadup.components.gateway.message.base.api.MessageEngine;
import com.github.loadup.components.gateway.message.script.cache.GroovyScriptCache;
import com.github.loadup.components.gateway.message.script.parser.MessageParser;
import com.github.loadup.components.gateway.message.unimsg.UnifyMsg;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

/**
 * 报文引擎服务实现
 */
@Component("messageEngine")
public class MessageEngineImpl implements MessageEngine {

    @Autowired
    @Qualifier("standerApiMsgParser")
    private MessageParser defaultMessageParser;

    @Resource
    private GroovyEngine groovyEngine;

    @Override
    public MessageEnvelope assemble(String interfaceId,
                                    RoleType roleType,
                                    GatewayRuntimeProcessContext context,
                                    String interfaceTypeStr,
                                    UnifyMsg message) {
        String beanName = GroovyScriptCache.getBeanName(interfaceId + "_ASSEMBLER", roleType,
                context.getTransactionType());
        return groovyEngine.assemble(beanName, roleType, context.getTransactionType(), message);

    }

    @Override
    public MessageEnvelope assembleErrorMessage(String interfaceId, RoleType roleType,
                                                GatewayRuntimeProcessContext context, String interfaceTypeStr,
                                                UnifyMsg message,
                                                MessageEnvelope messageEnvelope,
                                                CommonException exception) {
        String beanName = GroovyScriptCache.getBeanName(interfaceId + "_ASSEMBLER", roleType,
                context.getTransactionType());
        return groovyEngine.assembleErrorMessage(beanName, roleType, context.getTransactionType(), message, exception);
    }

    @Override
    public UnifyMsg parse(String interfaceId, RoleType roleType,
                          GatewayRuntimeProcessContext context,
                          MessageEnvelope messageEnvelope) {
        String beanName = GroovyScriptCache.getBeanName(interfaceId + "_PARSER", roleType,
                context.getTransactionType());
        if (StringUtils.isBlank(beanName) && roleType == RoleType.SENDER) {
            return defaultMessageParser.parse(messageEnvelope);
        } else {
            return groovyEngine.parse(beanName, roleType, context.getTransactionType(), messageEnvelope);
        }
    }

}
