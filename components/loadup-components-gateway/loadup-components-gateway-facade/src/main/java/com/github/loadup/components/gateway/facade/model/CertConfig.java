/* Copyright (C) LoadUp Cloud 2022-2025 */
package com.github.loadup.components.gateway.facade.model;

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

/**
 *
 */
public class CertConfig {
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
     *
     */
    public String getCertType() {
        return certType;
    }

    /**
     *
     */
    public void setCertType(String certType) {
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
    public String getProperties() {
        return properties;
    }

    /**
     *
     */
    public void setProperties(String properties) {
        this.properties = properties;
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
    public String getOperateType() {
        return operateType;
    }

    /**
     *
     */
    public void setOperateType(String operateType) {
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
