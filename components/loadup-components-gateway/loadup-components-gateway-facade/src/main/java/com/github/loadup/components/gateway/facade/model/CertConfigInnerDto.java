package com.github.loadup.components.gateway.facade.model;

/**
 *
 */
public class CertConfigInnerDto {
    /**
     * Required
     */
    private String certType;

    /**
     * Required
     */
    private String certContent;

    /**
     * Optional
     */
    private String properties;

    /**
     * securityStrategyCode
     */
    private String securityStrategyCode;

    /**
     * clientId
     */
    private String clientId;
    /**
     * Optional
     */
    private String operateType;

    /**
     * Optional
     */
    private String algoName;
    /**
     * keyType
     */
    private String keyType;

    /**
     * Getter method for property <tt>certType</tt>.
     */
    public String getCertType() {
        return certType;
    }

    /**
     * Setter method for property <tt>certType</tt>.
     */
    public void setCertType(String certType) {
        this.certType = certType;
    }

    /**
     * Getter method for property <tt>certContent</tt>.
     */
    public String getCertContent() {
        return certContent;
    }

    /**
     * Setter method for property <tt>certContent</tt>.
     */
    public void setCertContent(String certContent) {
        this.certContent = certContent;
    }

    /**
     * Getter method for property <tt>properties</tt>.
     */
    public String getProperties() {
        return properties;
    }

    /**
     * Setter method for property <tt>properties</tt>.
     */
    public void setProperties(String properties) {
        this.properties = properties;
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
     * Getter method for property <tt>clientId</tt>.
     */
    public String getClientId() {
        return clientId;
    }

    /**
     * Setter method for property <tt>clientId</tt>.
     */
    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    /**
     * Getter method for property <tt>operateType</tt>.
     */
    public String getOperateType() {
        return operateType;
    }

    /**
     * Setter method for property <tt>operateType</tt>.
     */
    public void setOperateType(String operateType) {
        this.operateType = operateType;
    }

    /**
     * Getter method for property <tt>algoName</tt>.
     */
    public String getAlgoName() {
        return algoName;
    }

    /**
     * Setter method for property <tt>algoName</tt>.
     */
    public void setAlgoName(String algoName) {
        this.algoName = algoName;
    }

    /**
     * Getter method for property <tt>keyType</tt>.
     */
    public String getKeyType() {
        return keyType;
    }

    /**
     * Setter method for property <tt>keyType</tt>.
     */
    public void setKeyType(String keyType) {
        this.keyType = keyType;
    }
}