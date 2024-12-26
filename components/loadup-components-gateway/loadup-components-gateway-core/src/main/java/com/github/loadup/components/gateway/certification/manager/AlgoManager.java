package com.github.loadup.components.gateway.certification.manager;

import com.github.loadup.components.gateway.certification.model.CertificationFactor;

/**
 * 算法管理接口(报文类)
 */
public interface AlgoManager {

    /**
     * 加密接口
     */
    public String encrypt(String srcContent, CertificationFactor certificationFactor);

    /**
     * 解密接口
     */
    public String decrypt(String encryptedContent, CertificationFactor certificationFactor);

    /**
     * 加签接口
     */
    public String sign(String srcContent, CertificationFactor certificationFactor);

    /**
     * 验签接口
     */
    public boolean verify(String SrcContent, String signedContent,
                          CertificationFactor certificationFactor);

    /**
     * 摘要接口
     */
    public String digest(String srcContent, CertificationFactor certificationFactor);

    /**
     * 特殊加签接口
     */
    public String encode(String srcContent, CertificationFactor certificationFactor);

    /**
     * 特殊验签接口
     */
    public String decode(String SrcContent, String signedContent,
                         CertificationFactor certificationFactor);

}
