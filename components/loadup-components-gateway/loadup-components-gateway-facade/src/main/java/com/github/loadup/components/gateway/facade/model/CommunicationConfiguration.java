package com.github.loadup.components.gateway.facade.model;

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
     * Getter method for property <tt>communicationId</tt>.
     */
    public String getCommunicationId() {
        return communicationId;
    }

    /**
     * Setter method for property <tt>communicationId</tt>.
     */
    public void setCommunicationId(String communicationId) {
        this.communicationId = communicationId;
    }

    /**
     * Getter method for property <tt>protocol</tt>.
     */
    public String getProtocol() {
        return protocol;
    }

    /**
     * Setter method for property <tt>protocol</tt>.
     */
    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    /**
     * Getter method for property <tt>uri</tt>.
     */
    public String getUri() {
        return uri;
    }

    /**
     * Setter method for property <tt>uri</tt>.
     */
    public void setUri(String uri) {
        this.uri = uri;
    }

    /**
     * Getter method for property <tt>properties</tt>.
     */
    public Map<String, String> getProperties() {
        return properties;
    }

    /**
     * Setter method for property <tt>properties</tt>.
     */
    public void setProperties(Map<String, String> properties) {
        this.properties = properties;
    }
}