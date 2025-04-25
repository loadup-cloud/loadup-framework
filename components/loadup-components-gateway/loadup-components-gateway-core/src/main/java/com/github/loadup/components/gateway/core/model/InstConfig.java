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

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 *
 */
public class InstConfig {

    /**
     * properties
     */
    private final Properties properties = new Properties();
    /**
     * client id
     */
    private String clientId;
    /**
     * name
     */
    private String name;
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
     *
     */
    public String getClientId() {
        return clientId;
    }

    /**
     * 
     */
    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    /**
     *
     */
    public String getName() {
        return name;
    }

    /**
     * 
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     *
     */
    public Map<String, InterfaceConfig> getInterfaceMap() {
        return interfaceMap;
    }

    /**
     * 
     */
    public void setInterfaceMap(Map<String, InterfaceConfig> interfaceMap) {
        this.interfaceMap = interfaceMap;
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
    public Date getGmtCreate() {
        return gmtCreate;
    }

    /**
     * 
     */
    public void setGmtCreate(Date gmtCreate) {
        this.gmtCreate = gmtCreate;
    }

    /**
     *
     */
    public Date getGmtModified() {
        return gmtModified;
    }

    /**
     * 
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
