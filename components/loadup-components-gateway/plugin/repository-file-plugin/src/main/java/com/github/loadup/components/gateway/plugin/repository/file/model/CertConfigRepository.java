package com.github.loadup.components.gateway.plugin.repository.file.model;

/*-
 * #%L
 * repository-file-plugin
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

import com.github.loadup.components.gateway.core.common.Constant;
import org.apache.commons.lang3.StringUtils;

/**
 * <p>
 * ApiConfigRepository.java
 * </p>
 */
public class CertConfigRepository {

    /**
     * File index 0
     */
    private String securityStrategyCode;
    /**
     * File index 1
     */
    private String operateType;
    /**
     * File index 2
     */
    private String algorithm;
    /**
     * File index 3
     */
    private String keyType;
    /**
     * File index 4
     */
    private String certType;
    /**
     * File index 5
     */
    private String keyContent;
    /**
     * File index 6
     */
    private String clientId;
    /**
     * File index 7
     */
    private String certProperties;
    /**
     * File index 8
     */
    private String algorithmProperties;

    /**
     * Constructor.
     */
    public CertConfigRepository() {}

    /**
     * Constructor.
     */
    public CertConfigRepository(String line) {
        String[] columns = StringUtils.splitPreserveAllTokens(line, Constant.COMMA_SEPARATOR);
        this.securityStrategyCode = columns[0];
        this.operateType = columns[1];
        this.algorithm = columns[2];
        this.keyType = columns[3];
        this.certType = columns[4];
        this.keyContent = columns[5];
        this.clientId = columns[6];
        this.certProperties = columns[7];
        this.algorithmProperties = columns[8];
    }

    /**
     * Gets get security strategy code.
     */
    public String getSecurityStrategyCode() {
        return securityStrategyCode;
    }

    /**
     * Sets set security strategy code.
     */
    public void setSecurityStrategyCode(String securityStrategyCode) {
        this.securityStrategyCode = securityStrategyCode;
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
     * Gets get algorithm.
     */
    public String getAlgorithm() {
        return algorithm;
    }

    /**
     * Sets set algorithm.
     */
    public void setAlgorithm(String algorithm) {
        this.algorithm = algorithm;
    }

    /**
     * Gets get key type.
     */
    public String getKeyType() {
        return keyType;
    }

    /**
     * Sets set key type.
     */
    public void setKeyType(String keyType) {
        this.keyType = keyType;
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
     * Gets get key content.
     */
    public String getKeyContent() {
        return keyContent;
    }

    /**
     * Sets set key content.
     */
    public void setKeyContent(String keyContent) {
        this.keyContent = keyContent;
    }

    /**
     * Gets get client id.
     */
    public String getClientId() {
        return clientId;
    }

    /**
     * Sets set client id.
     */
    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    /**
     * Gets get cert properties.
     */
    public String getCertProperties() {
        return certProperties;
    }

    /**
     * Sets set cert properties.
     */
    public void setCertProperties(String certProperties) {
        this.certProperties = certProperties;
    }

    /**
     * Gets get algorithm properties.
     */
    public String getAlgorithmProperties() {
        return algorithmProperties;
    }

    /**
     * Sets set algorithm properties.
     */
    public void setAlgorithmProperties(String algorithmProperties) {
        this.algorithmProperties = algorithmProperties;
    }
}
