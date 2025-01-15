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

import com.github.loadup.components.gateway.common.util.CacheLogUtil;
import com.github.loadup.components.gateway.core.model.InstConfig;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * <p>
 * InstConfigCache.java
 * </p>
 */
public final class InstConfigCache {

	private static Map<String, InstConfig> instConfigMap = new ConcurrentHashMap<>();

	/**
	 * build the cache
	 */
	public static void putAll(boolean clear, List<InstConfig> instConfigList) {
		if (CollectionUtils.isEmpty(instConfigList)) {
			return;
		}

		Map<String, InstConfig> tempMap = new ConcurrentHashMap<>();
		instConfigList.forEach(instConfig -> tempMap.put(instConfig.getClientId(), instConfig));
		if (clear) {
			instConfigMap = tempMap;
		} else {
			instConfigMap.putAll(tempMap);
		}
		CacheLogUtil.printLog("instConfigMap", instConfigMap);
	}

	/**
	 * get with client Id
	 */
	public static InstConfig getWithClientId(String clientId) {
		return instConfigMap.get(clientId);
	}
}
