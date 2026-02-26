package io.github.loadup.components.signature.service;

import io.github.loadup.components.signature.enums.KeyAlgorithm;
import io.github.loadup.components.signature.model.KeyPairInfo;

import java.security.PrivateKey;
import java.security.PublicKey;

/**
 * 密钥对管理服务接口
 *
 * @author loadup
 */
public interface KeyPairService {

    /**
     * 生成密钥对
     *
     * @param algorithm 密钥算法
     * @param keySize   密钥长度
     * @return 密钥对信息（包含 Base64 编码的公私钥）
     */
    KeyPairInfo generateKeyPair(KeyAlgorithm algorithm, int keySize);

    /**
     * 生成密钥对（使用默认密钥长度）
     *
     * @param algorithm 密钥算法
     * @return 密钥对信息
     */
    KeyPairInfo generateKeyPair(KeyAlgorithm algorithm);

    /**
     * 从 Base64 字符串加载私钥
     *
     * @param base64PrivateKey Base64 编码的私钥
     * @param algorithm        密钥算法
     * @return 私钥对象
     */
    PrivateKey loadPrivateKey(String base64PrivateKey, KeyAlgorithm algorithm);

    /**
     * 从 Base64 字符串加载公钥
     *
     * @param base64PublicKey Base64 编码的公钥
     * @param algorithm       密钥算法
     * @return 公钥对象
     */
    PublicKey loadPublicKey(String base64PublicKey, KeyAlgorithm algorithm);
}

