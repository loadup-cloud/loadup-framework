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

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * 应用和接口关系映射
 */
public class AppInterfaceMap {

    /**
     * 接口id
     */
    private String interfaceId;

    /**
     * 应用id
     */
    private String appId;

    /**
     * 通讯配置id
     */
    private String communicationId;

    /**
     * 前置路由id
     */
    private String routeId;

    /**
     * 业务参数属性
     */
    private Properties bizProperties = new Properties();

    /**
     * 扩展参数属性
     */
    private Properties extProperties = new Properties();

    /**
     * 租户ID
     */
    private String tntInstId;

    /**
     * 是否启用
     */
    private boolean enable;

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
    public String getAppId() {
        return appId;
    }

    /**
     * 
     */
    public void setAppId(String appId) {
        this.appId = appId;
    }

    /**
     *
     */
    public String getCommunicationId() {
        return communicationId;
    }

    /**
     * 
     */
    public void setCommunicationId(String communicationId) {
        this.communicationId = communicationId;
    }

    /**
     *
     */
    public String getRouteId() {
        return routeId;
    }

    /**
     * 
     */
    public void setRouteId(String routeId) {
        this.routeId = routeId;
    }

    /**
     *
     */
    public boolean isEnable() {
        return enable;
    }

    /**
     * 
     */
    public void setEnable(boolean enable) {
        this.enable = enable;
    }

    /**
     *
     */
    public Properties getBizProperties() {
        return bizProperties;
    }

    /**
     * 
     */
    public void setBizProperties(String bizProperties) {
        this.bizProperties.setProperties(bizProperties);
    }

    /**
     *
     */
    public Properties getExtProperties() {
        return extProperties;
    }

    /**
     * 
     */
    public void setExtProperties(String extProperties) {
        this.extProperties.setProperties(extProperties);
    }

    /**
     *
     */
    public String getTntInstId() {
        return StringUtils.defaultIfBlank(tntInstId, "");
    }

    /**
     * 
     */
    public void setTntInstId(String tntInstId) {
        this.tntInstId = tntInstId;
    }

    /**
     * toString
     */
    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.JSON_STYLE);
    }
}
