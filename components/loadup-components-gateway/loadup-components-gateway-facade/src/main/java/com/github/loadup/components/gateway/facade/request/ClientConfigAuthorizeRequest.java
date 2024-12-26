package com.github.loadup.components.gateway.facade.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 *
 */

public class ClientConfigAuthorizeRequest extends BaseRequest {

    @NotBlank(message = "clientId can not be null")
    @Size(max = 64, message = "clientId's length should be not bigger than 64.")
    private String clientId;

    @NotBlank(message = "interfaceId can not be null")
    @Size(max = 512, message = "interfaceId's length should be not bigger than 512.")
    private String interfaceId;

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
}
