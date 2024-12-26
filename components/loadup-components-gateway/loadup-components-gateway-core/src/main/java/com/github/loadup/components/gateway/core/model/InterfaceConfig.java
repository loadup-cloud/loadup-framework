package com.github.loadup.components.gateway.core.model;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.util.Map;

/**
 * 接口配置
 */
public class InterfaceConfig {

    /**
     * 接口id
     */
    private String interfaceId;

    /**
     * 接口名称
     */
    private String interfaceName;

    /**
     * 报文处理id
     */
    private String messageProcessorId;

    /**
     * 集成ID
     */
    private String messageReceiverInterfaceId;

    /**
     * 接口属性
     */
    private final Properties properties = new Properties();

    /**
     * 接口版本号
     */
    private String version;

    /**
     * 安全信息号
     */
    private String securityStrategyCode;

    /**
     * 是否开启
     */
    private boolean isEnable;

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
     * Getter method for property <tt>interfaceName</tt>.
     */
    public String getInterfaceName() {
        return interfaceName;
    }

    /**
     * Setter method for property <tt>interfaceName</tt>.
     */
    public void setInterfaceName(String interfaceName) {
        this.interfaceName = interfaceName;
    }

    /**
     * Getter method for property <tt>messageProcessorId</tt>.
     */
    public String getMessageProcessorId() {
        return messageProcessorId;
    }

    /**
     * Setter method for property <tt>messageProcessorId</tt>.
     */
    public void setMessageProcessorId(String messageProcessorId) {
        this.messageProcessorId = messageProcessorId;
    }

    /**
     * Getter method for property <tt>propertes</tt>.
     */
    public Properties getProperties() {
        return properties;
    }

    /**
     * Setter method for property <tt>propertes</tt>.
     */
    public void setProperties(String properties) {
        this.properties.setProperties(properties);
    }

    /**
     * Setter method for property <tt>propertes</tt>.
     */
    public void setPropertiesByMap(Map<String, String> properties) {
        this.properties.setProperties(properties);
    }

    /**
     * Getter method for property <tt>isEnable</tt>.
     */
    public boolean isEnable() {
        return isEnable;
    }

    /**
     * Setter method for property <tt>isEnable</tt>.
     */
    public void setEnable(boolean isEnable) {
        this.isEnable = isEnable;
    }

    /**
     * Getter method for property <tt>version</tt>.
     */
    public String getVersion() {
        return version;
    }

    /**
     * Setter method for property <tt>version</tt>.
     */
    public void setVersion(String version) {
        this.version = version;
    }

    /**
     * Getter method for property <tt>messageReceiverInterfaceId</tt>.
     */
    public String getMessageReceiverInterfaceId() {
        return messageReceiverInterfaceId;
    }

    /**
     * Setter method for property <tt>messageReceiverInterfaceId</tt>.
     */
    public void setMessageReceiverInterfaceId(String messageReceiverInterfaceId) {
        this.messageReceiverInterfaceId = messageReceiverInterfaceId;
    }

    /**
     * Getter method for property <tt>securityStrategyCode</tt>.
     */
    public String getSecurityStrategyCode() {
        return securityStrategyCode;
    }

    /**
     * Setter method for property <tt>securityStrategyCode</tt>.
     */
    public void setSecurityStrategyCode(String securityStrategyCode) {
        this.securityStrategyCode = securityStrategyCode;
    }

    /**
     * @see Object#toString()
     */
    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}
