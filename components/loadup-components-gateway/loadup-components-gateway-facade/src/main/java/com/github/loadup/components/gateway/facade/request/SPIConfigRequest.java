package com.github.loadup.components.gateway.facade.request;

import jakarta.validation.constraints.NotBlank;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.util.Map;

/**
 *
 */
public class SPIConfigRequest extends BaseRequest {

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
     * tenant id
     */
    private String tenantId;

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
     * communication properties
     */
    private Map<String, String> communicationProperties;

    /**
     * spi version
     */
    private String version;

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
     * to string
     */
    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.JSON_STYLE);
    }
}