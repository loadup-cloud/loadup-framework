package io.github.loadup.components.signature.enums;

import lombok.Getter;

/**
 * 摘要算法枚举
 *
 * @author loadup
 */
@Getter
public enum DigestAlgorithm {

    /**
     * MD5 (不推荐用于安全场景)
     */
    MD5("MD5", false),

    /**
     * SHA-1 (不推荐用于安全场景)
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

    /**
     * JCA 算法名称
     */
    private final String jcaName;

    /**
     * 是否为 HMAC 算法
     */
    private final boolean isHmac;

    DigestAlgorithm(String jcaName, boolean isHmac) {
        this.jcaName = jcaName;
        this.isHmac = isHmac;
    }
}

