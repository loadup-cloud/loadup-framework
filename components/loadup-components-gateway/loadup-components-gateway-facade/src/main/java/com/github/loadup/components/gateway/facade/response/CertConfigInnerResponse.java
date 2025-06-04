/* Copyright (C) LoadUp Cloud 2022-2025 */
package com.github.loadup.components.gateway.facade.response;

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
import java.util.Map;

/**
 *
 */
public class CertConfigInnerResponse extends BaseResponse {
    /**
     * Primary key (option)
     */
    private String certCode;

    /**
     * Required
     */
    private CertTypeEnum certType;

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
    private Map<String, String> properties;

    /**
     * Optional
     */
    private Map<String, String> algorithmProperties;

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
    private OperationType operateType;

    /**
     * Optional
     */
    private String algoName;

    private String keyType;

    public Map<String, String> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, String> properties) {
        this.properties = properties;
    }

    public String getKeyType() {
        return keyType;
    }

    public void setKeyType(String keyType) {
        this.keyType = keyType;
    }

    /**
     *
     */
    public String getCertCode() {
        return certCode;
    }

    /**
     *
     */
    public void setCertCode(String certCode) {
        this.certCode = certCode;
    }

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
     *
     *
     */
    /*public Map<String, String> getCertProperties() {
        return certProperties;
    }*/

    /**
     *
     *
     *
     */
    /*public void setCertProperties(Map<String, String> certProperties) {
        this.certProperties = certProperties;
    }*/

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
}
