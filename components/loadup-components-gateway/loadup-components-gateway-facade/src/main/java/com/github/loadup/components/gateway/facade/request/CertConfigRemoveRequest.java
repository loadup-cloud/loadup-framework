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

import com.github.loadup.components.gateway.facade.enums.OperationType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

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
