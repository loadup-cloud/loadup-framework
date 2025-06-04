/* Copyright (C) LoadUp Cloud 2022-2025 */
package com.github.loadup.components.gateway.facade.model;

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

/**
 * <p>
 * CommunicationConfig.java
 * </p>
 */
public class CommunicationConfigDto {

    private String communicationId;

    private String interfaceId;

    private String protocol;

    private String uri;

    private String connectTimeout;

    private String readTimeout;

    private String properties;

    /**
     * Gets get communication id.
     */
    public String getCommunicationId() {
        return communicationId;
    }

    /**
     * Sets set communication id.
     */
    public void setCommunicationId(String communicationId) {
        this.communicationId = communicationId;
    }

    /**
     * Gets get interface id.
     */
    public String getInterfaceId() {
        return interfaceId;
    }

    /**
     * Sets set interface id.
     */
    public void setInterfaceId(String interfaceId) {
        this.interfaceId = interfaceId;
    }

    /**
     * Gets get uri.
     */
    public String getUri() {
        return uri;
    }

    /**
     * Sets set uri.
     */
    public void setUri(String uri) {
        this.uri = uri;
    }

    /**
     * Gets get connect timeout.
     */
    public String getConnectTimeout() {
        return connectTimeout;
    }

    /**
     * Sets set connect timeout.
     */
    public void setConnectTimeout(String connectTimeout) {
        this.connectTimeout = connectTimeout;
    }

    /**
     * Gets get read timeout.
     */
    public String getReadTimeout() {
        return readTimeout;
    }

    /**
     * Sets set read timeout.
     */
    public void setReadTimeout(String readTimeout) {
        this.readTimeout = readTimeout;
    }

    /**
     * Gets get properties.
     */
    public String getProperties() {
        return properties;
    }

    /**
     * Sets set properties.
     */
    public void setProperties(String properties) {
        this.properties = properties;
    }

    /**
     * Gets get protocol.
     */
    public String getProtocol() {
        return protocol;
    }

    /**
     * Sets set protocol.
     */
    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }
}
