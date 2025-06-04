/* Copyright (C) LoadUp Cloud 2022-2025 */
package com.github.loadup.components.gateway.common.convertor;

/*-
 * #%L
 * loadup-components-gateway-core
 * %%
 * Copyright (C) 2022 - 2025 loadup_cloud
 * %%
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 * #L%
 */

import static com.github.loadup.components.gateway.facade.config.model.Constant.PLATFORM_TENANT_ID;

import com.github.loadup.commons.error.CommonException;
import com.github.loadup.components.gateway.certification.cache.CacheUtil;
import com.github.loadup.components.gateway.common.util.TenantUtil;
import com.github.loadup.components.gateway.core.common.Constant;
import com.github.loadup.components.gateway.core.common.GatewayErrorCode;
import com.github.loadup.components.gateway.core.model.CertConfig;
import com.github.loadup.components.gateway.facade.config.model.SecurityConditionGroup;
import com.github.loadup.components.gateway.facade.util.LogUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 *
 */
@Component("gatewayCertConfigConvertor")
public class CertConfigConvertor {

    /**
     * logger
     */
    private static final Logger logger = LoggerFactory.getLogger(CertConfigConvertor.class);

    /**
     * convert ConditionGroup to CertConfig
     */
    public static CertConfig convertToCertConfig(SecurityConditionGroup securityConditionGroup, String tenantId) {
        if (securityConditionGroup == null) {
            return null;
        }
        CertConfig result = new CertConfig();
        String certCode = null;
        if (StringUtils.equals(tenantId, PLATFORM_TENANT_ID)) {
            // platform config key for PUBLIC tenant in prodcenter
            certCode = CacheUtil.generateKey(
                    securityConditionGroup.getSecurityStrategyCode(),
                    securityConditionGroup.getSecurityStrategyOperateType(),
                    securityConditionGroup.getSecurityStrategyAlgorithm());
        } else {
            // current tenant config key for tenant in prodcenter
            String clientId = TenantUtil.getClientIdByTenantId(tenantId);
            certCode = CacheUtil.generateKey(
                    securityConditionGroup.getSecurityStrategyCode(),
                    securityConditionGroup.getSecurityStrategyOperateType(),
                    securityConditionGroup.getSecurityStrategyAlgorithm(),
                    clientId);
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
            LogUtil.error(logger, "error to get actual cert content. SecurityConditionGroup=", result);
            throw new CommonException(GatewayErrorCode.CONFIGURATION_LOAD_ERROR, ex);
        }
        result.setCertStatus(Constant.DEFAULT_CERT_STATUS);
        return result;
    }
}
