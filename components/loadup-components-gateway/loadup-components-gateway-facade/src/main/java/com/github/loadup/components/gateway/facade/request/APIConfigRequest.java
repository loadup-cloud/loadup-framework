/* Copyright (C) LoadUp Cloud 2022-2025 */
package com.github.loadup.components.gateway.facade.request;

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

import jakarta.validation.constraints.NotBlank;
import java.util.Map;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 *
 */
public class APIConfigRequest extends BaseRequest {

    /**
     * open url
     */
    @NotBlank(message = "url can not be blank.")
    private String url;

    /**
     * tenant id
     */
    private String tenantId;

    /**
     * <pre>
     * integration url, such as:
     * 1. http://domaneName/path
     * 2. https://domainName/path
     * 3. TR://domainName:12000/class/method
     * 4. SPRINGBEAN://class/method
     * 5. testRestful
     *
     * </pre>
     */
    @NotBlank(message = "integrationUrl can not be blank.")
    private String integrationUrl;

    /**
     * security strategy code
     */
    @NotBlank(message = "securityStrategyCode can not be blank.")
    private String securityStrategyCode;

    /**
     * integration service request header assemble content, velocity format.
     */
    @NotBlank(message = "integrationRequestHeaderAssemble can not be blank.")
    private String integrationRequestHeaderAssemble;

    /**
     * integration service request assemble content, velocity format.
     */
    @NotBlank(message = "integrationRequestBodyAssemble can not be blank.")
    private String integrationRequestBodyAssemble;

    /**
     * integration service response parser, groovy format.
     */
    @NotBlank(message = "integrationResponseParser can not be blank.")
    private String integrationResponseParser;

    /**
     * api version
     */
    private String version;

    /**
     * communication properties
     */
    private Map<String, String> communicationProperties;

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
    public Map<String, String> getCommunicationProperties() {
        return communicationProperties;
    }

    /**
     *
     */
    public void setCommunicationProperties(Map<String, String> communicationProperties) {
        this.communicationProperties = communicationProperties;
    }

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
     * to string
     */
    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.JSON_STYLE);
    }
}
