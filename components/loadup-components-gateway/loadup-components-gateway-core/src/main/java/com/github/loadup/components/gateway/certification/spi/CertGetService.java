package com.github.loadup.components.gateway.certification.spi;

import com.github.loadup.components.gateway.facade.enums.CertTypeEnum;

/**
 * 证书获取接口
 */
public interface CertGetService {

    /**
     * 获取证书或秘钥内容, 从缓存获取对应内容
     */
    public String getCert(String certAliasName, CertTypeEnum certType, String bizKey);
}
