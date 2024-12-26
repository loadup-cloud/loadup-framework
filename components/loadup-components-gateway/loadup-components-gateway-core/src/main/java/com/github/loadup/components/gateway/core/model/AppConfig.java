package com.github.loadup.components.gateway.core.model;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * 应用配置
 */
public class AppConfig {

    /**
     * 应用id
     */
    private String appId;

    /**
     * 应用名
     */
    private String appName;

    /**
     * 安全组件Code
     */
    private String certCode;

    private String instId;

    private String tntInstId;

    /**
     * Getter method for property <tt>instId</tt>.
     */
    public String getInstId() {
        return instId;
    }

    /**
     * Setter method for property <tt>instId</tt>.
     */
    public void setInstId(String instId) {
        this.instId = instId;
    }

    /**
     * Getter method for property <tt>tntInstId</tt>.
     */
    public String getTntInstId() {
        return tntInstId;
    }

    /**
     * Setter method for property <tt>tntInstId</tt>.
     */
    public void setTntInstId(String tntInstId) {
        this.tntInstId = tntInstId;
    }

    /**
     * field property
     **/
    private final Properties properties = new Properties();

    /**
     * Getter method for property <tt>appId</tt>.
     */
    public String getAppId() {
        return appId;
    }

    /**
     * Setter method for property <tt>appId</tt>.
     */
    public void setAppId(String appId) {
        this.appId = appId;
    }

    /**
     * Getter method for property <tt>appName</tt>.
     */
    public String getAppName() {
        return appName;
    }

    /**
     * Setter method for property <tt>appName</tt>.
     */
    public void setAppName(String appName) {
        this.appName = appName;
    }

    /**
     * Getter method for property <tt>certCode<tt>.
     */
    public String getCertCode() {
        return certCode;
    }

    /**
     * Setter method for property <tt>certCode<tt>.
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
    public void setProperties(String properties) {
        this.properties.setProperties(properties);
    }

    /**
     * toString
     */
    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.JSON_STYLE);
    }
}
