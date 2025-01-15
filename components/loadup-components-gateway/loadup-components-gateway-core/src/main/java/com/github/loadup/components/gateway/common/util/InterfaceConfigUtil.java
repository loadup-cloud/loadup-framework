package com.github.loadup.components.gateway.common.util;

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

import com.github.loadup.components.gateway.core.common.Constant;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;

import static com.github.loadup.components.gateway.core.common.Constant.PATH_CONJUNCTION;
import static com.github.loadup.components.gateway.core.common.Constant.PATH_SEPARATOR;

/**
 *
 */
public class InterfaceConfigUtil {

	/**
	 *
	 */
	public static String generateInterfaceId(String url, String tenantId, String version, String type,
											Map<String, String> communicationProperties) {

		if (MapUtils.isNotEmpty(communicationProperties)) {
			String customInterfaceId = communicationProperties.get(Constant.INTERFACE_ID);
			if (StringUtils.isNotBlank(customInterfaceId)) {
				return customInterfaceId;
			}
		}

		String subInterfaceId = StringUtils.replace(UriUtil.getURIPath(url), PATH_SEPARATOR, PATH_CONJUNCTION);

		return StringUtils.defaultIfBlank(tenantId, "platform") + PATH_CONJUNCTION + type
				+ PATH_CONJUNCTION + subInterfaceId + PATH_CONJUNCTION + version;
	}
}
