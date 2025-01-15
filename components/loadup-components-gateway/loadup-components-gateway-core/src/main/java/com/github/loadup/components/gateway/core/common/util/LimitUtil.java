package com.github.loadup.components.gateway.core.common.util;

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

import com.github.loadup.components.gateway.common.error.LimitRuleErrorCode;
import com.github.loadup.components.gateway.common.exception.GatewayException;
import com.github.loadup.components.gateway.common.exception.util.AssertUtil;
import com.github.loadup.components.gateway.common.util.PropertiesUtil;
import com.github.loadup.components.gateway.common.util.RepositoryUtil;
import com.github.loadup.components.gateway.core.common.Constant;
import com.github.loadup.components.gateway.core.common.enums.RepositoryType;
import com.github.loadup.components.gateway.core.model.InterfaceConfig;
import com.github.loadup.components.gateway.core.model.Properties;
import com.github.loadup.components.gateway.core.prototype.util.DigestLoggerUtil;
import com.github.loadup.components.gateway.core.service.InterfaceProdCenterQueryService;
import com.github.loadup.components.gateway.core.service.LimitProdCenterQueryService;
import com.github.loadup.components.gateway.facade.config.model.CommunicationPropertiesGroup;
import com.github.loadup.components.gateway.facade.enums.*;
import com.github.loadup.components.gateway.facade.model.LimitConfig;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 *
 */
@Component
public class LimitUtil {

	/**
	 * Configuration of open the limit service or not
	 */
	@Value("${gateway.rate.limit.enable:false}")
	private String openLimitService;

	/**
	 * Limit type, could be standalone or distributed
	 */
	@Value("${gateway.rate.limit.type:standalone}")
	private String limitType;

	@Value("${gateway.rate.limit.interface.max.limit:}")
	private String interfaceMaxLimitStr;

	@Value("${gateway.rate.limit.tenant.max.limit:}")
	private String tenantMaxLimitStr;

	@Value("${gateway.rate.limit.client.max.limit:}")
	private String clientMaxLimitStr;

	/**
	 * Limit time rule, could be QPS or QPM or CONCURRENCY
	 */
	@Value("${gateway.rate.limit.distributed.time.rule:QPS}")
	private String limitTimeRule;

	/**
	 * strategy choice at the situation of cache exception
	 */
	@Value("${gateway.rate.limit.distributed.fallback.strategy:STANDALONE}")
	private String fallbackStrategy;

	/**
	 * The interface max limit when fallback to standalone
	 */
	@Value("${gateway.rate.limit.distributed.fallback.standalone.interface.max.limit:}")
	private String fallbackInterfaceMaxLimit;

	/**
	 * The client max limit when fallback to standalone
	 */
	@Value("${gateway.rate.limit.distributed.fallback.standalone.client.max.limit:}")
	private String fallbackClientMaxLimit;

	/**
	 * The tenant max limit when fallback to standalopne
	 */
	@Value("${gateway.rate.limit.distributed.fallback.standalone.tenant.max.limit:}")
	private String fallbackTenantMaxLimit;

	/**
	 * environment
	 */
	@Resource
	private Environment environment;

	/**
	 * limit product center service
	 */
	@Resource
	private LimitProdCenterQueryService limitProdCenterQueryService;

	@Resource
	private InterfaceProdCenterQueryService interfaceProdCenterQueryService;

	/**
	 * Getter method for property <tt>openLimitService</tt>.
	 */
	public Boolean getOpenLimitService() {
		if (RepositoryUtil.getRepositoryType() == RepositoryType.PRODCENTER) {
			return limitProdCenterQueryService.queryLimitConditionGroup().getOpenLimit();
		}
		return Boolean.parseBoolean(openLimitService);
	}

	/**
	 * Getter method for property <tt>limitType</tt>.
	 */
	public String getLimitType() {
		if (RepositoryUtil.getRepositoryType() == RepositoryType.PRODCENTER) {
			return limitProdCenterQueryService.queryLimitConditionGroup().getLimitType();
		}
		return limitType;
	}

	/**
	 * Getter method for property <tt>interfaceMaxLimitStr</tt>.
	 */
	@Deprecated
	public String getInterfaceMaxLimitStr() {
		if (RepositoryUtil.getRepositoryType() == RepositoryType.PRODCENTER) {
			return limitProdCenterQueryService.queryLimitConditionGroup().getInterfaceLimit();
		}
		return interfaceMaxLimitStr;
	}

