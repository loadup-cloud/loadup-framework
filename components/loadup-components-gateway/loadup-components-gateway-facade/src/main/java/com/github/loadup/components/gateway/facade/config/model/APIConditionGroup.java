package com.github.loadup.components.gateway.facade.config.model;

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

import java.io.Serializable;

/**
 * 网关API配置条件组
 */

public class APIConditionGroup implements Serializable {

    private static final long serialVersionUID = -4878291431746547062L;

    /**
     * sender url
     */
    private String url;

    /**
     * interface name configured in product center used by other components
     */
    private String interfaceName;

    /**
     * receiver url
     */
    private String integrationUrl;

    /**
     * security strategy code
     */
    private String securityStrategyCode;

    /**
     * sender request parser, groovy format.
     */
    private String interfaceRequestParser;

    /**
     * integration service request header assemble content, velocity format.
     */
    private String integrationRequestHeaderAssemble;

    /**
     * integration service request assemble content, velocity format.
     */
    private String integrationRequestBodyAssemble;

    /**
     * integration service response parser, groovy format.
     */
    private String integrationResponseParser;

    /**
     * sender response response header assemble content, velocity format.
     */
    private String interfaceResponseHeaderAssemble;

    /**
     * sender response response body assemble content, velocity format.
     */
    private String interfaceResponseBodyAssemble;

    /**
     * limit connections for this OPENAPI
     */
    private Integer limitConn;

    /**
     * communication properties
     */
    private String communicationProperties;

    /**
     * http status except 200 map to error code.
     * in format
     */
    private String httpStatusToErrorCode;

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
     * Getter method for property <tt>limitConn</tt>.
     */
    public Integer getLimitConn() {
        return limitConn;
    }

    /**
     * Setter method for property <tt>limitConn</tt>.
     */
    public void setLimitConn(Integer limitConn) {
        this.limitConn = limitConn;
    }

    /**
     * Getter method for property <tt>communicationProperties</tt>.
     */
    public String getCommunicationProperties() {
        return communicationProperties;
    }

    /**
     * Setter method for property <tt>communicationProperties</tt>.
     */
    public void setCommunicationProperties(String communicationProperties) {
        this.communicationProperties = communicationProperties;
    }

    /**
     * Getter method for property <tt>httpStatusToErrorCode</tt>.
     */
    public String getHttpStatusToErrorCode() {
        return httpStatusToErrorCode;
    }

    /**
     * Setter method for property <tt>httpStatusToErrorCode</tt>.
     */
    public void setHttpStatusToErrorCode(String httpStatusToErrorCode) {
        this.httpStatusToErrorCode = httpStatusToErrorCode;
    }

    /**
     * @see Object#toString()
     */
    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}
