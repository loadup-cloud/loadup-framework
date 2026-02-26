package io.github.loadup.components.signature.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 密钥对信息
 *
 * @author loadup
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KeyPairInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Base64 编码的公钥
     */
    private String publicKey;

    /**
     * Base64 编码的私钥
     */
    private String privateKey;

    /**
     * 密钥算法
     */
    private String algorithm;

    /**
     * 密钥长度
     */
    private Integer keySize;
}

