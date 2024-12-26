package com.github.loadup.components.gateway.core.communication.common.protocol;

import com.github.loadup.components.gateway.core.model.common.MessageEnvelope;

/**
 * 消息处理类，只是一个模板类，各个组件可以参考实现
 */
public abstract class ProtocolHandler {

    /**
     * 将接收到的消息进行格式转化
     *
     * @throws Exception
     */
    protected abstract MessageEnvelope convertInMessage(Object inMessage,
                                                        Object... protocolSelector)
            throws Exception;

    /**
     * 将发送或者响应消息进行格式转化
     *
     * @throws Exception
     */
    protected abstract Object convertOutMessage(MessageEnvelope outMessage,
                                                Object... protocolSelector) throws Exception;

}
