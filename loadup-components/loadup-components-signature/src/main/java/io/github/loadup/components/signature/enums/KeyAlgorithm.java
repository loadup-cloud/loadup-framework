package io.github.loadup.components.signature.enums;

/** Supported asymmetric key algorithms. */
public enum KeyAlgorithm {

    /**
     * RSA.
     */
    RSA("RSA", 2048),

    /**
     * DSA.
     */
    DSA("DSA", 2048),

    /**
     * ECDSA (elliptic curve).
     */
    EC("EC", 256);

    /**
     * The JCA algorithm name.
     */
    private final String jcaName;

    /**
     * The default key size in bits.
     */
    private final int defaultKeySize;

    KeyAlgorithm(String jcaName, int defaultKeySize) {
        this.jcaName = jcaName;
        this.defaultKeySize = defaultKeySize;
    }

    public String getJcaName() {
        return this.jcaName;
    }

    public int getDefaultKeySize() {
        return this.defaultKeySize;
    }
}
