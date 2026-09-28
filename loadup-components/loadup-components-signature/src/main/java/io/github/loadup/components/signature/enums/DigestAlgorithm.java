package io.github.loadup.components.signature.enums;

/** Supported digest and HMAC algorithms. */
public enum DigestAlgorithm {

    /**
     * MD5 (not recommended for security-sensitive scenarios).
     */
    MD5("MD5", false),

    /**
     * SHA-1 (not recommended for security-sensitive scenarios).
     */
    SHA1("SHA-1", false),

    /**
     * SHA-256
     */
    SHA256("SHA-256", false),

    /**
     * SHA-512
     */
    SHA512("SHA-512", false),

    /**
     * HMAC with SHA-256
     */
    HMAC_SHA256("HmacSHA256", true),

    /**
     * HMAC with SHA-512
     */
    HMAC_SHA512("HmacSHA512", true);

    /** The JCA algorithm name. */
    private final String jcaName;

    /**
     * Whether this is an HMAC algorithm.
     */
    private final boolean hmac;

    DigestAlgorithm(String jcaName, boolean isHmac) {
        this.jcaName = jcaName;
        this.hmac = isHmac;
    }

    public String getJcaName() {
        return this.jcaName;
    }

    public boolean isHmac() {
        return hmac;
    }
}
