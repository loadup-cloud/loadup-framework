package com.github.loadup.components.gateway.message.base.api.impl;

import com.github.loadup.components.gateway.common.exception.GatewayException;
import com.github.loadup.components.gateway.common.exception.util.AssertUtil;
import com.github.loadup.components.gateway.core.common.enums.RoleType;
import com.github.loadup.components.gateway.core.model.common.MessageEnvelope;
import com.github.loadup.components.gateway.message.base.api.GroovyEngine;
import com.github.loadup.components.gateway.message.base.assemble.MessageAssembler;
import com.github.loadup.components.gateway.message.common.errorr.ParserErrorCode;
import com.github.loadup.components.gateway.message.script.parser.MessageParser;
import com.github.loadup.components.gateway.message.script.parser.groovy.GroovyDynamicLoader;
import com.github.loadup.components.gateway.message.unimsg.UnifyMsg;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

@Component
public class GroovyEngineImpl implements GroovyEngine {
    @Resource
    private GroovyDynamicLoader groovyDynamicLoader;

    @Override
    public UnifyMsg parse(String beanName, RoleType roleType, String interfaceTypeStr,
                          MessageEnvelope messageEnvelope) {
        try {
            AssertUtil.isNotBlank(beanName, ParserErrorCode.NOT_EXIST_SCRIPT);
            MessageParser messageParser = groovyDynamicLoader.getGroovyBean(beanName);
            return messageParser.parse(messageEnvelope);
        } catch (GatewayException e) {
            throw e;
        } catch (Exception e) {
            throw new GatewayException(ParserErrorCode.PARSE_ERROR, e);
        }
    }

    @Override
    public MessageEnvelope assemble(String beanName, RoleType roleType, String interfaceTypeStr, UnifyMsg message) {
        try {
            AssertUtil.isNotBlank(beanName, ParserErrorCode.NOT_EXIST_SCRIPT);
            MessageAssembler messageAssembler = groovyDynamicLoader.getGroovyBean(beanName);
            return messageAssembler.assemble(message);
        } catch (GatewayException e) {
            throw e;
        } catch (Exception e) {
            throw new GatewayException(ParserErrorCode.PARSE_ERROR, e);
        }
    }

    @Override
    public MessageEnvelope assembleErrorMessage(String beanName, RoleType roleType, String interfaceTypeStr, UnifyMsg message,
                                                GatewayException exception) {
        try {
            AssertUtil.isNotBlank(beanName, ParserErrorCode.NOT_EXIST_SCRIPT);
            MessageAssembler messageAssembler = groovyDynamicLoader.getGroovyBean(beanName);
            return messageAssembler.assembleError(message, exception);
        } catch (GatewayException e) {
            throw e;
        } catch (Exception e) {
            throw new GatewayException(ParserErrorCode.PARSE_ERROR, e);
        }
    }
}
