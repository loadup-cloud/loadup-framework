package com.github.loadup.components.gateway.core.communication.common.proxy;

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

    @Resource
    private              ExtensionExecutor extensionExecutor;
    /**
     * logger
     */
    private static final Logger            logger = LoggerFactory.getLogger(ProxyClientServiceImpl.class);

    /**
     * @see CommunicationService#send(String, CommunicationConfig, MessageEnvelope)
     */
    @Override
    public MessageSendResult send(String traceId, CommunicationConfig communicationConfig,
                                  MessageEnvelope messageEnvelope) {

        String protocol = communicationConfig.getProtocol();

        //String messageContent = messageEnvelope.toString(); @TODO to be confirmed?
        String messageContent = messageEnvelope.getContent().toString();
        CommunicationConfiguration communicationConfigDto = new CommunicationConfiguration();
        communicationConfigDto.setProtocol(protocol);
        communicationConfigDto.setUri(communicationConfig.getUri().getUrl());
        communicationConfig.getProperties().getConfigMap().put(
                Constant.CONNECTION_TIMEOUT, String.valueOf(communicationConfig.getConnectTimeout()));
        communicationConfigDto.setProperties(communicationConfig.getProperties().getConfigMap());

        String resultContent = extensionExecutor.execute(CommunicationProxyExtPt.class, BizScenario.valueOf(protocol),
                ext -> ext.sendMessage(communicationConfigDto, messageContent));

        MessageEnvelope responseMsg = new MessageEnvelope(MessageFormat.TEXT, resultContent, null);

        return new MessageSendResult(responseMsg, false, false);
    }

    /**
     * @see CommunicationService#init(Object...)
     */
    @Override
    public void init(Object... obj) {
    }

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
    public void refresh(Object... obj) {
    }

    /**
     * @see CommunicationService#refreshById(String, Object...)
     */
    @Override
    public void refreshById(String id, Object... obj) {
    }

    /**
     * @see InitializingBean#afterPropertiesSet()
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        CommunicationServiceImpl.registerHandler(TransportProtocol.PROXY, this);
    }

}
