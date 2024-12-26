package com.github.loadup.components.gateway.core.model;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.util.*;

/**
 *
 */
public class InstConfig {

    /**
     * client id
     */
    private String clientId;

    /**
     * name
     */
    private String name;

    /**
     * properties
     */
    private final Properties properties = new Properties();

    /**
     * interface map
     */
    private Map<String, InterfaceConfig> interfaceMap = new HashMap<>();

    /**
     * gmt create
     */
    private Date gmtCreate;

    /**
     * gmt modified
     */
    private Date gmtModified;

    /**
     * Getter method for property <tt>clientId</tt>.
     */
    public String getClientId() {
        return clientId;
    }

    /**
     * Setter method for property <tt>clientId</tt>.
     */
    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    /**
     * Getter method for property <tt>name</tt>.
     */
    public String getName() {
        return name;
    }

    /**
     * Setter method for property <tt>name</tt>.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Getter method for property <tt>interfaceMap</tt>.
     */
    public Map<String, InterfaceConfig> getInterfaceMap() {
        return interfaceMap;
    }

    /**
     * Setter method for property <tt>interfaceMap</tt>.
     */
    public void setInterfaceMap(Map<String, InterfaceConfig> interfaceMap) {
        this.interfaceMap = interfaceMap;
    }

    /**
     * Getter method for property <tt>properties</tt>.
     */
    public Properties getProperties() {
        return properties;
    }

    /**
     * Getter method for property <tt>gmtCreate</tt>.
     */
    public Date getGmtCreate() {
        return gmtCreate;
    }

    /**
     * Setter method for property <tt>gmtCreate</tt>.
     */
    public void setGmtCreate(Date gmtCreate) {
        this.gmtCreate = gmtCreate;
    }

    /**
     * Getter method for property <tt>gmtModified</tt>.
     */
    public Date getGmtModified() {
        return gmtModified;
    }

    /**
     * Setter method for property <tt>gmtModified</tt>.
     */
    public void setGmtModified(Date gmtModified) {
        this.gmtModified = gmtModified;
    }

    /**
     * @see Object#toString()
     */
    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}