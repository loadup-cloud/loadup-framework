package com.github.loadup.components.gateway.facade.model;

/**
 * <p>
 * MessageSenderConfig.java
 * </p>
 */
public class MessageSenderConfigDto {

    /**
     * Primary key
     */
    private String messageSenderId;

    /**
     * Optional
     */
    private String messageSenderName;

    /**
     * Optional
     */
    private String certCode;

    /**
     * Optional
     */
    private String properties;

    /**
     * Gets get message sender id.
     */
    public String getMessageSenderId() {
        return messageSenderId;
    }

    /**
     * Sets set message sender id.
     */
    public void setMessageSenderId(String messageSenderId) {
        this.messageSenderId = messageSenderId;
    }

    /**
     * Gets get message sender name.
     */
    public String getMessageSenderName() {
        return messageSenderName;
    }

    /**
     * Sets set message sender name.
     */
    public void setMessageSenderName(String messageSenderName) {
        this.messageSenderName = messageSenderName;
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