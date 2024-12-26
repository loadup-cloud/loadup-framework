package com.github.loadup.components.gateway.certification.spi;

import com.github.loadup.components.gateway.certification.model.CertificationFactor;

/**
 * 远程调用spi实现，本地满足不了算法需求则走远程调用，需应用自助实现
 */
public interface OuterService {

    /**
     * 签名操作统一接口
     */
    public String sign(String srcContent, CertificationFactor certificationFactor);

    /**
     * 验签操作统一接口
     *
     *
     *
     * param certificationFactor  操作要素
     */
    public boolean verify(String SrcContent, String signedContent,
                          CertificationFactor certificationFactor);

    /**
     * 报文加密统一接口
     *
     *
     * param certificationFactor  操作要素
     */
    public String encrypt(String srcContent, CertificationFactor certificationFactor);

    /**
     * 报文解密统一接口
     *
     *
     * param certificationFactor  操作要素
     */
    public String decrypt(String encryptedContent, CertificationFactor certificationFactor);

    /**
     * 报文摘要统一接口
     */
    public String digest(String srcContent, CertificationFactor certificationFactor);

}