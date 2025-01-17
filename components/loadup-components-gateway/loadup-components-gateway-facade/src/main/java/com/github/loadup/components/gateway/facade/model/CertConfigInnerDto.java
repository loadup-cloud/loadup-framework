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
public class CertConfigInnerDto {
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
     * Getter method for property <tt>properties</tt>.
     */
    public String getProperties() {
        return properties;
    }

    /**
     * Setter method for property <tt>properties</tt>.
     */
    public void setProperties(String properties) {
        this.properties = properties;
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
    public String getOperateType() {
        return operateType;
    }

    /**
     * Setter method for property <tt>operateType</tt>.
     */
    public void setOperateType(String operateType) {
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
     * Getter method for property <tt>keyType</tt>.
     */
    public String getKeyType() {
        return keyType;
    }

    /**
     * Setter method for property <tt>keyType</tt>.
     */
    public void setKeyType(String keyType) {
        this.keyType = keyType;
    }
}
