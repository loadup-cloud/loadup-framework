/* Copyright (C) LoadUp Cloud 2022-2025 */
package com.github.loadup.components.gateway.core.model;

/*-
 * #%L
 * loadup-components-gateway-core
 * %%
 * Copyright (C) 2022 - 2025 loadup_cloud
 * %%
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 * #L%
 */

import java.util.Map;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * 接口配置
 */
public class InterfaceConfig {

    /**
     * 接口属性
     */
    private final Properties properties = new Properties();
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
     *
     */
    public String getInterfaceId() {
        return interfaceId;
    }

    /**
     *
     */
    public void setInterfaceId(String interfaceId) {
        this.interfaceId = interfaceId;
    }

    /**
     *
     */
    public String getInterfaceName() {
        return interfaceName;
    }

    /**
     *
     */
    public void setInterfaceName(String interfaceName) {
        this.interfaceName = interfaceName;
    }

    /**
     *
     */
    public String getMessageProcessorId() {
        return messageProcessorId;
    }

    /**
     *
     */
    public void setMessageProcessorId(String messageProcessorId) {
        this.messageProcessorId = messageProcessorId;
    }

    /**
     *
     */
    public Properties getProperties() {
        return properties;
    }

    /**
     *
     */
    public void setProperties(String properties) {
        this.properties.setProperties(properties);
    }

    /**
     *
     */
    public void setPropertiesByMap(Map<String, String> properties) {
        this.properties.setProperties(properties);
    }

    /**
     *
     */
    public boolean isEnable() {
        return isEnable;
    }

    /**
     *
     */
    public void setEnable(boolean isEnable) {
        this.isEnable = isEnable;
    }

    /**
     *
     */
    public String getVersion() {
        return version;
    }

    /**
     *
     */
    public void setVersion(String version) {
        this.version = version;
    }

    /**
     *
     */
    public String getMessageReceiverInterfaceId() {
        return messageReceiverInterfaceId;
    }

    /**
     *
     */
    public void setMessageReceiverInterfaceId(String messageReceiverInterfaceId) {
        this.messageReceiverInterfaceId = messageReceiverInterfaceId;
    }

    /**
     *
     */
    public String getSecurityStrategyCode() {
        return securityStrategyCode;
    }

    /**
     *
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
