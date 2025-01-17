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
 * InterfaceConfig.java
 * </p>
 */
public class InterfaceConfigDto {

    /**
     * Required
     * Primary key
     */
    private String interfaceId;

    /**
     * Optional
     */
    private String interfaceName;

    /**
     * Required
     */
    private String messageProcessId;

    /**
     * Optional
     */
    private String certCode;

    /**
     * Required When open api
     */
    private String messageReceiverInterfaceId;

    /**
     * version
     */
    private String version;

    /**
     * status
     */
    private String status;

    /**
     * Required
     */
    private String isEnable;

    /**
     * Optional
     */
    private String properties;

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
     * Gets get interface name.
     */
    public String getInterfaceName() {
        return interfaceName;
    }

    /**
     * Sets set interface name.
     */
    public void setInterfaceName(String interfaceName) {
        this.interfaceName = interfaceName;
    }

    /**
     * Gets get message process id.
     */
    public String getMessageProcessId() {
        return messageProcessId;
    }

    /**
     * Sets set message process id.
     */
    public void setMessageProcessId(String messageProcessId) {
        this.messageProcessId = messageProcessId;
    }

    /**
     * Gets get message receiver interface id.
     */
    public String getMessageReceiverInterfaceId() {
        return messageReceiverInterfaceId;
    }

    /**
     * Sets set message receiver interface id.
     */
    public void setMessageReceiverInterfaceId(String messageReceiverInterfaceId) {
        this.messageReceiverInterfaceId = messageReceiverInterfaceId;
    }

    /**
     * Gets get is enable.
     */
    public String getIsEnable() {
        return isEnable;
    }

    /**
     * Sets set is enable.
     */
    public void setIsEnable(String isEnable) {
        this.isEnable = isEnable;
    }

    /**
     * Gets get cert code.
     */
    public String getCertCode() {
        return certCode;
    }

    /**
     * Sets set cert code.
     */
    public void setCertCode(String certCode) {
        this.certCode = certCode;
    }

    /**
     * get properties
     */
    public String getProperties() {
        return properties;
    }

    /**
     * set properties
     */
    public void setProperties(String properties) {
        this.properties = properties;
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
     * Getter method for property <tt>status</tt>.
     */
    public String getStatus() {
        return status;
    }

    /**
     * Setter method for property <tt>status</tt>.
     */
    public void setStatus(String status) {
        this.status = status;
    }
}
