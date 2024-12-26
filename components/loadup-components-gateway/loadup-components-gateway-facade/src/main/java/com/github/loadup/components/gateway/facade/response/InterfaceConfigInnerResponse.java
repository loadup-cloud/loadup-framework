package com.github.loadup.components.gateway.facade.response;

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