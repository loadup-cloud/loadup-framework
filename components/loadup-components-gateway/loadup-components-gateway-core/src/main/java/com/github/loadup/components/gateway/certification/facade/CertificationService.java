package com.github.loadup.components.gateway.certification.facade;

import com.github.loadup.components.gateway.certification.exception.CertificationException;

/**
 * 安全组件统一操作接口
 */
public interface CertificationService {

    /**
     * 获取证书内容, 从缓存获取证书内容，证书内容以byte[]的base64Encode后的String格式
     *
     * @throws CertificationException
     */
    String getCert(String security_strategy_code,
                   String security_strategy_operate_type,
                   String security_strategy_algorithm,
                   String clientId)
            throws CertificationException;

}
