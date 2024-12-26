package com.github.loadup.components.gateway.facade.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 *
 */
public class InterfaceConfigOfflineRequest extends BaseRequest {

    @Size(max = 512, message = "interfaceId's length should be not bigger than 512.")
    @NotNull(message = "interfaceId can not be null.")
    private String interfaceId;

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