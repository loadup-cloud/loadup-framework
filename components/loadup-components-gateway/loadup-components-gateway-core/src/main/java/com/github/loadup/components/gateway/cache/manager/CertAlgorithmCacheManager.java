package com.github.loadup.components.gateway.cache.manager;

import com.github.loadup.components.gateway.cache.CertConfigCache;
import com.github.loadup.components.gateway.certification.cache.CacheUtil;
import com.github.loadup.components.gateway.core.model.CertConfig;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import static com.github.loadup.components.gateway.core.common.Constant.EMPTY_STRING;

/**
 * cert and algorithm cache manager
 */
@Component
public class CertAlgorithmCacheManager {

    /**
     * logger
     */
    private static final Logger logger = LoggerFactory.getLogger(CertAlgorithmCacheManager.class);

    /**
     * cert config cache class
     */
    @Resource
    private CertConfigCache certConfigCache;

    /**
     * certCode = security_strategy_code-security_strategy_operate_type-security_strategy_type-client_id
     */
    private String buildCertCode(String security_strategy_code, String security_strategy_operate_type,
                                 String security_strategy_type, String client_id) {
        return CacheUtil.generateKey(security_strategy_code, security_strategy_operate_type, security_strategy_type, client_id);
    }

    /**
     * get Cert Content String
     */
    public String getCertContentString(String security_strategy_code,
                                       String security_strategy_operate_type,
                                       String security_strategy_algorithm, String clientId) {
        String key = CacheUtil.generateKey(security_strategy_code, security_strategy_operate_type, security_strategy_algorithm, clientId);
        //TODO need to fetch cert content if this is only a key
        //CertConfig certConfig = certConfigCache.get(key);
        CertConfig certConfig = certConfigCache.getWithCertCode(key);
        return null == certConfig ? EMPTY_STRING : certConfig.getCertContent();
    }

    /**
     * get CertConfig by certCode
     */
    public CertConfig getCertConfigByCode(String security_strategy_code,
                                          String security_strategy_operate_type,
                                          String security_strategy_type,
                                          String client_id) {
        String certCode = buildCertCode(security_strategy_code, security_strategy_operate_type, security_strategy_type, client_id);
        CertConfig certConfig = certConfigCache.getWithCertCode(certCode);
        return certConfig;
    }

}
