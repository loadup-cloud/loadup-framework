package com.github.loadup.components.gateway.facade.response;

import com.github.loadup.components.gateway.facade.enums.MessageFormat;

/**
 *
 */
public class CommunicationConfigResponse {

    private String protocol;

    private String uri;

    private String integrationUri;

    /**
     * 发送消息类型
     */
    private MessageFormat recvMessageFormat;

    /**
     * 接收消息类型
     */
    private MessageFormat sendMessageFormat;

    private String connectTimeout;

    private String readTimeout;

    private String properties;

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
     * Getter method for property <tt>recvMessageFormat</tt>.
     */
    public MessageFormat getRecvMessageFormat() {
        return recvMessageFormat;
    }

    /**
     * Setter method for property <tt>recvMessageFormat</tt>.
     */
    public void setRecvMessageFormat(MessageFormat recvMessageFormat) {
        this.recvMessageFormat = recvMessageFormat;
    }

    /**
     * Getter method for property <tt>sendMessageFormat</tt>.
     */
    public MessageFormat getSendMessageFormat() {
        return sendMessageFormat;
    }

    /**
     * Setter method for property <tt>sendMessageFormat</tt>.
     */
    public void setSendMessageFormat(MessageFormat sendMessageFormat) {
        this.sendMessageFormat = sendMessageFormat;
    }

    /**
     * Getter method for property <tt>connectTimeout</tt>.
     */
    public String getConnectTimeout() {
        return connectTimeout;
    }

    /**
     * Setter method for property <tt>connectTimeout</tt>.
     */
    public void setConnectTimeout(String connectTimeout) {
        this.connectTimeout = connectTimeout;
    }

    /**
     * Getter method for property <tt>readTimeout</tt>.
     */
    public String getReadTimeout() {
        return readTimeout;
    }

    /**
     * Setter method for property <tt>readTimeout</tt>.
     */
    public void setReadTimeout(String readTimeout) {
        this.readTimeout = readTimeout;
    }

    /**
     * Getter method for property <tt>properties</tt>.
     */
    public String getProperties() {
        return properties;
    }

    /**
     * Setter method for property <tt>properties</tt>.
     */
    public void setProperties(String properties) {
        this.properties = properties;
    }

    /**
     * Getter method for property <tt>integrationUri</tt>.
     */
    public String getIntegrationUri() {
        return integrationUri;
    }

    /**
     * Setter method for property <tt>integrationUri</tt>.
     */
    public void setIntegrationUri(String integrationUri) {
        this.integrationUri = integrationUri;
    }
}