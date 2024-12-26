package com.github.loadup.components.gateway.facade.request;

import com.github.loadup.components.gateway.facade.enums.InterfaceType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 *
 */
public class InterfaceConfigUpgradeRequest extends BaseRequest {
    /**
     * tenant id
     */
    private String tenantId;

    /**
     * want to upgrade to interface version
     */
    @Size(max = 8, message = "version's length should be not bigger than 8.")
    @NotNull(message = "version can not be null.")
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
     * communication config
     */
    @NotNull(message = "communicationConfig can not be null.")
    private CommunicationConfigRequest communicationConfig;

    /**
     * processConfig
     */
    private MessageProcessConfigRequest processConfig;

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
     * Getter method for property <tt>interfaceType</tt>.
     */
    public InterfaceType getInterfaceType() {
        return interfaceType;
    }

    /**
     * Setter method for property <tt>interfaceType</tt>.
     */
    public void setInterfaceType(InterfaceType interfaceType) {
        this.interfaceType = interfaceType;
    }

    /**
     * Getter method for property <tt>securityStrategyCode</tt>.
     */
    public String getSecurityStrategyCode() {
        return securityStrategyCode;
    }

    /**
     * Setter method for property <tt>securityStrategyCode</tt>.
     */
    public void setSecurityStrategyCode(String securityStrategyCode) {
        this.securityStrategyCode = securityStrategyCode;
    }

    /**
     * Getter method for property <tt>communicationConfig</tt>.
     */
    public CommunicationConfigRequest getCommunicationConfig() {
        return communicationConfig;
    }

    /**
     * Setter method for property <tt>communicationConfig</tt>.
     */
    public void setCommunicationConfig(CommunicationConfigRequest communicationConfig) {
        this.communicationConfig = communicationConfig;
    }

    /**
     * Getter method for property <tt>processConfig</tt>.
     */
    public MessageProcessConfigRequest getProcessConfig() {
        return processConfig;
    }

    /**
     * Setter method for property <tt>processConfig</tt>.
     */
    public void setProcessConfig(MessageProcessConfigRequest processConfig) {
        this.processConfig = processConfig;
    }

    /**
     * Getter method for property <tt>tenantId</tt>.
     */
    public String getTenantId() {
        return tenantId;
    }

    /**
     * Setter method for property <tt>tenantId</tt>.
     */
    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
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
}