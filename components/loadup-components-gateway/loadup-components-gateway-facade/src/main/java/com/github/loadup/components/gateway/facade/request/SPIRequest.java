package com.github.loadup.components.gateway.facade.request;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.util.Map;

/**
 *
 */
public class SPIRequest {
    /**
     * SPI 地址
     * the destination uri or interface id set in the SPI_CONF.csv
     */
    private String              integrationUrl;
    /**
     * SPI 请求内容
     * request message
     */
    private Map<String, String> message;

    public SPIRequest() {
    }

    public SPIRequest(String integrationUrl, Map<String, String> message) {
        this.integrationUrl = integrationUrl;
        this.message = message;
    }

    /**
     * Getter method for property <tt>integrationUrl</tt>.
     */
    public String getIntegrationUrl() {
        return integrationUrl;
    }

    /**
     * Setter method for property <tt>integrationUrl</tt>.
     */
    public void setIntegrationUrl(String integrationUrl) {
        this.integrationUrl = integrationUrl;
    }

    /**
     * Getter method for property <tt>message</tt>.
     */
    public Map<String, String> getMessage() {
        return message;
    }

    /**
     * Setter method for property <tt>message</tt>.
     */
    public void setMessage(Map<String, String> message) {
        this.message = message;
    }

    /**
     * to string
     */
    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.JSON_STYLE);
    }
}