package com.github.loadup.components.gateway.facade.request;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.util.Map;

/**
 *
 */
public class PluginAPIRequest {

    /**
     * body message
     */
    private String message;

    /**
     * extend info
     */
    private Map<String, String> extendInfo;

    /**
     * Getter method for property <tt>message</tt>.
     */
    public String getMessage() {
        return message;
    }

    /**
     * Setter method for property <tt>message</tt>.
     */
    public void setMessage(String message) {
        this.message = message;
    }

    /**
     * Getter method for property <tt>extendInfo</tt>.
     */
    public Map<String, String> getExtendInfo() {
        return extendInfo;
    }

    /**
     * Setter method for property <tt>extendInfo</tt>.
     */
    public void setExtendInfo(Map<String, String> extendInfo) {
        this.extendInfo = extendInfo;
    }

    /**
     * to string
     */
    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.JSON_STYLE);
    }
}