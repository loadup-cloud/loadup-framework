package com.github.loadup.components.gateway.core.model;

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

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * 应用配置
 */
public class MessageReceiverConfig {

    /**
     * 发送者ID
     */
    private String MessageReceiverId;

    /**
     * 发送者Name
     */
    private String MessageReceiverName;

    /**
     * 安全组件Code
     */
    private String certCode;

    /**
     * 扩展参数
     */
    private Properties properties = new Properties();

    /**
     * Getter method for property <tt>MessageReceiverId</tt>.
     */
    public String getMessageReceiverId() {
        return MessageReceiverId;
    }

    /**
     * Setter method for property <tt>MessageReceiverId</tt>.
     */
    public void setMessageReceiverId(String messageReceiverId) {
        MessageReceiverId = messageReceiverId;
    }

    /**
     * Getter method for property <tt>MessageReceiverName</tt>.
     */
    public String getMessageReceiverName() {
        return MessageReceiverName;
    }

    /**
     * Setter method for property <tt>MessageReceiverName</tt>.
     */
    public void setMessageReceiverName(String messageReceiverName) {
        MessageReceiverName = messageReceiverName;
    }

    /**
     * Getter method for property <tt>certCode</tt>.
     */
    public String getCertCode() {
        return certCode;
    }

    /**
     * Setter method for property <tt>certCode</tt>.
     */
    public void setCertCode(String certCode) {
        this.certCode = certCode;
    }

    /**
     * Getter method for property <tt>properties</tt>.
     */
    public Properties getProperties() {
        return properties;
    }

    /**
     * Setter method for property <tt>properties</tt>.
     */
    public void setProperties(Properties properties) {
        this.properties = properties;
    }

    /**
     * toString
     */
    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.JSON_STYLE);
    }
}
