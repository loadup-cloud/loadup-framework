package com.github.loadup.components.gateway.facade.response;

import com.github.loadup.components.gateway.facade.enums.OperationType;

/**
 *
 */
public class CertConfigAddResponse extends BaseResponse {
    /**
     * clientId
     */
    private String        clientId;
    /**
     * securityStrategyCode
     */
    private String        securityStrategyCode;
    /**
     * algoName
     */
    private String        algoName;
    /**
     * operationType
     */
    private OperationType operationType;

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
     * Getter method for property <tt>operationType</tt>.
     */
    public OperationType getOperationType() {
        return operationType;
    }

    /**
     * Setter method for property <tt>operationType</tt>.
     */
    public void setOperationType(OperationType operationType) {
        this.operationType = operationType;
    }
}