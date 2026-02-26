package io.github.loadup.components.signature.enums;

import lombok.Getter;

/**
 * 签名算法枚举
 *
 * @author loadup
 */
@Getter
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

    /**
     * JCA 算法名称
     */
    private final String jcaName;

    /**
     * 密钥算法
     */
    private final String keyAlgorithm;

    SignatureAlgorithm(String jcaName, String keyAlgorithm) {
        this.jcaName = jcaName;
        this.keyAlgorithm = keyAlgorithm;
    }
}

