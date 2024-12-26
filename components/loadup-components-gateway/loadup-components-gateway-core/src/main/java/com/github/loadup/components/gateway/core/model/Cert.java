package com.github.loadup.components.gateway.core.model;

/**
 *
 */
public class Cert {

    /**
     * cert config
     */
    private CertConfig certConfig;

    /**
     * cert algorithm config
     */
    private CertAlgorithmConfig certAlgorithmConfig;

    /**
     * Getter method for property <tt>certConfig</tt>.
     */
    public CertConfig getCertConfig() {
        return certConfig;
    }

    /**
     * Setter method for property <tt>certConfig</tt>.
     */
    public void setCertConfig(CertConfig certConfig) {
        this.certConfig = certConfig;
    }

    /**
     * Getter method for property <tt>certAlgorithmConfig</tt>.
     */
    public CertAlgorithmConfig getCertAlgorithmConfig() {
        return certAlgorithmConfig;
    }

    /**
     * Setter method for property <tt>certAlgorithmConfig</tt>.
     */
    public void setCertAlgorithmConfig(CertAlgorithmConfig certAlgorithmConfig) {
        this.certAlgorithmConfig = certAlgorithmConfig;
    }
}