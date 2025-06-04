/* Copyright (C) LoadUp Cloud 2022-2025 */
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

import static com.github.loadup.components.gateway.plugin.repository.file.util.FileRepositoryUtil.getColumns;

/**
 * <p>
 * ApiConfigRepository.java
 * </p>
 */
public class SpiConfigRepository {

    /**
     * File index 0
     */
    private String integrationUri;
    /**
     * File index 1
     */
    private String securityStrategyCode;
    /**
     * File index 2
     */
    private String integrationHeaderAssemble;
    /**
     * File index 3
     */
    private String integrationAssemble;
    /**
     * File index 4
     */
    private String integrationParser;
    /**
     * File index 5
     */
    private String communicationProperties;

    public SpiConfigRepository() {}

    /**
     * Constructor.
     */
    public SpiConfigRepository(String line) {
        String[] columns = getColumns(line);
        this.setIntegrationUri(columns[0]);
        this.setSecurityStrategyCode(columns[1]);
        this.setIntegrationHeaderAssemble(columns[2]);
        this.setIntegrationAssemble(columns[3]);
        this.setIntegrationParser(columns[4]);
        this.setCommunicationProperties(columns[5]);
    }

    /**
     * Gets get integration uri.
     */
    public String getIntegrationUri() {
        return integrationUri;
    }

    /**
     * Sets set integration uri.
     */
    public void setIntegrationUri(String integrationUri) {
        this.integrationUri = integrationUri;
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
     * Gets get integration header assemble.
     */
    public String getIntegrationHeaderAssemble() {
        return integrationHeaderAssemble;
    }

    /**
     * Sets set integration header assemble.
     */
    public void setIntegrationHeaderAssemble(String integrationHeaderAssemble) {
        this.integrationHeaderAssemble = integrationHeaderAssemble;
    }

    /**
     * Gets get integration assemble.
     */
    public String getIntegrationAssemble() {
        return integrationAssemble;
    }

    /**
     * Sets set integration assemble.
     */
    public void setIntegrationAssemble(String integrationAssemble) {
        this.integrationAssemble = integrationAssemble;
    }

    /**
     * Gets get integration parser.
     */
    public String getIntegrationParser() {
        return integrationParser;
    }

    /**
     * Sets set integration parser.
     */
    public void setIntegrationParser(String integrationParser) {
        this.integrationParser = integrationParser;
    }

    /**
     * Gets get communication properties.
     */
    public String getCommunicationProperties() {
        return communicationProperties;
    }

    /**
     * Sets set communication properties.
     */
    public void setCommunicationProperties(String communicationProperties) {
        this.communicationProperties = communicationProperties;
    }
}
