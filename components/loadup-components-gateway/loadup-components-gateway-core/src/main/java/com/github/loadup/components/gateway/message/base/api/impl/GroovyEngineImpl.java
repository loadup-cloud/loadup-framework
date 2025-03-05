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
    public UnifyMsg parse(
            String beanName, RoleType roleType, String interfaceTypeStr, MessageEnvelope messageEnvelope) {
        try {
            AssertUtil.isNotBlank(beanName, ParserErrorCode.NOT_EXIST_SCRIPT);
            MessageParser messageParser = groovyDynamicLoader.getGroovyBean(beanName);
            return messageParser.parse(messageEnvelope);
        } catch (CommonException e) {
            throw e;
        } catch (Exception e) {
            throw new CommonException(ParserErrorCode.PARSE_ERROR, e);
        }
    }

    @Override
    public MessageEnvelope assemble(String beanName, RoleType roleType, String interfaceTypeStr, UnifyMsg message) {
        try {
            AssertUtil.isNotBlank(beanName, ParserErrorCode.NOT_EXIST_SCRIPT);
            MessageAssembler messageAssembler = groovyDynamicLoader.getGroovyBean(beanName);
            return messageAssembler.assemble(message);
        } catch (CommonException e) {
            throw e;
        } catch (Exception e) {
            throw new CommonException(ParserErrorCode.PARSE_ERROR, e);
        }
    }

    @Override
    public MessageEnvelope assembleErrorMessage(
            String beanName, RoleType roleType, String interfaceTypeStr, UnifyMsg message, CommonException exception) {
        try {
            AssertUtil.isNotBlank(beanName, ParserErrorCode.NOT_EXIST_SCRIPT);
            MessageAssembler messageAssembler = groovyDynamicLoader.getGroovyBean(beanName);
            return messageAssembler.assembleError(message, exception);
        } catch (CommonException e) {
            throw e;
        } catch (Exception e) {
            throw new CommonException(ParserErrorCode.PARSE_ERROR, e);
        }
    }
}
