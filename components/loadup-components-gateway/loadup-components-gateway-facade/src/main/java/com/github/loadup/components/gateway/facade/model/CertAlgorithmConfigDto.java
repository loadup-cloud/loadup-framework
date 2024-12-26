package com.github.loadup.components.gateway.facade.model;

/**
 * <p>
 * CertAlgorithmConfig.java
 * </p>
 */
public class CertAlgorithmConfigDto {

    /**
     * Primary key(optionaal)
     */
    private String certCode;

    /**
     * Required
     */
    private String operateType;

    /**
     * Required
     */
    private String algoName;

    /**
     * Required
     */
    private String certType;

    /**
     * Optional
     */
    private String algoProperties;

    /**
     * Required
     */
    private String algoType;

    /**
     * securityStrategyCode
     */
    private String securityStrategyCode;

    /**
     *
     */
    private String clientId;

    /**
     * Gets get cert code.
     */
    public String getCertCode() {

        return certCode;
    }

    /**
     * Sets set cert code.
     */
    public void setCertCode(String certCode) {
        this.certCode = certCode;
    }

    /**
     * Gets get operate type.
     */
    public String getOperateType() {
        return operateType;
    }

    /**
     * Sets set operate type.
     */
    public void setOperateType(String operateType) {
        this.operateType = operateType;
    }

    /**
     * Gets get algo name.
     */
    public String getAlgoName() {
        return algoName;
    }

    /**
     * Sets set algo name.
     */
    public void setAlgoName(String algoName) {
        this.algoName = algoName;
    }

    /**
     * Gets get cert type.
     */
    public String getCertType() {
        return certType;
    }

    /**
     * Sets set cert type.
     */
    public void setCertType(String certType) {
        this.certType = certType;
    }

    /**
     * Gets get algo properties.
     */
    public String getAlgoProperties() {
        return algoProperties;
    }

    /**
     * Sets set algo properties.
     */
    public void setAlgoProperties(String algoProperties) {
        this.algoProperties = algoProperties;
    }

    /**
     * Gets get algo type.
     */
    public String getAlgoType() {
        return algoType;
    }

    /**
     * Sets set algo type.
     */
    public void setAlgoType(String algoType) {
        this.algoType = algoType;
    }

    public String getSecurityStrategyCode() {
        return securityStrategyCode;
    }

    /**
     * set security strategy code
     */
    public void setSecurityStrategyCode(String securityStrategyCode) {
        this.securityStrategyCode = securityStrategyCode;
    }

    /**
     * get client id
     */
    public String getClientId() {
        return clientId;
    }

    /**
     * set client id
     */
    public void setClientId(String clientId) {
        this.clientId = clientId;
    }
}