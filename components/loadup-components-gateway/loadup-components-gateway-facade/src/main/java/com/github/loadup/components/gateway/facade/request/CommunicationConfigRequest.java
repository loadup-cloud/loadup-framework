/* Copyright (C) LoadUp Cloud 2022-2025 */
package com.github.loadup.components.gateway.facade.request;

/*-
 * #%L
 * loadup-components-gateway-facade
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

import com.github.loadup.components.gateway.facade.enums.MessageFormat;
import jakarta.validation.constraints.NotNull;
import java.util.Map;

/**
 *
 */
public class CommunicationConfigRequest {
    @NotNull(message = "protocol can not be null.")
    private String protocol;

    @NotNull(message = "uri can not be null.")
    private String uri;

    @NotNull(message = "integrationUri can not be null.")
    private String integrationUri;

    /**
     * 接收消息类型
     */
    private MessageFormat recvMessageFormat;

    /**
     * 发送消息类型
     */
    private MessageFormat sendMessageFormat;

    private String connectTimeout;

    private String readTimeout;

    private Map<String, String> properties;

    /**
     *
     */
    public String getProtocol() {
        return protocol;
    }

    /**
     *
     */
    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    /**
     *
     */
    public String getUri() {
        return uri;
    }

    /**
     *
     */
    public void setUri(String uri) {
        this.uri = uri;
    }

    /**
     *
     */
    public String getIntegrationUri() {
        return integrationUri;
    }

    /**
     *
     */
    public void setIntegrationUri(String integrationUri) {
        this.integrationUri = integrationUri;
    }

    /**
     *
     */
    public String getConnectTimeout() {
        return connectTimeout;
    }

    /**
     *
     */
    public void setConnectTimeout(String connectTimeout) {
        this.connectTimeout = connectTimeout;
    }

    /**
     *
     */
    public String getReadTimeout() {
        return readTimeout;
    }

    /**
     *
     */
    public void setReadTimeout(String readTimeout) {
        this.readTimeout = readTimeout;
    }

    /**
     *
     */
    public Map<String, String> getProperties() {
        return properties;
    }

    /**
     *
     */
    public void setProperties(Map<String, String> properties) {
        this.properties = properties;
    }

    /**
     *
     */
    public MessageFormat getRecvMessageFormat() {
        return recvMessageFormat;
    }

    /**
     *
     */
    public void setRecvMessageFormat(MessageFormat recvMessageFormat) {
        this.recvMessageFormat = recvMessageFormat;
    }

    /**
     *
     */
    public MessageFormat getSendMessageFormat() {
        return sendMessageFormat;
    }

    /**
     *
     */
    public void setSendMessageFormat(MessageFormat sendMessageFormat) {
        this.sendMessageFormat = sendMessageFormat;
    }
}
