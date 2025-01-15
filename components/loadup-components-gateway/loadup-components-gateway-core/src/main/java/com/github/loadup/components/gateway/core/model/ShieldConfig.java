package com.github.loadup.components.gateway.core.model;

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

import com.github.loadup.components.gateway.common.util.RepositoryUtil;
import com.github.loadup.components.gateway.core.common.Constant;
import com.github.loadup.components.gateway.core.common.enums.InterfaceType;
import com.github.loadup.components.gateway.core.common.enums.RepositoryType;
import com.github.loadup.components.gateway.core.service.InterfaceProdCenterQueryService;
import com.github.loadup.components.gateway.facade.config.model.APIConditionGroup;
import com.github.loadup.components.gateway.facade.config.model.CommunicationPropertiesGroup;
import jakarta.annotation.Resource;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

/**
 * ShieldConfig
 */
@Component
public class ShieldConfig {

	/**
	 * default key
	 */
	public static final String KEY_DEFAULT = "DEFAULT";

	/**
	 * shield config cache
	 * ie. Map<interfaceId, Map<path, ShieldType>>
	 * if no interface id, the key default as DEFAULT
	 */
	private static Map<String, Map<String, ShieldType>> shieldConfig = new ConcurrentHashMap<>();

	/**
	 * separator "|"
	 */
	public static final String SEPARATOR = "|";

	private static InterfaceProdCenterQueryService interfaceProdCenterQueryService;

	public static Map<String, ShieldType> getShieldRules(String interfaceId,
														InterfaceType interfaceType) {
		Map<String, ShieldType> result = new HashMap<>();
		Map<String, ShieldType> defaultMap = shieldConfig.get(KEY_DEFAULT);
		if (MapUtils.isNotEmpty(defaultMap)) {
			result.putAll(defaultMap);
		}

		if (StringUtils.isNotBlank(interfaceId)) {
			Map<String, ShieldType> interfaceMap = shieldConfig.get(interfaceId);
			if (MapUtils.isNotEmpty(interfaceMap)) {
				result.putAll(interfaceMap);
			}
		}

		Map<String, ShieldType> prodShieldRules = getProdShieldRules(interfaceId, interfaceType);
		if (MapUtils.isNotEmpty(prodShieldRules)) {
			result.putAll(prodShieldRules);
		}

		return result;
	}

	/**
	 *
	 */
	private static Map<String, ShieldType> getProdShieldRules(String interfaceId,
															InterfaceType type) {
		Map<String, ShieldType> rules = new HashMap<>();
		if (RepositoryUtil.getRepositoryType() == RepositoryType.PRODCENTER) {
			//                && StringUtils.isNotBlank(TenantUtil.getTenantId())) {

			CommunicationPropertiesGroup propertiesGroup = interfaceProdCenterQueryService.
					queryCommunicationPropertiesGroup(interfaceId);

			if (propertiesGroup == null && type == InterfaceType.OPENAPI) {
				APIConditionGroup apiConditionGroup = interfaceProdCenterQueryService.
						queryAPIConditionGroup(interfaceId, null);
				if (apiConditionGroup != null) {
					propertiesGroup = interfaceProdCenterQueryService.
							queryCommunicationPropertiesGroup(apiConditionGroup.getIntegrationUrl());
				}
			}

			CommunicationPropertiesGroup allPropertiesGroup = interfaceProdCenterQueryService
					.queryCommunicationPropertiesGroup(Constant.TENANT_COMMUNICATION_PROPERTIES_GROUP_URL);
			CommunicationPropertiesGroup typePropertiesGroup = null;
			if (type != null) {
				typePropertiesGroup = interfaceProdCenterQueryService
						.queryCommunicationPropertiesGroup(type.name());
			}

			rules.putAll(convertProdConfigToShieldRules(propertiesGroup));
			rules.putAll(convertProdConfigToShieldRules(allPropertiesGroup));
			rules.putAll(convertProdConfigToShieldRules(typePropertiesGroup));

		}
		return rules;
	}

	/**
	 *
	 */
	private static Map<String, ShieldType> convertProdConfigToShieldRules(CommunicationPropertiesGroup communicationPropertiesGroup) {
		Map<String, ShieldType> rules = new HashMap<>();
		if (communicationPropertiesGroup != null) {
			Stream.of(StringUtils.split(
					StringUtils.defaultString(communicationPropertiesGroup.getShieldKeys(), ""),
					Constant.COMMA_SEPARATOR)).forEach(m -> {
				// 目前只支持一种类型，全部隐藏
				rules.put(m, ShieldType.ALL);
			});
		}
		return rules;
	}

	/**
	 * Put shield config for default.
	 */
	public static void putShiledConfig(String key, ShieldType shieldType) {

		if (!shieldConfig.containsKey(KEY_DEFAULT)) {
			shieldConfig.put(KEY_DEFAULT, new ConcurrentHashMap<String, ShieldType>(16));
		}
		shieldConfig.get(KEY_DEFAULT).put(key, shieldType);
	}

	/**
	 * Put shield config by interface id
	 * If no interface, use DEFAULT
	 */
	public static void putShieldConfig(String interfaceId, String key, ShieldType shieldType) {
		if (!shieldConfig.containsKey(interfaceId)) {
			shieldConfig.put(interfaceId, new ConcurrentHashMap<String, ShieldType>(16));
		}
		shieldConfig.get(interfaceId).put(key, shieldType);
	}

	/**
	 * Clear all config cache
	 */
	public static void clearCache() {
		shieldConfig = new ConcurrentHashMap<String, Map<String, ShieldType>>();
	}

	/**
	 * Clear cache by interfaceId
	 */
	public static void clearCacheByInterface(String interfaceId) {
		shieldConfig.remove(interfaceId);
	}

	/**
	 * Setter method for property <tt>interfaceProdCenterQueryService</tt>.
	 */
	@Resource
	public void setInterfaceProdCenterQueryService(InterfaceProdCenterQueryService interfaceProdCenterQueryService) {
		ShieldConfig.interfaceProdCenterQueryService = interfaceProdCenterQueryService;
	}
}
