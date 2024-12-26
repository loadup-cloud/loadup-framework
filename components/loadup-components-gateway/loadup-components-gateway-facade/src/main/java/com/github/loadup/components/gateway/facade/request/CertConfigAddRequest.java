package com.github.loadup.components.gateway.facade.request;

import com.github.loadup.components.gateway.facade.enums.CertTypeEnum;
import com.github.loadup.components.gateway.facade.enums.OperationType;
import jakarta.validation.constraints.*;

import java.util.Map;

/**
 *
 */

public class CertConfigAddRequest extends BaseRequest {

    /**
     * Optional
     */
    private CertTypeEnum certType;

    /**
     * Required
     */
    @Size(max = 4096, message = "certContent's length should be not bigger than 4096.")
    private String certContent;

    /**
     * Required
     */
    @Size(max = 32, message = "certStatus's length should be not bigger than 32.")
    private String certStatus;

    /**
     * Optional
     */
    private Map<String, String> properties;

    /**
     * Optional
     */
    private Map<String, String> algorithmProperties;

    /**
     * securityStrategyCode
     */
    @NotBlank(message = "securityStrategyCode can not be blank")
    @Size(max = 64, message = "securityStrategyCode's length should be not bigger than 64.")
    private String securityStrategyCode;

    /**
     *
     */
    @NotBlank(message = "clientId can not be blank")
    @Size(max = 64, message = "clientId's length should be not bigger than 64.")
    private String clientId;

    /**
     * Optional
     */
    @NotNull(message = "operateType must be not null")
    private OperationType operateType;

    /**
     * Optional
     */
    @NotBlank(message = "algoName can not be blank")
    @Size(max = 64, message = "algoName's length should be not bigger than 64.")
    private String algoName;
    /**
     *
     */
    @Size(max = 32, message = "algoName's length should be not bigger than 32.")
    private String keyType;

    /**
     * Getter method for property <tt>certType</tt>.
     */
    public CertTypeEnum getCertType() {
        return certType;
    }

    /**
     * Setter method for property <tt>certType</tt>.
     */
    public void setCertType(CertTypeEnum certType) {
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
     * Getter method for property <tt>certStatus</tt>.
     */
    public String getCertStatus() {
        return certStatus;
    }

    /**
     * Setter method for property <tt>certStatus</tt>.
     */
    public void setCertStatus(String certStatus) {
        this.certStatus = certStatus;
    }

    /**
     * Getter method for property <tt>properties</tt>.
     */
    public Map<String, String> getProperties() {
        return properties;
    }

    /**
     * Setter method for property <tt>properties</tt>.
     */
    public void setProperties(Map<String, String> properties) {
        this.properties = properties;
    }

    /**
     * Getter method for property <tt>algorithmProperties</tt>.
     */
    public Map<String, String> getAlgorithmProperties() {
        return algorithmProperties;
    }

    /**
     * Setter method for property <tt>algorithmProperties</tt>.
     */
    public void setAlgorithmProperties(Map<String, String> algorithmProperties) {
        this.algorithmProperties = algorithmProperties;
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
    public OperationType getOperateType() {
        return operateType;
    }

    /**
     * Setter method for property <tt>operateType</tt>.
     */
    public void setOperateType(OperationType operateType) {
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
     *
     */
    public String getKeyType() {
        return keyType;
    }

    /**
     *
     */
    public void setKeyType(String keyType) {
        this.keyType = keyType;
    }
}