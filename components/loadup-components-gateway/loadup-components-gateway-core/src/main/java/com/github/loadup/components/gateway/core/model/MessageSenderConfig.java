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
public class MessageSenderConfig {

    /**
     * 发送者ID
     */
    private String messageSenderId;

    /**
     * 发送者Name
     */
    private String messageSenderName;

    /**
     * 安全组件Code
     */
    private String certCode;

    /**
     * 扩展参数
     */
    private Properties properties = new Properties();

    /**
     *
     */
    public String getMessageSenderId() {
        return messageSenderId;
    }

    /**
     * 
     */
    public void setMessageSenderId(String messageSenderId) {
        this.messageSenderId = messageSenderId;
    }

    /**
     *
     */
    public String getMessageSenderName() {
        return messageSenderName;
    }

    /**
     * 
     */
    public void setMessageSenderName(String messageSenderName) {
        this.messageSenderName = messageSenderName;
    }

    /**
     *
     */
    public String getCertCode() {
        return certCode;
    }

    /**
     * 
     */
    public void setCertCode(String certCode) {
        this.certCode = certCode;
    }

    /**
     *
     */
    public Properties getProperties() {
        return properties;
    }

    /**
     * 
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
