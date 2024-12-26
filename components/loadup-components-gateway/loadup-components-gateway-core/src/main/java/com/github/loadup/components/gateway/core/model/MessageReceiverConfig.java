package com.github.loadup.components.gateway.core.model;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * 应用配置
 */
public class MessageReceiverConfig {

    /**
     * 发送者ID
     */
    private String MessageReceiverId;

    /**
     * 发送者Name
     */
    private String MessageReceiverName;

    /**
     * 安全组件Code
     */
    private String certCode;

    /**
     * 扩展参数
     */
    private Properties properties = new Properties();

    /**
     * Getter method for property <tt>MessageReceiverId</tt>.
     */
    public String getMessageReceiverId() {
        return MessageReceiverId;
    }

    /**
     * Setter method for property <tt>MessageReceiverId</tt>.
     */
    public void setMessageReceiverId(String messageReceiverId) {
        MessageReceiverId = messageReceiverId;
    }

    /**
     * Getter method for property <tt>MessageReceiverName</tt>.
     */
    public String getMessageReceiverName() {
        return MessageReceiverName;
    }

    /**
     * Setter method for property <tt>MessageReceiverName</tt>.
     */
    public void setMessageReceiverName(String messageReceiverName) {
        MessageReceiverName = messageReceiverName;
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