	public List<LimitConfig> getLimitConfig(InterfaceConfig interfaceConfig, String transactionType) {
		List<LimitConfig> limitConfigList = new ArrayList<>(3);
		if (!getOpenLimitService()) {
			return limitConfigList;
		}
		String tenantId = "";//TenantUtil.getTenantId();
		String entryKeyId;
		String url;

		// 接口级别限流配置
		url = interfaceConfig.getInterfaceId();
		entryKeyId = tenantId + interfaceConfig.getInterfaceId() + transactionType;
		LimitConfig interfaceLimitConfig = null;
		if (RepositoryUtil.getRepositoryType() == RepositoryType.PRODCENTER) {
			interfaceLimitConfig = getInterfaceLimitConfig(url, entryKeyId);
		}
		// 新老限流配置兼容，如果配置中心没有配置新的限流配置模型，则读取老模型
		if (interfaceLimitConfig == null) {
			interfaceLimitConfig = new LimitConfig();
			interfaceLimitConfig.setEntryKeyId(entryKeyId);
			Properties properties = interfaceConfig.getProperties();
			String currentInterfaceMaxLimitStr = PropertiesUtil.getProperty(properties,
					Constant.INTERFACE_LIMIT_KEY);
			String maxLimit = getMaxLimit(interfaceConfig.getInterfaceId(), getInterfaceMaxLimitStr(),
					currentInterfaceMaxLimitStr);
			if (StringUtils.isBlank(maxLimit)) {
				interfaceLimitConfig.setEnableLimit(Boolean.FALSE);
			} else {
				interfaceLimitConfig.setLimitValue(Integer.parseInt(maxLimit));
				interfaceLimitConfig.setLimitTimeRule(
						LimitTimeRuleEnum.getTimeRuleByString(getLimitTimeRule()));
				interfaceLimitConfig.setLimitType(LimitTypeEnum.getLimitType(getLimitType()));
				interfaceLimitConfig.setDistributedFallbackStrategy(
						DistributedExceptionStrategy.getByCode(getFallbackStrategy()));
				String fallbackInterfaceMaxLimit = getFallbackInterfaceMaxLimit();
				interfaceLimitConfig.setDistributedFallbackStrategyLimitValue(StringUtils.isBlank(fallbackInterfaceMaxLimit) ?
						null : Integer.parseInt(fallbackInterfaceMaxLimit));
				interfaceLimitConfig.setEnableLimit(true);
			}
		}
		limitConfigList.add(interfaceLimitConfig);

		if (RepositoryUtil.getRepositoryType() == RepositoryType.PRODCENTER) {
			// 租户整体限流配置
			url = Constant.TENANT_COMMUNICATION_PROPERTIES_GROUP_URL;
			entryKeyId = tenantId;
			LimitConfig tenantLimitConfig = getInterfaceLimitConfig(url, entryKeyId);
			if (tenantLimitConfig != null) {
				limitConfigList.add(tenantLimitConfig);
			}

			// 租户 OPENAPI/SPI 级别限流配置
			url = transactionType;
			entryKeyId = tenantId + transactionType;
			LimitConfig tenantTransactionTypeLimitConfig = getInterfaceLimitConfig(url, entryKeyId);
			if (tenantTransactionTypeLimitConfig != null) {
				limitConfigList.add(tenantTransactionTypeLimitConfig);
			}
		}
		return limitConfigList;
	}

	private LimitConfig getInterfaceLimitConfig(String url, String entryKeyId) {
		CommunicationPropertiesGroup communicationPropertiesGroup =
				interfaceProdCenterQueryService.queryCommunicationPropertiesGroup(url);
		return buildLimitConfig(communicationPropertiesGroup, entryKeyId);
	}

	private LimitConfig buildLimitConfig(
			CommunicationPropertiesGroup communicationPropertiesGroup, String entryKeyId) {
		LimitConfig limitConfig = null;
		if (communicationPropertiesGroup != null) {
			limitConfig = new LimitConfig();
			limitConfig.setLimitValue(communicationPropertiesGroup.getLimitValue());
			limitConfig.setLimitTimeRule(LimitTimeRuleEnum
					.getTimeRuleByString(communicationPropertiesGroup.getLimitTimeRule()));
			limitConfig.setLimitType(
					LimitTypeEnum.getLimitType(communicationPropertiesGroup.getLimitType()));
			limitConfig.setDistributedFallbackStrategy(DistributedExceptionStrategy
					.getByCode(communicationPropertiesGroup.getDistributedFallbackStrategy()));
			limitConfig.setDistributedFallbackStrategyLimitValue(
					communicationPropertiesGroup.getDistributedFallbackStrategyLimitValue());
			limitConfig.setEntryKeyId(entryKeyId);
			limitConfig.setEnableLimit(true);
		}
		return limitConfig;
	}

