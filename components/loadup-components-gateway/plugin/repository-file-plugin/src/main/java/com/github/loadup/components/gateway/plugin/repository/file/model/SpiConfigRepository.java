package com.github.loadup.components.gateway.plugin.repository.file.model;

import static com.github.loadup.components.gateway.plugin.repository.file.util.FileRepositoryUtil.getColumns;

/**
 * <p>
 * ApiConfigRepository.java
 * </p>
 */
public class SpiConfigRepository {

    public SpiConfigRepository() {
    }

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