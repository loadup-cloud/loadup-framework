package com.github.loadup.components.gateway.facade.model;

/**
 * <p>
 * MessageReceiverConfig.java
 * </p>
 */
public class MessageReceiverConfigDto {

    /**
     * Primary key
     */
    private String messageReceiverId;

    /**
     * Optional
     */
    private String messageReceiverName;

    /**
     * Optional
     */
    private String certCode;

    /**
     * Optional
     */
    private String properties;

    /**
     * Gets get message receiver id.
     */
    public String getMessageReceiverId() {
        return messageReceiverId;
    }

    /**
     * Sets set message receiver id.
     */
    public void setMessageReceiverId(String messageReceiverId) {
        this.messageReceiverId = messageReceiverId;
    }

    /**
     * Gets get message receiver name.
     */
    public String getMessageReceiverName() {
        return messageReceiverName;
    }

    /**
     * Sets set message receiver name.
     */
    public void setMessageReceiverName(String messageReceiverName) {
        this.messageReceiverName = messageReceiverName;
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
}