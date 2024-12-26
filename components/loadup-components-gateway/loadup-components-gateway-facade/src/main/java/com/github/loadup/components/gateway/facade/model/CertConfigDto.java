package com.github.loadup.components.gateway.facade.model;

import java.util.Date;

/**
 * <p>
 * CertConfig.java
 * </p>
 */
public class CertConfigDto {

    /**
     * Primary key (option)
     */
    private String certCode;

    /**
     * Required
     */
    private String certType;

    /**
     * Required
     */
    private String certContent;

    /**
     * Required
     */
    private String certStatus;

    /**
     * Optional
     */
    private Date gmtValid;

    /**
     * Optional
     */
    private Date gmtInValid;

    /**
     * Optional
     */
    private String properties;

    /**
     * securityStrategyCode
     */
    private String securityStrategyCode;

    /**
     *
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
     * Gets get cert content.
     */
    public String getCertContent() {
        return certContent;
    }

    /**
     * Sets set cert content.
     */
    public void setCertContent(String certContent) {
        this.certContent = certContent;
    }

    /**
     * Gets get cert status.
     */
    public String getCertStatus() {
        return certStatus;
    }

    /**
     * Sets set cert status.
     */
    public void setCertStatus(String certStatus) {
        this.certStatus = certStatus;
    }

    /**
     * Gets get gmt valid.
     */
    public Date getGmtValid() {
        return gmtValid;
    }

    /**
     * Sets set gmt valid.
     */
    public void setGmtValid(Date gmtValid) {
        this.gmtValid = gmtValid;
    }

    /**
     * Gets get gmt in valid.
     */
    public Date getGmtInValid() {
        return gmtInValid;
    }

    /**
     * Sets set gmt in valid.
     */
    public void setGmtInValid(Date gmtInValid) {
        this.gmtInValid = gmtInValid;
    }

    /**
     * Gets get properties.
     */
    public String getProperties() {
        return properties;
    }

    /**
     * Sets set properties.
     */
    public void setProperties(String properties) {
        this.properties = properties;
    }

    /**
     * get security
     */
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

    /**
     * get operate type
     */
    public String getOperateType() {
        return operateType;
    }

    /**
     * set operate type
     */
    public void setOperateType(String operateType) {
        this.operateType = operateType;
    }

    /**
     * get algo name
     */
    public String getAlgoName() {
        return algoName;
    }

    /**
     * set algo name
     */
    public void setAlgoName(String algoName) {
        this.algoName = algoName;
    }
}