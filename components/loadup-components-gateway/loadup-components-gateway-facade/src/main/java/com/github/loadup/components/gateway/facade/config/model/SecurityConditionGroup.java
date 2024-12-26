package com.github.loadup.components.gateway.facade.config.model;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.io.Serializable;

/**
 * 网关密钥配置条件组
 */

public class SecurityConditionGroup implements Serializable {

    private static final long serialVersionUID = 8555071210094258364L;

    /**
     * security code
     */
    private String securityStrategyCode;

    /**
     * OP_SIGN, OP_VERIFY, etc. see OperationType.class
     */
    private String securityStrategyOperateType;

    /**
     * algorithm, like RSA
     */
    private String securityStrategyAlgorithm;

    /**
     * key type, like CERT_OFFICIAL_CONTENT, CERT_KMS_EXT, etc.
     */
    private String securityStrategyKeyType;

    /**
     * cert type, like PUBLICK_KEY, PRIVATE_KEY.
     */
    private String certType;

    /**
     * key content.
     */
    private String securityStrategyKey;

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
     * Getter method for property <tt>securityStrategyOperateType</tt>.
     */
    public String getSecurityStrategyOperateType() {
        return securityStrategyOperateType;
    }

    /**
     * Setter method for property <tt>securityStrategyOperateType</tt>.
     */
    public void setSecurityStrategyOperateType(String securityStrategyOperateType) {
        this.securityStrategyOperateType = securityStrategyOperateType;
    }

    /**
     * Getter method for property <tt>securityStrategyAlgorithm</tt>.
     */
    public String getSecurityStrategyAlgorithm() {
        return securityStrategyAlgorithm;
    }

    /**
     * Setter method for property <tt>securityStrategyAlgorithm</tt>.
     */
    public void setSecurityStrategyAlgorithm(String securityStrategyAlgorithm) {
        this.securityStrategyAlgorithm = securityStrategyAlgorithm;
    }

    /**
     * Getter method for property <tt>securityStrategyKeyType</tt>.
     */
    public String getSecurityStrategyKeyType() {
        return securityStrategyKeyType;
    }

    /**
     * Setter method for property <tt>securityStrategyKeyType</tt>.
     */
    public void setSecurityStrategyKeyType(String securityStrategyKeyType) {
        this.securityStrategyKeyType = securityStrategyKeyType;
    }

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
     * Getter method for property <tt>securityStrategyKey</tt>.
     */
    public String getSecurityStrategyKey() {
        return securityStrategyKey;
    }

    /**
     * Setter method for property <tt>securityStrategyKey</tt>.
     */
    public void setSecurityStrategyKey(String securityStrategyKey) {
        this.securityStrategyKey = securityStrategyKey;
    }

    /**
     * @see Object#toString()
     */
    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}