package com.github.loadup.components.gateway.certification.algo;

import com.github.loadup.components.gateway.certification.exception.CertificationErrorCode;
import com.github.loadup.components.gateway.certification.exception.CertificationException;
import com.github.loadup.components.gateway.certification.util.CommonUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.InitializingBean;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;

/**
 * 抽象类
 */
public class AbstractAlgorithm implements Algorithm, InitializingBean {

    /**
     * 处理的对象类型
     */
    public static final String CONTENT_TYPE = "MESSAGE";

    /**
     * 解密操作接口
     */
    @Override
    public byte[] decrypt(byte[] data, byte[] key, String algorithm) {
        throw new CertificationException(CertificationErrorCode.UNSUPPORTED_OPERATION, algorithm);
    }

    /**
     * 公共解密接口， 带有秘钥类型
     */
    @Override
    public byte[] decrypt(byte[] data, byte[] key, String algorithm, boolean isPrivateKey) {
        throw new CertificationException(CertificationErrorCode.UNSUPPORTED_OPERATION, algorithm);
    }

    /**
     * 无秘钥摘要算法
     */
    @Override
    public byte[] digest(byte[] data, String algorithm) {
        throw new CertificationException(CertificationErrorCode.UNSUPPORTED_OPERATION, algorithm);
    }

    /**
     * 有秘钥摘要算法
     */
    @Override
    public byte[] digest(byte[] data, byte[] key, String algorithm) {
        throw new CertificationException(CertificationErrorCode.UNSUPPORTED_OPERATION, algorithm);
    }

    /**
     * 加密操作接口
     */
    @Override
    public byte[] encrypt(byte[] data, byte[] key, String algorithm) {
        throw new CertificationException(CertificationErrorCode.UNSUPPORTED_OPERATION, algorithm);
    }

    /**
     * 公共加密接口，带有秘钥类型
     */
    @Override
    public byte[] encrypt(byte[] data, byte[] key, String algorithm, boolean isPrivateKey) {
        throw new CertificationException(CertificationErrorCode.UNSUPPORTED_OPERATION, algorithm);
    }

    /**
     * 公共签名接口
     */
    @Override
    public byte[] sign(byte[] data, byte[] key, String algorithm) {
        throw new CertificationException(CertificationErrorCode.UNSUPPORTED_OPERATION, algorithm);
    }

    /**
     *
     */
    @Override
    public byte[] sign(byte[] data, byte[] key, byte[] cert, String algorithm, boolean attach) {
        throw new CertificationException(CertificationErrorCode.UNSUPPORTED_OPERATION, algorithm);
    }

    /**
     * 公共验签接口
     */
    @Override
    public boolean verify(byte[] unSignedData, byte[] signedData, byte[] key, String algorithm) {
        throw new CertificationException(CertificationErrorCode.UNSUPPORTED_OPERATION, algorithm);
    }

    /**
     *
     */
    @Override
    public boolean verify(byte[] unSignedData, byte[] signedData, byte[] key, String algorithm,
                          boolean attach) {
        throw new CertificationException(CertificationErrorCode.UNSUPPORTED_OPERATION, algorithm);
    }

    /**
     * Invoked by a BeanFactory after it has set all bean properties supplied
     * (and satisfied BeanFactoryAware and ApplicationContextAware).
     * <p>This method allows the bean instance to perform initialization only
     * possible when all bean properties have been set and to throw an
     * exception in the event of misconfiguration.
     *
     * @throws Exception in the event of misconfiguration (such
     * as failure to set an essential property) or if initialization fails.
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        doRegisterManager();
    }

    /**
     * XML签名
     *
     *
     *
     *
     *
     * 支持下列算法
     * <ul>
     * <li>XMLSignature.ALGO_ID_SIGNATURE_RSA</li>
     * <li>XMLSignature.ALGO_ID_SIGNATURE_RSA_SHA1</li>
     * <li>XMLSignature.ALGO_ID_SIGNATURE_RSA_SHA256</li>
     * <li>XMLSignature.ALGO_ID_SIGNATURE_RSA_SHA384</li>
     * <li>XMLSignature.ALGO_ID_SIGNATURE_RSA_SHA512</li>
     * </ul>
     *
     * <ul>
     * <li>作为子节点：  XmlSignatureAppendMode.AS_CHILDREN</li>
     * <li>作为兄弟节点：XmlSignatureAppendMode.AS_BROTHER</li>
     * </ul>
     *
     * @throws Exception the exception
     */
    @Override
    public byte[] signXmlElement(byte[] priKeyData, byte[] certData, byte[] xmlDocBytes,
                                 String encode, String elementTagName, String algorithm,
                                 int signatureAppendMode) {
        throw new CertificationException(CertificationErrorCode.UNSUPPORTED_OPERATION, algorithm);
    }

    /**
     * 验证XML签名
     *
     * @throws Exception the exception
     */
    @Override
    public boolean verifyXmlElement(byte[] pubKeyData, byte[] xmlDocBytes, String encode) {
        throw new CertificationException(CertificationErrorCode.UNSUPPORTED_OPERATION, "xml signature");
    }

    /**
     * KATONG等特殊签名算法
     */
    @Override
    public byte[] encode(byte[] privateKey, byte[] srcContent) {
        throw new CertificationException(CertificationErrorCode.UNSUPPORTED_OPERATION, "encode");
    }

    /**
     * KATONG等特殊验签算法
     */
    @Override
    public String decode(byte[] publicKey, byte[] srcContent) {
        throw new CertificationException(CertificationErrorCode.UNSUPPORTED_OPERATION, "decode");
    }

    /**
     * 注册算法类到对应manager接口
     */
    protected void doRegisterManager() {

    }

    /**
     * 生成日志头信息
     */
    public static String genLogSign(String algo) {
        return CommonUtil.decorateBySquareBrackets(CONTENT_TYPE, algo);
    }

    /**
     * 初始化向量
     */
    protected IvParameterSpec getIv(Cipher cipher) {

        byte[] iv = new byte[cipher.getBlockSize()];
        for (int i = 0; i < iv.length; i++) {
            iv[i] = 0;
        }
        return new IvParameterSpec(iv);
    }

    /**
     * 判断算是否CBC模式，此方法值针对DES, DESede等算法适用
     */
    public boolean isCBCMode(String algoStr) {
        return StringUtils.contains(algoStr, "CBC");
    }

}