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

import java.util.HashMap;
import java.util.Map;

/**
 * Extension Communication Configuration.
 */
public class CommunicationConfiguration {
    /**
     * communicationId
     */
    private String communicationId;

    /**
     * protocol
     */
    private String protocol;

    /**
     * the uri that we receive
     */
    private String uri;

    /**
     * communication properties
     */
    private Map<String, String> properties = new HashMap<String, String>();

    /**
     *
     */
    public String getCommunicationId() {
        return communicationId;
    }

    /**
     *
     */
    public void setCommunicationId(String communicationId) {
        this.communicationId = communicationId;
    }

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
    public Map<String, String> getProperties() {
        return properties;
    }

    /**
     *
     */
    public void setProperties(Map<String, String> properties) {
        this.properties = properties;
    }
}
