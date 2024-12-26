package com.github.loadup.components.gateway.facade.model;

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