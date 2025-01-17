package com.github.loadup.components.gateway.facade.response;

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

import com.github.loadup.components.gateway.facade.enums.InterfaceStatus;

/**
 *
 */
public class InterfaceConfigInnerResponse extends BaseResponse {

    private String interfaceId;

    private String interfaceName;

    private InterfaceStatus status;

    private String version;

    private CommunicationConfigResponse communicationConfig;

    private MessageProcessConfigResponse processConfig;

    /**
     * Getter method for property <tt>interfaceId</tt>.
     */
    public String getInterfaceId() {
        return interfaceId;
    }

    /**
     * Setter method for property <tt>interfaceId</tt>.
     */
    public void setInterfaceId(String interfaceId) {
        this.interfaceId = interfaceId;
    }

    /**
     * Getter method for property <tt>interfaceName</tt>.
     */
    public String getInterfaceName() {
        return interfaceName;
    }

    /**
     * Setter method for property <tt>interfaceName</tt>.
     */
    public void setInterfaceName(String interfaceName) {
        this.interfaceName = interfaceName;
    }

    /**
     * Getter method for property <tt>status</tt>.
     */
    public InterfaceStatus getStatus() {
        return status;
    }

    /**
     * Setter method for property <tt>status</tt>.
     */
    public void setStatus(InterfaceStatus status) {
        this.status = status;
    }

    /**
     * Getter method for property <tt>version</tt>.
     */
    public String getVersion() {
        return version;
    }

    /**
     * Setter method for property <tt>version</tt>.
     */
    public void setVersion(String version) {
        this.version = version;
    }

    /**
     * Getter method for property <tt>communicationConfig</tt>.
     */
    public CommunicationConfigResponse getCommunicationConfig() {
        return communicationConfig;
    }

    /**
     * Setter method for property <tt>communicationConfig</tt>.
     */
    public void setCommunicationConfig(CommunicationConfigResponse communicationConfig) {
        this.communicationConfig = communicationConfig;
    }

    /**
     * Getter method for property <tt>processConfig</tt>.
     */
    public MessageProcessConfigResponse getProcessConfig() {
        return processConfig;
    }

    /**
     * Setter method for property <tt>processConfig</tt>.
     */
    public void setProcessConfig(MessageProcessConfigResponse processConfig) {
        this.processConfig = processConfig;
    }
}
