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

import com.github.loadup.components.gateway.facade.enums.CertTypeEnum;
import com.github.loadup.components.gateway.facade.enums.OperationType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

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
     * 
     */
    public CertTypeEnum getCertType() {
        return certType;
    }

    /**
     * 
     */
    public void setCertType(CertTypeEnum certType) {
        this.certType = certType;
    }

    /**
     * 
     */
    public String getCertContent() {
        return certContent;
    }

    /**
     * 
     */
    public void setCertContent(String certContent) {
        this.certContent = certContent;
    }

    /**
     * 
     */
    public String getCertStatus() {
        return certStatus;
    }

    /**
     * 
     */
    public void setCertStatus(String certStatus) {
        this.certStatus = certStatus;
    }

    /**
     * 
     */
    public Map<String, String> getProperties() {
        return properties;
    }

    /**
     * 
     */
    public void setProperties(Map<String, String> properties) {
        this.properties = properties;
    }

    /**
     * 
     */
    public Map<String, String> getAlgorithmProperties() {
        return algorithmProperties;
    }

    /**
     * 
     */
    public void setAlgorithmProperties(Map<String, String> algorithmProperties) {
        this.algorithmProperties = algorithmProperties;
    }

    /**
     * 
     */
    public String getSecurityStrategyCode() {
        return securityStrategyCode;
    }

    /**
     * 
     */
    public void setSecurityStrategyCode(String securityStrategyCode) {
        this.securityStrategyCode = securityStrategyCode;
    }

    /**
     * 
     */
    public String getClientId() {
        return clientId;
    }

    /**
     * 
     */
    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    /**
     * 
     */
    public OperationType getOperateType() {
        return operateType;
    }

    /**
     * 
     */
    public void setOperateType(OperationType operateType) {
        this.operateType = operateType;
    }

    /**
     * 
     */
    public String getAlgoName() {
        return algoName;
    }

    /**
     * 
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
