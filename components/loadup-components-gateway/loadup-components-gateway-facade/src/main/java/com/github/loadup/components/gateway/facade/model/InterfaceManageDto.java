package com.github.loadup.components.gateway.facade.model;

/*-
 * #%L
 * loadup-components-gateway-facade
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

import java.util.Date;

/**
 * <p>
 * InterfaceConfig.java
 * </p>
 */
public class InterfaceManageDto {

    //========== properties ==========

    /**
     * This property corresponds to db column <tt>tenant_id</tt>.
     */
    private String tenantId;

    /**
     * This property corresponds to db column <tt>interface_id</tt>.
     */
    private String interfaceId;

    /**
     * This property corresponds to db column <tt>interface_name</tt>.
     */
    private String interfaceName;

    /**
     * This property corresponds to db column <tt>url</tt>.
     */
    private String url;

    /**
     * This property corresponds to db column <tt>integration_url</tt>.
     */
    private String integrationUrl;

    /**
     * This property corresponds to db column <tt>security_strategy_code</tt>.
     */
    private String securityStrategyCode;

    /**
     * This property corresponds to db column <tt>version</tt>.
     */
    private String version;

    /**
     * This property corresponds to db column <tt>type</tt>.
     */
    private String type;

    /**
     * This property corresponds to db column <tt>status</tt>.
     */
    private String status;

    /**
     * This property corresponds to db column <tt>interface_request_parser</tt>.
     */
    private String interfaceRequestParser;

    /**
     * This property corresponds to db column <tt>integration_request_header_assemble</tt>.
     */
    private String integrationRequestHeaderAssemble;

    /**
     * This property corresponds to db column <tt>integration_request_body_assemble</tt>.
     */
    private String integrationRequestBodyAssemble;

    /**
     * This property corresponds to db column <tt>integration_response_parser</tt>.
     */
    private String integrationResponseParser;

    /**
     * This property corresponds to db column <tt>interface_response_header_assemble</tt>.
     */
    private String interfaceResponseHeaderAssemble;

    private String interfaterceRequestParser;

    /**
     * This property corresponds to db column <tt>interface_response_body_assemble</tt>.
     */
    private String interfaceResponseBodyAssemble;

    /**
     * This property corresponds to db column <tt>communication_properties</tt>.
     */
    private String communicationProperties;

    /**
     * This property corresponds to db column <tt>gmt_create</tt>.
     */
    private Date gmtCreate;

    /**
     * This property corresponds to db column <tt>gmt_modified</tt>.
     */
    private Date gmtModified;

    //========== getters and setters ==========

    /**
     *
     */
    public String getTenantId() {
        return tenantId;
    }

    /**
     * 
     */
    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

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
    public String getUrl() {
        return url;
    }

    /**
     * 
     */
    public void setUrl(String url) {
        this.url = url;
    }

    /**
     *
     */
    public String getIntegrationUrl() {
        return integrationUrl;
    }

    /**
     * 
     */
    public void setIntegrationUrl(String integrationUrl) {
        this.integrationUrl = integrationUrl;
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
    public String getType() {
        return type;
    }

    /**
     * 
     */
    public void setType(String type) {
        this.type = type;
    }

    /**
     *
     */
    public String getStatus() {
        return status;
    }

    /**
     * 
     */
    public void setStatus(String status) {
        this.status = status;
    }

    /**
     *
     */
    public String getInterfaceRequestParser() {
        return interfaceRequestParser;
    }

    /**
     * 
     */
    public void setInterfaceRequestParser(String interfaceRequestParser) {
        this.interfaceRequestParser = interfaceRequestParser;
    }

    /**
     *
     */
    public String getIntegrationRequestHeaderAssemble() {
        return integrationRequestHeaderAssemble;
    }

    /**
     * 
     */
    public void setIntegrationRequestHeaderAssemble(String integrationRequestHeaderAssemble) {
        this.integrationRequestHeaderAssemble = integrationRequestHeaderAssemble;
    }

    /**
     *
     */
    public String getIntegrationRequestBodyAssemble() {
        return integrationRequestBodyAssemble;
    }

    /**
     * 
     */
    public void setIntegrationRequestBodyAssemble(String integrationRequestBodyAssemble) {
        this.integrationRequestBodyAssemble = integrationRequestBodyAssemble;
    }

    /**
     *
     */
    public String getIntegrationResponseParser() {
        return integrationResponseParser;
    }

    /**
     * 
     */
    public void setIntegrationResponseParser(String integrationResponseParser) {
        this.integrationResponseParser = integrationResponseParser;
    }

    /**
     *
     */
    public String getInterfaceResponseHeaderAssemble() {
        return interfaceResponseHeaderAssemble;
    }

    /**
     * 
     */
    public void setInterfaceResponseHeaderAssemble(String interfaceResponseHeaderAssemble) {
        this.interfaceResponseHeaderAssemble = interfaceResponseHeaderAssemble;
    }

    /**
     *
     */
    public String getInterfaceResponseBodyAssemble() {
        return interfaceResponseBodyAssemble;
    }

    /**
     * 
     */
    public void setInterfaceResponseBodyAssemble(String interfaceResponseBodyAssemble) {
        this.interfaceResponseBodyAssemble = interfaceResponseBodyAssemble;
    }

    /**
     *
     */
    public String getCommunicationProperties() {
        return communicationProperties;
    }

    /**
     * 
     */
    public void setCommunicationProperties(String communicationProperties) {
        this.communicationProperties = communicationProperties;
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

    public String getInterfaterceRequestParser() {
        return interfaterceRequestParser;
    }

    public void setInterfaterceRequestParser(String interfaterceRequestParser) {
        this.interfaterceRequestParser = interfaterceRequestParser;
    }

    /**
     * @see Object#toString()
     */
    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}
