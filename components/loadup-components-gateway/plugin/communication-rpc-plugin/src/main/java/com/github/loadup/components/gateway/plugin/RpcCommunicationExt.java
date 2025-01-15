package com.github.loadup.components.gateway.plugin;

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

import com.alibaba.cola.extension.Extension;
import com.github.loadup.components.gateway.common.exception.GatewayException;
import com.github.loadup.components.gateway.core.common.GatewayliteErrorCode;
import com.github.loadup.components.gateway.core.prototype.util.SerializationUtil;
import com.github.loadup.components.gateway.facade.extpoint.CommunicationProxyExtPt;
import com.github.loadup.components.gateway.facade.model.CommunicationConfiguration;
import com.github.loadup.components.gateway.facade.request.OpenApiTransRequest;
import com.github.loadup.components.gateway.facade.response.OpenApiTransResponse;
import com.github.loadup.components.gateway.facade.spi.OpenApi;
import com.github.loadup.components.gateway.facade.util.LogUtil;
import com.github.loadup.components.gateway.plugin.helper.RpcClientHelper;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

import static com.github.loadup.components.gateway.core.common.Constant.*;

/**
 *
 */
@Extension(bizId = "RPC")
@Component("rpcCommunicationExt")
public class RpcCommunicationExt implements CommunicationProxyExtPt {
	/**
	 * logger
	 */
	private static final Logger logger = LoggerFactory
			.getLogger(RpcCommunicationExt.class);

	/**
	 * default rpc time out
	 */
	private static final int DEFAULT_TIME_OUT = 3000;

	/**
	 * value separator
	 */
	private static final String VALUE_SEPARATOR = "=";

	/**
	 * key value separator
	 */
	private static final String KEY_VALUE_SEPARATOR = "&";

	/**
	 * query string separator
	 */
	private static final String QUERY_STRING_SEPARATOR = "?";

	/**
	 * timout key name
	 */
	private static final String TIMEOUT_KEY = "timeout";

	@Resource
	private RpcClientHelper rpcClientHelper;

	@Value("${gateway.extension.dateformat:}")
	private String dateFormat;

	@Override
	public String sendMessage(CommunicationConfiguration config, String messageContent) {
		try {
			// get time out from config
			int timeout = Integer.parseInt(config.getProperties().get(CONNECTION_TIMEOUT));
			String interfaceId = getInterfaceId(config.getUri());
			// 1.2 get openapi service
			OpenApi openApi = rpcClientHelper.getOpenApi(interfaceId, timeout);
			// 1.3 init request
			OpenApiTransRequest openApiTransRequest = new OpenApiTransRequest();
			openApiTransRequest.setInterfaceId(interfaceId);
			openApiTransRequest.setMessage(messageContent);
			// 1.4 invoke
			OpenApiTransResponse openApiTransResponse = openApi.invoke(openApiTransRequest);
			if (openApiTransResponse == null) {
				throw new GatewayException(GatewayliteErrorCode.PROCESS_FAIL);
			}
			return SerializationUtil.serializeWithDateFormat(openApiTransResponse.getMessage(), dateFormat);
		} catch (Exception e) {
			LogUtil.error(logger, e,
					"RPC_CALL_ERROR_PREX, Failed to send message because of error occurs when trying to get rpc bean with config:"
							+ config.getUri());
			throw new RuntimeException("Failed to send message.", e);
		}
	}

	/**
	 * get rpc timeout
	 */
	private int getTimeout(String uri) {
		uri = StringUtils.split(uri, URI_SEPARATOR)[1];
		int queryIndex = StringUtils.indexOf(uri, QUERY_STRING_SEPARATOR);
		if (queryIndex != -1) {
			Map<String, String> params = getParams(StringUtils.substring(uri, queryIndex + 1));
			return NumberUtils.toInt(params.get(TIMEOUT_KEY), DEFAULT_TIME_OUT);
		}
		return DEFAULT_TIME_OUT;
	}

	/**
	 * get query params map
	 */
	private Map<String, String> getParams(String queryParams) {
		Map<String, String> configMap = new HashMap<String, String>();
		if (StringUtils.isEmpty(queryParams)) {
			return configMap;
		}
		String[] configs = queryParams.split(KEY_VALUE_SEPARATOR);
		for (String config : configs) {
			int firstSplitorIndex = config.indexOf(VALUE_SEPARATOR);
			String keyString = StringUtils.substring(config, 0,
					firstSplitorIndex);
			keyString = StringUtils.trim(keyString);
			configMap.put(keyString, config.substring(firstSplitorIndex + 1));
		}

		return configMap;
	}

	private String getInterfaceId(String uri) {
		String interfaceId = StringUtils.replace(uri, URI_SEPARATOR, PATH_CONJUNCTION);
		return StringUtils.replace(interfaceId, PATH_SEPARATOR, PATH_CONJUNCTION);
	}
}
