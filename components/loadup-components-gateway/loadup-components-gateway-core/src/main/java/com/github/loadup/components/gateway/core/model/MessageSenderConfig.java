package com.github.loadup.components.gateway.core.model;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * 应用配置
 */
public class MessageSenderConfig {

    /**
     * 发送者ID
     */
    private String messageSenderId;

    /**
     * 发送者Name
     */
    private String messageSenderName;

    /**
     * 安全组件Code
     */
    private String certCode;

    /**
     * 扩展参数
     */
    private Properties properties = new Properties();

    /**
     * Getter method for property <tt>messageSenderId</tt>.
     */
    public String getMessageSenderId() {
        return messageSenderId;
    }

    /**
     * Setter method for property <tt>messageSenderId</tt>.
     */
    public void setMessageSenderId(String messageSenderId) {
        this.messageSenderId = messageSenderId;
    }

    /**
     * Getter method for property <tt>messageSenderName</tt>.
     */
    public String getMessageSenderName() {
        return messageSenderName;
    }

    /**
     * Setter method for property <tt>messageSenderName</tt>.
     */
    public void setMessageSenderName(String messageSenderName) {
        this.messageSenderName = messageSenderName;
    }

    /**
     * Getter method for property <tt>certCode</tt>.
     */
    public String getCertCode() {
        return certCode;
    }

    /**
     * Setter method for property <tt>certCode</tt>.
     */
    public void setCertCode(String certCode) {
        this.certCode = certCode;
    }

    /**
     * Getter method for property <tt>properties</tt>.
     */
    public Properties getProperties() {
        return properties;
    }

    /**
     * Setter method for property <tt>properties</tt>.
     */
    public void setProperties(Properties properties) {
        this.properties = properties;
    }

    /**
     * toString
     */
    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.JSON_STYLE);
    }
}
