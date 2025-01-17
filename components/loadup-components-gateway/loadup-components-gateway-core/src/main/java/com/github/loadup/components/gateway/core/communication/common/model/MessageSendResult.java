package com.github.loadup.components.gateway.core.communication.common.model;

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