	/**
	 * get max limit string between global max limit string and current entity max limit string.
	 * <p>
	 * It will use currentEntityMaxLimitStr if currentEntityMaxLimitStr is not blank and currentEntityMaxLimitStr is not zero.
	 * <p>
	 * If currentEntityMaxLimitStr is blank, will use globalMaxLimitStr.
	 */
	private String getMaxLimit(String key, String globalMaxLimitStr,
							String currentEntityMaxLimitStr) {
		try {
			if (StringUtils.isNotBlank(currentEntityMaxLimitStr)) {
				int currentEntityMaxLimit = Integer.parseInt(currentEntityMaxLimitStr);
				AssertUtil.isTrue(currentEntityMaxLimit != 0, LimitRuleErrorCode.LIMIT_NO_TOKEN,
						"currentEntityMaxLimitStr is zero, no token:" + key);
				return currentEntityMaxLimitStr;
			}

			if (StringUtils.isBlank(globalMaxLimitStr)) {
				return currentEntityMaxLimitStr;
			}
			int globalMaxLimit = Integer.parseInt(globalMaxLimitStr);
			AssertUtil.isTrue(globalMaxLimit != 0, LimitRuleErrorCode.LIMIT_NO_TOKEN,
					"globalMaxLimitStr is zero, no token:" + key);
			return globalMaxLimitStr;
		} catch (GatewayException ex) {
			DigestLoggerUtil.printLimitDigestLog(key,
					StringUtils.isNotBlank(currentEntityMaxLimitStr) ? currentEntityMaxLimitStr
							: globalMaxLimitStr,
					ex.getErrorCode());
			throw ex;
		}
	}

	/**
	 * Getter method for property <tt>tenantMaxLimitStr</tt>.
	 */
	public String getTenantMaxLimitStr() {
		if (RepositoryUtil.getRepositoryType() == RepositoryType.PRODCENTER) {
			return limitProdCenterQueryService.queryLimitConditionGroup().getTenantLimit();
		}
		return tenantMaxLimitStr;
	}

	/**
	 * Getter method for property <tt>clientMaxLimitStr</tt>.
	 */
	public String getClientMaxLimitStr() {
		if (RepositoryUtil.getRepositoryType() == RepositoryType.PRODCENTER) {
			return limitProdCenterQueryService.queryLimitConditionGroup().getClientLimit();
		}
		return clientMaxLimitStr;
	}

	/**
	 * Getter method for property <tt>limitTimeRule</tt>.
	 */
	public String getLimitTimeRule() {
		if (RepositoryUtil.getRepositoryType() == RepositoryType.PRODCENTER) {
			return limitProdCenterQueryService.queryLimitConditionGroup()
					.getDistributedLimitTimeRule();
		}
		return limitTimeRule;
	}

	/**
	 * Getter method for property <tt>fallbackStrategy</tt>.
	 */
	public String getFallbackStrategy() {
		if (RepositoryUtil.getRepositoryType() == RepositoryType.PRODCENTER) {
			return limitProdCenterQueryService.queryLimitConditionGroup()
					.getDistributedFallbackStrategy();
		}
		return fallbackStrategy;
	}

	/**
	 * Getter method for property <tt>fallbackInterfaceMaxLimit</tt>.
	 */
	public String getFallbackInterfaceMaxLimit() {
		if (RepositoryUtil.getRepositoryType() == RepositoryType.PRODCENTER) {
			return limitProdCenterQueryService.queryLimitConditionGroup()
					.getDistributedFallbackStandaloneInterfaceLimit();
		}
		return fallbackInterfaceMaxLimit;
	}

	/**
	 * Getter method for property <tt>fallbackClientMaxLimit</tt>.
	 */
	public String getFallbackClientMaxLimit() {
		if (RepositoryUtil.getRepositoryType() == RepositoryType.PRODCENTER) {
			return limitProdCenterQueryService.queryLimitConditionGroup()
					.getDistributedFallbackStandaloneClientLimit();
		}
		return fallbackClientMaxLimit;
	}

	/**
	 * Getter method for property <tt>fallbackTenantMaxLimit</tt>.
	 */
	public String getFallbackTenantMaxLimit() {
		if (RepositoryUtil.getRepositoryType() == RepositoryType.PRODCENTER) {
			return limitProdCenterQueryService.queryLimitConditionGroup()
					.getDistributedFallbackStandaloneTenantLimit();
		}
		return fallbackTenantMaxLimit;
	}

	/**
	 * get tenant limit for tenant id
	 */
	public String getTenantLimit(String tenantId) {
		return environment.getProperty(Constant.TENANT_ID_LIMIT_CONFIG_PREFIX + tenantId);
	}

	/**
	 * get client limit for client id
	 */
	public String getClientLimit(String clientId) {
		return environment.getProperty(Constant.CLIENT_ID_LIMIT_CONFIG_PREFIX + clientId);
	}
}
