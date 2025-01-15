package com.github.loadup.components.gateway.plugin.helper;

/*-
 * #%L
 * communication-rpc-plugin
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

import com.github.loadup.components.gateway.facade.spi.OpenApi;
import com.github.loadup.components.gateway.facade.util.LogUtil;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import static com.github.loadup.components.gateway.core.common.Constant.PATH_CONJUNCTION;

/**
 *
 */
@Component("gatewayRpcClientHelper")
public class RpcClientHelper {
	/**
	 * logger
	 */
	private static final Logger logger = LoggerFactory
			.getLogger(RpcClientHelper.class);

	/**
	 * environment
	 */
	@Resource
	private Environment environment;

	/**
	 * get open api instance
	 *
	 * @throws Exception
	 */
	private OpenApi getOpenApiInstance(String uniqueId, int timeout) throws Exception {
		return null;
	}

	/**
	 * get open api
	 */
	public OpenApi getOpenApi(String interfaceId, int timeout) {
		OpenApi openApi = null;
		try {
			String openApiServiceName = StringUtils.split(interfaceId, PATH_CONJUNCTION)[1];
			openApi = getOpenApiInstance(openApiServiceName, timeout);
		} catch (Exception e) {
			LogUtil.error(logger, "create open api service instance error, interfaceId=",
					interfaceId, e);
		}
		return openApi;
	}
}
