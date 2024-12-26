package com.github.loadup.components.gateway.common.convertor;

import com.github.loadup.components.gateway.certification.cache.CacheUtil;
import com.github.loadup.components.gateway.common.exception.GatewayException;
import com.github.loadup.components.gateway.common.util.TenantUtil;
import com.github.loadup.components.gateway.core.common.Constant;
import com.github.loadup.components.gateway.core.common.GatewayliteErrorCode;
import com.github.loadup.components.gateway.core.model.CertConfig;
import com.github.loadup.components.gateway.facade.config.model.SecurityConditionGroup;
import com.github.loadup.components.gateway.facade.util.LogUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import static com.github.loadup.components.gateway.facade.config.model.Constant.PLATFORM_TENANT_ID;

/**
 *
 */
@Component("gatewayCertConfigConvertor")
public class CertConfigConvertor {

    /**
     * logger
     */
    private static final Logger logger = LoggerFactory
            .getLogger(CertConfigConvertor.class);

    /**
     * convert ConditionGroup to CertConfig
     */
    public static CertConfig convertToCertConfig(SecurityConditionGroup securityConditionGroup,
                                                 String tenantId) {
        if (securityConditionGroup == null) {
            return null;
        }
        CertConfig result = new CertConfig();
        String certCode = null;
        if (StringUtils.equals(tenantId, PLATFORM_TENANT_ID)) {
            // platform config key for PUBLIC tenant in prodcenter
            certCode = CacheUtil.generateKey(securityConditionGroup.getSecurityStrategyCode(),
                    securityConditionGroup.getSecurityStrategyOperateType(),
                    securityConditionGroup.getSecurityStrategyAlgorithm());
        } else {
            // current tenant config key for tenant in prodcenter
            String clientId = TenantUtil.getClientIdByTenantId(tenantId);
            certCode = CacheUtil.generateKey(securityConditionGroup.getSecurityStrategyCode(),
                    securityConditionGroup.getSecurityStrategyOperateType(),
                    securityConditionGroup.getSecurityStrategyAlgorithm(), clientId);
            result.setClientId(clientId);
        }
        result.setCertCode(certCode);
        result.setCertType(securityConditionGroup.getCertType());

        try {
            //            String actualCertContent = certContentUtil.getCertContent(
            //                    securityConditionGroup.getSecurityStrategyKeyType(),
            //                    securityConditionGroup.getSecurityStrategyKey(),
            //                    securityConditionGroup.getCertType());
            //            result.setCertContent(actualCertContent);
        } catch (Exception ex) {
            LogUtil.error(logger, "error to get actual cert content. SecurityConditionGroup=",
                    result);
            throw new GatewayException(GatewayliteErrorCode.CONFIGURATION_LOAD_ERROR, ex);
        }
        result.setCertStatus(Constant.DEFAULT_CERT_STATUS);
        return result;
    }

}