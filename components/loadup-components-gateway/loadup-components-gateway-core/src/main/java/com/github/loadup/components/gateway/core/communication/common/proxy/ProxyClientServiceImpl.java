package com.github.loadup.components.gateway.core.communication.common.proxy;

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

import com.alibaba.cola.extension.BizScenario;
import com.alibaba.cola.extension.ExtensionExecutor;
import com.github.loadup.components.gateway.core.common.Constant;
import com.github.loadup.components.gateway.core.common.enums.MessageFormat;
import com.github.loadup.components.gateway.core.communication.common.facade.CommunicationService;
import com.github.loadup.components.gateway.core.communication.common.impl.CommunicationServiceImpl;
import com.github.loadup.components.gateway.core.communication.common.model.MessageSendResult;
import com.github.loadup.components.gateway.core.model.CommunicationConfig;
import com.github.loadup.components.gateway.core.model.common.MessageEnvelope;
import com.github.loadup.components.gateway.core.model.communication.TransportProtocol;
import com.github.loadup.components.gateway.facade.extpoint.CommunicationProxyExtPt;
import com.github.loadup.components.gateway.facade.model.CommunicationConfiguration;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

/**
 * 代理协议服务实现
 */
@Component("proxyClientService")
public class ProxyClientServiceImpl implements CommunicationService, InitializingBean {

    /**
     * logger
     */
    private static final Logger logger = LoggerFactory.getLogger(ProxyClientServiceImpl.class);

    @Resource
    private ExtensionExecutor extensionExecutor;

    /**
     * @see CommunicationService#send(String, CommunicationConfig, MessageEnvelope)
     */
    @Override
    public MessageSendResult send(
            String traceId, CommunicationConfig communicationConfig, MessageEnvelope messageEnvelope) {

        String protocol = communicationConfig.getProtocol();

        // String messageContent = messageEnvelope.toString(); @TODO to be confirmed?
        String messageContent = messageEnvelope.getContent().toString();
        CommunicationConfiguration communicationConfigDto = new CommunicationConfiguration();
        communicationConfigDto.setProtocol(protocol);
        communicationConfigDto.setUri(communicationConfig.getUri().getUrl());
        communicationConfig
                .getProperties()
                .getConfigMap()
                .put(Constant.CONNECTION_TIMEOUT, String.valueOf(communicationConfig.getConnectTimeout()));
        communicationConfigDto.setProperties(communicationConfig.getProperties().getConfigMap());

        String resultContent = extensionExecutor.execute(
                CommunicationProxyExtPt.class,
                BizScenario.valueOf(protocol),
                ext -> ext.sendMessage(communicationConfigDto, messageContent));

        MessageEnvelope responseMsg = new MessageEnvelope(MessageFormat.TEXT, resultContent, null);

        return new MessageSendResult(responseMsg, false, false);
    }

    /**
     * @see CommunicationService#init(Object...)
     */
    @Override
    public void init(Object... obj) {}

    /**
     * @see CommunicationService#isInitOk()
     */
    @Override
    public boolean isInitOk() {
        return true;
    }

    /**
     * @see CommunicationService#refresh(Object...)
     */
    @Override
    public void refresh(Object... obj) {}

    /**
     * @see CommunicationService#refreshById(String, Object...)
     */
    @Override
    public void refreshById(String id, Object... obj) {}

    /**
     * @see InitializingBean#afterPropertiesSet()
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        CommunicationServiceImpl.registerHandler(TransportProtocol.PROXY, this);
    }
}
