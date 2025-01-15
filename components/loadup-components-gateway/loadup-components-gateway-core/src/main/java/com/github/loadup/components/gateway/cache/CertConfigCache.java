package com.github.loadup.components.gateway.cache;

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

import com.github.loadup.components.gateway.certification.cache.CacheUtil;
import com.github.loadup.components.gateway.common.exception.GatewayException;
import com.github.loadup.components.gateway.common.util.*;
import com.github.loadup.components.gateway.core.common.GatewayliteErrorCode;
import com.github.loadup.components.gateway.core.common.enums.RepositoryType;
import com.github.loadup.components.gateway.core.model.CertConfig;
import com.github.loadup.components.gateway.core.prototype.constant.SwitchConstants;
import com.github.loadup.components.gateway.core.service.SecurityProdCenterQueryService;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * <p>
 * MessageSenderCache.java
 * </p>
 */
@Component("gatewayCertConfigCache")
public class CertConfigCache {

	/**
	 * Key is message sender id
	 */
	private static Map<String, CertConfig> certConfigMap = new ConcurrentHashMap();

	@Resource
	private SecurityProdCenterQueryService securityProdCenterQueryService;

	/**
	 * Put all.
	 */
	public static synchronized void putAll(boolean clear, List<CertConfig> certConfigList) {
		if (CollectionUtils.isEmpty(certConfigList)) {
			return;
		}

		Map<String, CertConfig> tempMap = new ConcurrentHashMap<String, CertConfig>();
		certConfigList.forEach(certConfig -> {
			String certCode = certConfig.getCertCode();
			tempMap.put(certCode, certConfig);
		});
		if (clear) {
			certConfigMap = tempMap;
		} else {
			certConfigMap.putAll(tempMap);
		}
		CacheLogUtil.printLog("certConfigMap", certConfigMap);
	}

	/**
	 * Gets get with cert code.
	 */
	public CertConfig getWithCertCode(String certCode) {
		RepositoryType repositoryType = RepositoryUtil.getRepositoryType();
		if (repositoryType.isConfigInInternalCache()) {
			return certConfigMap.get(certCode);
		}
		if (repositoryType == RepositoryType.PRODCENTER) {
			String[] keys = CacheUtil.splitKey(certCode);
			if (ArrayUtils.isEmpty(keys) || keys.length < 4) {
				return null;
			}
			String securityStrategyCode = keys[0];
			if (StringUtils.equals(securityStrategyCode, SwitchConstants.OFF)) {
				return null;
			}
			CertConfig result = certConfigMap.get(certCode);
			if (result != null) {
				return result;
			}
			// need to query cert config for platform config if no cert config for current tenant
			String securityStrategyOperateType = keys[1];
			String algorithm = keys[2];
			String platformCertCode = CacheUtil.generateKey(securityStrategyCode,
					securityStrategyOperateType, algorithm);
			result = certConfigMap.get(platformCertCode);
			if (result != null) {
				result.setClientId(TenantUtil.getCurrentClientId());
			}
			return result;
		}
		throw new GatewayException(GatewayliteErrorCode.CONFIGURATION_LOAD_ERROR,
				"Unsupported repository.");
	}

}
