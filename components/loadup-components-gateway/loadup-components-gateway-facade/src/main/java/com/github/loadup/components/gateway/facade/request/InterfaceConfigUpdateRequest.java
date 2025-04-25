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

import com.github.loadup.components.gateway.facade.enums.InterfaceType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 *
 */
public class InterfaceConfigUpdateRequest extends BaseRequest {

    /**
     * tenant id
     */
    private String tenantId;

    /**
     * interface version
     */
    private String version;

    /**
     * interfaceId
     */
    @Size(max = 512, message = "interfaceName's length should be not bigger than 512.")
    @NotNull(message = "interfaceId can not be null.")
    private String interfaceId;
    /**
     * interfaceName
     */
    @Size(max = 1024, message = "interfaceName's length should be not bigger than 1024.")
    @NotNull(message = "interfaceName can not be null.")
    private String interfaceName;
    /**
     * interfaceType
     */
    @NotNull(message = "interfaceType can not be null.")
    private InterfaceType interfaceType;
    /**
     * securityStrategyCode
     */
    @Size(max = 64, message = "securityStrategyCode's length should be not bigger than 64.")
    @NotNull(message = "securityStrategyCode can not be null.")
    private String securityStrategyCode;
    /**
     * communicationConfig
     */
    @NotNull(message = "communicationConfig can not be null.")
    private CommunicationConfigRequest communicationConfig;
    /**
     * processConfig
     */
    private MessageProcessConfigRequest processConfig;

    /**
     * 
     */
    public String getInterfaceId() {
        return interfaceId;
    }

    /**
     *
     */
    public void setInterfaceId(String interfaceId) {
        this.interfaceId = interfaceId;
    }

    /**
     * 
     */
    public String getInterfaceName() {
        return interfaceName;
    }

    /**
     *
     */
    public void setInterfaceName(String interfaceName) {
        this.interfaceName = interfaceName;
    }

    /**
     * 
     */
    public InterfaceType getInterfaceType() {
        return interfaceType;
    }

    /**
     *
     */
    public void setInterfaceType(InterfaceType interfaceType) {
        this.interfaceType = interfaceType;
    }

    /**
     * 
     */
    public String getSecurityStrategyCode() {
        return securityStrategyCode;
    }

    /**
     *
     */
    public void setSecurityStrategyCode(String securityStrategyCode) {
        this.securityStrategyCode = securityStrategyCode;
    }

    /**
     * 
     */
    public CommunicationConfigRequest getCommunicationConfig() {
        return communicationConfig;
    }

    /**
     *
     */
    public void setCommunicationConfig(CommunicationConfigRequest communicationConfig) {
        this.communicationConfig = communicationConfig;
    }

    /**
     * 
     */
    public MessageProcessConfigRequest getProcessConfig() {
        return processConfig;
    }

    /**
     *
     */
    public void setProcessConfig(MessageProcessConfigRequest processConfig) {
        this.processConfig = processConfig;
    }

    /**
     * 
     */
    public String getTenantId() {
        return tenantId;
    }

    /**
     *
     */
    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    /**
     * 
     */
    public String getVersion() {
        return version;
    }

    /**
     *
     */
    public void setVersion(String version) {
        this.version = version;
    }
}
