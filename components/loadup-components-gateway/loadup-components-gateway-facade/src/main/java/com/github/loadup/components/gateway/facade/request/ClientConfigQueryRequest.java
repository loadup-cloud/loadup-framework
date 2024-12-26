package com.github.loadup.components.gateway.facade.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 *
 */

public class ClientConfigQueryRequest extends BaseRequest {

    @NotBlank(message = "clientId can not be blank")
    @Size(max = 64, message = "clientId's length should be not bigger than 64.")
    private String clientId;

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
}