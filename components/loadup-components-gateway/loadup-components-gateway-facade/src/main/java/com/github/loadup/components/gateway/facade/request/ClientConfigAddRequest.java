package com.github.loadup.components.gateway.facade.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.Map;

/**
 *
 */
public class ClientConfigAddRequest extends BaseRequest {

    @NotBlank(message = "clientId can not be blank")
    @Size(max = 64, message = "clientId's length should be not bigger than 64.")
    private String clientId;

    @Size(max = 1024, message = "name's length should be not bigger than 1024.")
    private String name;

    private Map<String, String> properties;

    /**
     * Getter method for property <tt>clientId</tt>.
     */
    public String getClientId() {
        return clientId;
    }

    /**
     * Setter method for property <tt>clientId</tt>.
     */
    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    /**
     * Getter method for property <tt>name</tt>.
     */
    public String getName() {
        return name;
    }

    /**
     * Setter method for property <tt>name</tt>.
     */
    public void setName(String name) {
        this.name = name;
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