package com.github.loadup.components.gateway.certification.algo;

/**
 * 算法接口
 */
public interface Algorithm {

    /**
     * 无秘钥摘要算法
     */
    public byte[] digest(byte[] data, String algorithm);

    /**
     * 有秘钥摘要算法
     */
    public byte[] digest(byte[] data, byte[] key, String algorithm);

    /**
     * 加密操作接口
     */
    public byte[] encrypt(byte[] data, byte[] key, String algorithm);

    /**
     * 解密操作接口
     */
    public byte[] decrypt(byte[] data, byte[] key, String algorithm);

    /**
     * 公共加密接口，带有秘钥类型
     */
    public byte[] encrypt(byte[] data, byte[] key, String algorithm, boolean isPrivateKey);

    /**
     * 公共解密接口， 带有秘钥类型
     */
    public byte[] decrypt(byte[] data, byte[] key, String algorithm, boolean isPrivateKey);

    /**
     * 公共签名接口
     */
    public byte[] sign(byte[] data, byte[] key, String algorithm);

    /**
     * 公共签名接口
     */
    public byte[] sign(byte[] data, byte[] key, byte[] cert, String algorithm, boolean attach);

    /**
     * 公共验签接口
     */
    public boolean verify(byte[] unSignedData, byte[] signedData, byte[] key, String algorithm);

    /**
     * 公共验签接口
     */
    public boolean verify(byte[] unSignedData, byte[] signedData, byte[] key, String algorithm,
                          boolean attach);

    /**
     * XML签名
     *
     *
     *
     *
     *
     * 支持下列算法
     * <ul>
     *     <li>XMLSignature.ALGO_ID_SIGNATURE_RSA</li>
     *     <li>XMLSignature.ALGO_ID_SIGNATURE_RSA_SHA1</li>
     *     <li>XMLSignature.ALGO_ID_SIGNATURE_RSA_SHA256</li>
     *     <li>XMLSignature.ALGO_ID_SIGNATURE_RSA_SHA384</li>
     *     <li>XMLSignature.ALGO_ID_SIGNATURE_RSA_SHA512</li>
     * </ul>
     *
     * <ul>
     *   <li>作为子节点：  XmlSignatureAppendMode.AS_CHILDREN</li>
     *   <li>作为兄弟节点：XmlSignatureAppendMode.AS_BROTHER</li>
     * </ul>
     *
     * @throws Exception the exception
     */
    public byte[] signXmlElement(byte[] priKeyData, byte[] certData, byte[] xmlDocBytes,
                                 String encode, String elementTagName, String algorithm,
                                 int signatureAppendMode);

    /**
     * 验证XML签名
     *
     * @throws Exception the exception
     */
    public boolean verifyXmlElement(byte[] pubKeyData, byte[] xmlDocBytes, String encode);

    /**
     * KATONG等特殊签名算法
     */
    public byte[] encode(byte[] privateKey, byte[] srcContent);

    /**
     * KATONG等特殊验签算法
     */
    public String decode(byte[] publicKey, byte[] srcContent);

}
