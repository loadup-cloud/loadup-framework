package com.github.loadup.components.gateway.facade.model;

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