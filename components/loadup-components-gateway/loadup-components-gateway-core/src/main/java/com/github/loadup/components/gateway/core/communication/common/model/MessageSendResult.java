package com.github.loadup.components.gateway.core.communication.common.model;

import com.github.loadup.components.gateway.core.model.common.MessageEnvelope;

/**
 * 消息发送响应结果
 */
public class MessageSendResult {

    /**
     * 消息响应内容
     **/
    private final MessageEnvelope messageEnvelope;

    /**
     * 是否超时
     **/
    private final boolean timeout;

    /**
     * 是否有响应
     **/
    private final boolean response;

    /**
     *
     */
    public MessageSendResult(MessageEnvelope messageEnvelope, boolean timeout, boolean response) {
        this.messageEnvelope = messageEnvelope;
        this.timeout = timeout;
        this.response = response;
    }

    /**
     * Getter method for property <tt>messageEnvelope</tt>.
     */
    public MessageEnvelope getMessageEnvelope() {
        return messageEnvelope;
    }

    /**
     * Getter method for property <tt>timeout</tt>.
     */
    public boolean isTimeout() {
        return timeout;
    }

    /**
     * Getter method for property <tt>response</tt>.
     */
    public boolean isResponse() {
        return response;
    }

    /**
     * @see Object#toString()
     */
    @Override
    public String toString() {
        StringBuilder retValue = new StringBuilder("MessageSendResult[");
        retValue.append("timeout=").append(this.timeout).append(',');
        retValue.append("response=").append(this.response);
        retValue.append(']');

        return retValue.toString();
    }

}
