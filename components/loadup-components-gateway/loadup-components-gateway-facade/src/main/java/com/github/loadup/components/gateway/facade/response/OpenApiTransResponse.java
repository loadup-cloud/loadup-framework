package com.github.loadup.components.gateway.facade.response;

/**
 *
 */
public class OpenApiTransResponse {

    /**
     * message of rpc response
     */
    private Object message;

    /**
     * Gets get message.
     */
    public Object getMessage() {
        return message;
    }

    /**
     * Sets set message.
     */
    public void setMessage(Object message) {
        this.message = message;
    }

    public String toString() {
        StringBuffer sb = new StringBuffer("OpenApiTransResponse{");
        sb.append("message=").append(message);
        sb.append('}');
        return sb.toString();
    }
}