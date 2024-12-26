package com.github.loadup.components.gateway.facade.model;

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
     * Getter method for property <tt>gmtCreate</tt>.
     */
    public Date getGmtCreate() {
        return gmtCreate;
    }

    /**
     * Setter method for property <tt>gmtCreate</tt>.
     */
    public void setGmtCreate(Date gmtCreate) {
        this.gmtCreate = gmtCreate;
    }

    /**
     * Getter method for property <tt>gmtModified</tt>.
     */
    public Date getGmtModified() {
        return gmtModified;
    }

    /**
     * Setter method for property <tt>gmtModified</tt>.
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