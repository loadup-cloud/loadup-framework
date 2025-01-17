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
 * <p>
 * CertAlgorithmConfig.java
 * </p>
 */
public class CertAlgorithmConfigDto {

    /**
     * Primary key(optionaal)
     */
    private String certCode;

    /**
     * Required
     */
    private String operateType;

    /**
     * Required
     */
    private String algoName;

    /**
     * Required
     */
    private String certType;

    /**
     * Optional
     */
    private String algoProperties;

    /**
     * Required
     */
    private String algoType;

    /**
     * securityStrategyCode
     */
    private String securityStrategyCode;

    /**
     *
     */
    private String clientId;

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
     * Gets get operate type.
     */
    public String getOperateType() {
        return operateType;
    }

    /**
     * Sets set operate type.
     */
    public void setOperateType(String operateType) {
        this.operateType = operateType;
    }

    /**
     * Gets get algo name.
     */
    public String getAlgoName() {
        return algoName;
    }

    /**
     * Sets set algo name.
     */
    public void setAlgoName(String algoName) {
        this.algoName = algoName;
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
     * Gets get algo properties.
     */
    public String getAlgoProperties() {
        return algoProperties;
    }

    /**
     * Sets set algo properties.
     */
    public void setAlgoProperties(String algoProperties) {
        this.algoProperties = algoProperties;
    }

    /**
     * Gets get algo type.
     */
    public String getAlgoType() {
        return algoType;
    }

    /**
     * Sets set algo type.
     */
    public void setAlgoType(String algoType) {
        this.algoType = algoType;
    }

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
}
