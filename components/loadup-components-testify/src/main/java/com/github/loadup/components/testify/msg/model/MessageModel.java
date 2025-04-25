/**

 * Copyright (c) 2004-2015 All Rights Reserved.
 */
package com.github.loadup.components.testify.msg.model;

/*-
 * #%L
 * loadup-components-test
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

/**
 * 消息校验模型
 *
 * 
 *
 */
public class MessageModel {

    /**
     * message的标识码
     */
    private String messageKey;

    /**
     * 消息事件码
     */
    private String eventCode;

    /**
     * 消息主题
     */
    private String eventTopic;

    /**
     * 消息类名
     */
    private String messageClass;

    /**
     * @see Object#toString()
     */
    @Override
    public String toString() {
        return "MessageModel [messageKey=" + messageKey + ", eventCode=" + eventCode
                + ", eventTopic=" + eventTopic + ", messageClass=" + messageClass + "]";
    }

    /**
     * 
     *
     * @return property value of messageKey
     */
    public String getMessageKey() {
        return messageKey;
    }

    /**
     * 
     *
     * @param messageKey value to be assigned to property messageKey
     */
    public void setMessageKey(String messageKey) {
        this.messageKey = messageKey;
    }

    /**
     * 
     *
     * @return property value of eventCode
     */
    public String getEventCode() {
        return eventCode;
    }

    /**
     * 
     *
     * @param eventCode value to be assigned to property eventCode
     */
    public void setEventCode(String eventCode) {
        this.eventCode = eventCode;
    }

    /**
     * 
     *
     * @return property value of eventTopic
     */
    public String getEventTopic() {
        return eventTopic;
    }

    /**
     * 
     *
     * @param eventTopic value to be assigned to property eventTopic
     */
    public void setEventTopic(String eventTopic) {
        this.eventTopic = eventTopic;
    }

    /**
     * 
     *
     * @return property value of messageClass
     */
    public String getMessageClass() {
        return messageClass;
    }

    /**
     * 
     *
     * @param messageClass value to be assigned to property messageClass
     */
    public void setMessageClass(String messageClass) {
        this.messageClass = messageClass;
    }

}
