package com.github.loadup.components.gateway.facade.request;

/**
 *
 */
public class OpenApiTransRequest {

    /**
     * interface id
     */
    private String interfaceId;

    /**
     * message of rpc request
     */
    private String message;

    /**
     * Gets get message.
     */
    public String getMessage() {
        return message;
    }

    /**
     * Sets set message.
     */
    public void setMessage(String message) {
        this.message = message;
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
     * To string string.
     */
    public String toString() {
        StringBuffer sb = new StringBuffer("OpenApiTransRequest{");
        sb.append("interfaceId=").append(interfaceId);
        sb.append(", message='").append(message);
        sb.append('}');
        return sb.toString();
    }
}