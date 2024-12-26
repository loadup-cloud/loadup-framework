package com.github.loadup.components.gateway.core.model;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * 日志敏感字段配置
 */
public class ShieldTypeConfig {

    /**
     * interface id
     */
    private String interfaceId;

    /**
     * key
     */
    private String key;

    /**
     * {@link ShieldType}
     */
    private ShieldType shieldType;

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
     * Getter method for property <tt>key</tt>.
     */
    public String getKey() {
        return key;
    }

    /**
     * Setter method for property <tt>key</tt>.
     */
    public void setKey(String key) {
        this.key = key;
    }

    /**
     * Getter method for property <tt>shieldType</tt>.
     */
    public ShieldType getShieldType() {
        return shieldType;
    }

    /**
     * Setter method for property <tt>shieldType</tt>.
     */
    public void setShieldType(ShieldType shieldType) {
        this.shieldType = shieldType;
    }

    /**
     * @see Object#toString()
     */
    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}