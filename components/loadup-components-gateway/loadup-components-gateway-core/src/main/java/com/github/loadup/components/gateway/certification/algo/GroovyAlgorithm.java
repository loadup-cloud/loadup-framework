package com.github.loadup.components.gateway.certification.algo;

import com.github.loadup.components.gateway.certification.model.CertificationFactor;

/**
 * groovy 算法接口
 */
public interface GroovyAlgorithm {

    /**
     * 公共签名接口
     */
    public String sign(String data, CertificationFactor certificationFactor);

    /**
     * 公共验签接口
     */
    public boolean verify(String unSignedData, String signedData,
                          CertificationFactor certificationFactor);

    /**
     * 加密操作接口
     */
    public String encrypt(String data, CertificationFactor certificationFactor);

    /**
     * 解密操作接口
     */
    public String decrypt(String data, CertificationFactor certificationFactor);

    /**
     * 有秘钥摘要算法
     */
    public String digest(String data, CertificationFactor certificationFactor);

    /**
     * 特殊签名算法
     */
    public String encode(String srcContent, CertificationFactor certificationFactor);

    /**
     * 特殊验签算法
     */
    public String decode(String srcContent, CertificationFactor certificationFactor);

}