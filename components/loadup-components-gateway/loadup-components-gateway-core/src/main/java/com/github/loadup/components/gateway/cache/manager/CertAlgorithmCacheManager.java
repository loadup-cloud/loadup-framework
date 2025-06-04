/* Copyright (C) LoadUp Cloud 2022-2025 */
package com.github.loadup.components.gateway.cache.manager;

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

import static com.github.loadup.components.gateway.core.common.Constant.EMPTY_STRING;

import com.github.loadup.components.gateway.cache.CertConfigCache;
import com.github.loadup.components.gateway.certification.cache.CacheUtil;
import com.github.loadup.components.gateway.core.model.CertConfig;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

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
    private String buildCertCode(
            String security_strategy_code,
            String security_strategy_operate_type,
            String security_strategy_type,
            String client_id) {
        return CacheUtil.generateKey(
                security_strategy_code, security_strategy_operate_type, security_strategy_type, client_id);
    }

    /**
     * get Cert Content String
     */
    public String getCertContentString(
            String security_strategy_code,
            String security_strategy_operate_type,
            String security_strategy_algorithm,
            String clientId) {
        String key = CacheUtil.generateKey(
                security_strategy_code, security_strategy_operate_type, security_strategy_algorithm, clientId);
        // TODO need to fetch cert content if this is only a key
        // CertConfig certConfig = certConfigCache.get(key);
        CertConfig certConfig = certConfigCache.getWithCertCode(key);
        return null == certConfig ? EMPTY_STRING : certConfig.getCertContent();
    }

    /**
     * get CertConfig by certCode
     */
    public CertConfig getCertConfigByCode(
            String security_strategy_code,
            String security_strategy_operate_type,
            String security_strategy_type,
            String client_id) {
        String certCode = buildCertCode(
                security_strategy_code, security_strategy_operate_type, security_strategy_type, client_id);
        CertConfig certConfig = certConfigCache.getWithCertCode(certCode);
        return certConfig;
    }
}
