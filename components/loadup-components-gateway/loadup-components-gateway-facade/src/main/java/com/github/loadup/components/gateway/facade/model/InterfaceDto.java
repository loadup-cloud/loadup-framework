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

import java.util.Map;

/**
 *
 */
public class InterfaceDto {

    //========== properties ==========

    /**
     * tenant_id
     */
    private String tenantId;

    /**
     * interface_id
     */
    private String interfaceId;

    /**
     * interface_name
     */
    private String interfaceName;

    /**
     * url
     */
    private String url;

    /**
     * integration_url
     */
    private String integrationUrl;

    /**
     * security_strategy_code
     */
    private String securityStrategyCode;

    /**
     * version
     */
    private String version;

    /**
     * type
     */
    private String type;

    /**
     * status
     */
    private String status;

    /**
     * interface_request_parser
     */
    private String interfaceRequestParser;

    /**
     * integration_request_header_assemble
     */
    private String integrationRequestHeaderAssemble;

    /**
     * integration_request_body_assemble
     */
    private String integrationRequestBodyAssemble;

    /**
     * integration_response_parser
     */
    private String integrationResponseParser;

    /**
     * interface_response_header_assemble
     */
    private String interfaceResponseHeaderAssemble;

    /**
     * interface_response_body_assemble
     */
    private String interfaceResponseBodyAssemble;

    /**
     * communication properties
     */
    private Map<String, String> communicationProperties;

    /**
     * Getter method for property <tt>tenantId</tt>.
     */
    public String getTenantId() {
        return tenantId;
    }

    /**
     * Setter method for property <tt>tenantId</tt>.
     */
    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

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
     * Getter method for property <tt>url</tt>.
     */
    public String getUrl() {
        return url;
    }

    /**
     * Setter method for property <tt>url</tt>.
     */
    public void setUrl(String url) {
        this.url = url;
    }

    /**
     * Getter method for property <tt>integrationUrl</tt>.
     */
    public String getIntegrationUrl() {
        return integrationUrl;
    }

    /**
     * Setter method for property <tt>integrationUrl</tt>.
     */
    public void setIntegrationUrl(String integrationUrl) {
        this.integrationUrl = integrationUrl;
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
     * Getter method for property <tt>type</tt>.
     */
    public String getType() {
        return type;
    }

    /**
     * Setter method for property <tt>type</tt>.
     */
    public void setType(String type) {
        this.type = type;
    }

    /**
     * Getter method for property <tt>status</tt>.
     */
    public String getStatus() {
        return status;
    }

    /**
     * Setter method for property <tt>status</tt>.
     */
    public void setStatus(String status) {
        this.status = status;
    }

    /**
     * Getter method for property <tt>interfaceRequestParser</tt>.
     */
    public String getInterfaceRequestParser() {
        return interfaceRequestParser;
    }

    /**
     * Setter method for property <tt>interfaceRequestParser</tt>.
     */
    public void setInterfaceRequestParser(String interfaceRequestParser) {
        this.interfaceRequestParser = interfaceRequestParser;
    }

    /**
     * Getter method for property <tt>integrationRequestHeaderAssemble</tt>.
     */
    public String getIntegrationRequestHeaderAssemble() {
        return integrationRequestHeaderAssemble;
    }

    /**
     * Setter method for property <tt>integrationRequestHeaderAssemble</tt>.
     */
    public void setIntegrationRequestHeaderAssemble(String integrationRequestHeaderAssemble) {
        this.integrationRequestHeaderAssemble = integrationRequestHeaderAssemble;
    }

    /**
     * Getter method for property <tt>integrationRequestBodyAssemble</tt>.
     */
    public String getIntegrationRequestBodyAssemble() {
        return integrationRequestBodyAssemble;
    }

    /**
     * Setter method for property <tt>integrationRequestBodyAssemble</tt>.
     */
    public void setIntegrationRequestBodyAssemble(String integrationRequestBodyAssemble) {
        this.integrationRequestBodyAssemble = integrationRequestBodyAssemble;
    }

    /**
     * Getter method for property <tt>integrationResponseParser</tt>.
     */
    public String getIntegrationResponseParser() {
        return integrationResponseParser;
    }

    /**
     * Setter method for property <tt>integrationResponseParser</tt>.
     */
    public void setIntegrationResponseParser(String integrationResponseParser) {
        this.integrationResponseParser = integrationResponseParser;
    }

    /**
     * Getter method for property <tt>interfaceResponseHeaderAssemble</tt>.
     */
    public String getInterfaceResponseHeaderAssemble() {
        return interfaceResponseHeaderAssemble;
    }

    /**
     * Setter method for property <tt>interfaceResponseHeaderAssemble</tt>.
     */
    public void setInterfaceResponseHeaderAssemble(String interfaceResponseHeaderAssemble) {
        this.interfaceResponseHeaderAssemble = interfaceResponseHeaderAssemble;
    }

    /**
     * Getter method for property <tt>interfaceResponseBodyAssemble</tt>.
     */
    public String getInterfaceResponseBodyAssemble() {
        return interfaceResponseBodyAssemble;
    }

    /**
     * Setter method for property <tt>interfaceResponseBodyAssemble</tt>.
     */
    public void setInterfaceResponseBodyAssemble(String interfaceResponseBodyAssemble) {
        this.interfaceResponseBodyAssemble = interfaceResponseBodyAssemble;
    }

    /**
     * Getter method for property <tt>communicationProperties</tt>.
     */
    public Map<String, String> getCommunicationProperties() {
        return communicationProperties;
    }

    /**
     * Setter method for property <tt>communicationProperties</tt>.
     */
    public void setCommunicationProperties(Map<String, String> communicationProperties) {
        this.communicationProperties = communicationProperties;
    }
}
