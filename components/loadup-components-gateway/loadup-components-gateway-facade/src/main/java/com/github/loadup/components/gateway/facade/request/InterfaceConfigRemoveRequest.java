package com.github.loadup.components.gateway.facade.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 *
 */
public class InterfaceConfigRemoveRequest extends BaseRequest {
    /**
     * tenantId
     */
    private String tenantId;

    /**
     * interfaceId
     */
    @Size(max = 512, message = "interfaceName's length should be not bigger than 512.")
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

    /**
     * Getter method for property <tt>tenantId</tt>.
     */
    public String getTenantId() {
        return tenantId;
    }

    /**
     * Setter method for property <tt>tenantId</tt>.
     */
    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }
}