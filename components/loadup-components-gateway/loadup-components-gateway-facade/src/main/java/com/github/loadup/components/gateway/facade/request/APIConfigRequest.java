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
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.util.Map;

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
	 * to string
	 */
	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.JSON_STYLE);
	}
}
