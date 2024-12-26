package com.github.loadup.components.gateway.facade.request;

import com.github.loadup.components.gateway.facade.enums.OperationType;
import jakarta.validation.constraints.*;

/**
 *
 */

public class CertConfigRemoveRequest extends BaseRequest {

    @NotBlank(message = "clientId can not be blank")
    @Size(max = 64, message = "clientId's length should be not bigger than 64.")
    private String clientId;

    @NotBlank(message = "securityStrategyCode can not be blank")
    @Size(max = 64, message = "securityStrategyCode's length should be not bigger than 64.")
    private String securityStrategyCode;

    @NotBlank(message = "algoName can not be blank")
    @Size(max = 64, message = "algoName's length should be not bigger than 64.")
    private String algoName;

    @NotNull(message = "operateType must be not null")
    private OperationType operateType;

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
}