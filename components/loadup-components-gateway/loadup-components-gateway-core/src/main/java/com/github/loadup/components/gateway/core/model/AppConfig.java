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

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * 应用配置
 */
public class AppConfig {

    /**
     * field property
     **/
    private final Properties properties = new Properties();
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
     *
     */
    public String getInstId() {
        return instId;
    }

    /**
     *
     */
    public void setInstId(String instId) {
        this.instId = instId;
    }

    /**
     *
     */
    public String getTntInstId() {
        return tntInstId;
    }

    /**
     *
     */
    public void setTntInstId(String tntInstId) {
        this.tntInstId = tntInstId;
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
    public String getAppName() {
        return appName;
    }

    /**
     *
     */
    public void setAppName(String appName) {
        this.appName = appName;
    }

    /**
     *
     */
    public String getCertCode() {
        return certCode;
    }

    /**
     *
     */
    public void setCertCode(String certCode) {
        this.certCode = certCode;
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
     * toString
     */
    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.JSON_STYLE);
    }
}
