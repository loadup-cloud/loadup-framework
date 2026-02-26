package io.github.loadup.components.signature.enums;

import lombok.Getter;

/**
 * 密钥算法枚举
 *
 * @author loadup
 */
@Getter
public enum KeyAlgorithm {

    /**
     * RSA 算法
     */
    RSA("RSA", 2048),

    /**
     * DSA 算法
     */
    DSA("DSA", 2048),

    /**
     * ECDSA (椭圆曲线) 算法
     */
    EC("EC", 256);

    /**
     * JCA 算法名称
     */
    private final String jcaName;

    /**
     * 默认密钥长度
     */
    private final int defaultKeySize;

    KeyAlgorithm(String jcaName, int defaultKeySize) {
        this.jcaName = jcaName;
        this.defaultKeySize = defaultKeySize;
    }
}

