package com.github.loadup.components.gateway.message.base.api.impl;

import com.github.loadup.components.gateway.common.exception.GatewayException;
import com.github.loadup.components.gateway.core.common.enums.RoleType;
import com.github.loadup.components.gateway.core.ctrl.context.GatewayRuntimeProcessContext;
import com.github.loadup.components.gateway.core.model.common.MessageEnvelope;
import com.github.loadup.components.gateway.message.base.api.GroovyEngine;
import com.github.loadup.components.gateway.message.base.api.MessageEngine;
import com.github.loadup.components.gateway.message.base.assemble.MessageAssembler;
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
                                                GatewayException exception) {
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
