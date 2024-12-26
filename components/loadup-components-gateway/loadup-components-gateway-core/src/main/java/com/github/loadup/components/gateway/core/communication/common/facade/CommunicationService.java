package com.github.loadup.components.gateway.core.communication.common.facade;

import com.github.loadup.components.gateway.core.common.facade.Refreshable;
import com.github.loadup.components.gateway.core.communication.common.model.MessageSendResult;
import com.github.loadup.components.gateway.core.model.CommunicationConfig;
import com.github.loadup.components.gateway.core.model.common.MessageEnvelope;

/**
 * 通信api服务
 */
public interface CommunicationService extends Refreshable {

    /**
     * 发送报文方法
     */
    MessageSendResult send(String traceId, CommunicationConfig communicationConfig, MessageEnvelope messageEnvelope);

}
