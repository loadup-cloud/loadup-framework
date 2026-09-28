package io.github.loadup.components.signature.enums;

/** Supported asymmetric signature algorithms. */
public enum SignatureAlgorithm {

    /**
     * RSA with SHA-256
     */
    SHA256_WITH_RSA("SHA256withRSA", "RSA"),

    /**
     * RSA with SHA-512
     */
    SHA512_WITH_RSA("SHA512withRSA", "RSA"),

    /**
     * DSA with SHA-256
     */
    SHA256_WITH_DSA("SHA256withDSA", "DSA"),

    /**
     * ECDSA with SHA-256
     */
    SHA256_WITH_ECDSA("SHA256withECDSA", "EC");

    /** The JCA algorithm name. */
    private final String jcaName;

    /**
     * The key algorithm.
     */
    private final String keyAlgorithm;

    SignatureAlgorithm(String jcaName, String keyAlgorithm) {
        this.jcaName = jcaName;
        this.keyAlgorithm = keyAlgorithm;
    }

    public String getJcaName() {
        return this.jcaName;
    }

    public String getKeyAlgorithm() {
        return this.keyAlgorithm;
    }
}
