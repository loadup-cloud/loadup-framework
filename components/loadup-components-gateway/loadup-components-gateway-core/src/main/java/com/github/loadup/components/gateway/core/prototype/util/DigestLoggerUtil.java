package com.github.loadup.components.gateway.core.prototype.util;

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

import com.github.loadup.components.gateway.common.exception.ErrorCode;
import com.github.loadup.components.gateway.core.ctrl.context.GatewayRuntimeProcessContext;
import com.github.loadup.components.gateway.facade.util.LogUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>:DigestLoggerUtil.java
 */
public class DigestLoggerUtil {
	/**
	 * message digest
	 */
	public static final Logger digestMessageLogger = LoggerFactory
			.getLogger("DIGEST-MESSAGE-LOGGER");

	/**
	 * http digest
	 */
	public static final Logger DIGEST_HTTP_LOGGER = LoggerFactory
			.getLogger("DIGEST-HTTP-LOGGER");

	/**
	 * count error logger
	 */
	public static final Logger COUNT_ERROR_LOGGER = LoggerFactory
			.getLogger("COUNT-ERROR-LOGGER");

	/**
	 * limit digest logger
	 */
	public static final Logger digestLimitLogger = LoggerFactory
			.getLogger("DIGEST-LIMIT-LOGGER");

	/**
	 * 通用日志打印方法，屏蔽敏感日志
	 */
	public static void printInfoLog(GatewayRuntimeProcessContext processContext, long timeCost) {

	}

	/**
	 * 通用日志打印方法, 打印调用方耗时信息
	 */
	public static void printSimpleDigestLog(String url, String respMsg, long timeCost) {
		LogUtil.info(DIGEST_HTTP_LOGGER, url, ", ", respMsg, ", ", timeCost);
	}

	/**
	 * print digest limit log
	 */
	public static void printLimitDigestLog(String entityId, String maxLimit, ErrorCode errorCode) {
		LogUtil.info(digestLimitLogger, entityId, ", ", maxLimit, ", ", errorCode);
	}

}
