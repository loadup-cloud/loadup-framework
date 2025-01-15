package com.github.loadup.components.gateway.common.exception.util;

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
import com.github.loadup.components.gateway.common.exception.GatewayException;
import com.github.loadup.components.gateway.core.common.GatewayliteErrorCode;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;

/**
 * Assert Util
 */
public final class AssertUtil {

	/**
	 * Expect is true，Actual is false，throw <code>GatewayException</code>
	 *
	 * @throws GatewayException
	 */
	public static void isEqual(String sourceString, String destString) throws GatewayException {
		if (!StringUtils.equals(sourceString, destString)) {
			throw new GatewayException(GatewayliteErrorCode.SYSTEM_ERROR);
		}
	}

	/**
	 *
	 */
	public static void isNotBlank(String... itemArray) {

		for (String item : itemArray) {
			if (StringUtils.isBlank(item)) {
				throw new GatewayException(GatewayliteErrorCode.PARAM_ILLEGAL,
						Arrays.toString(itemArray) + " exist blank");
			}
		}

	}

	/**
	 * Expect is not empty，if actual is <code>null</code>，throw <code>GatewayException</code>
	 *
	 * @throws GatewayException
	 */
	public static void isNotNull(Object object, ErrorCode resultCode) throws GatewayException {
		if (object == null) {
			throw new GatewayException(resultCode);
		}
	}

	/**
	 * Expect is not empty，if actual is <code>null</code>，throw <code>GatewayException</code>
	 */
	public static void isNotNull(Object object) {
		if (object == null) {
			throw new IllegalArgumentException("Parameter is nulll");
		}
	}

	/**
	 * Expect is not empty，if actual is <code>null</code>，throw <code>GatewayException</code>
	 */
	public static void isNotNull(Object object, String msg) {
		if (object == null) {
			throw new IllegalArgumentException(
					(StringUtils.isBlank(msg) ? "Parameter should not be null！" : msg));
		}
	}

	/**
	 * Expect is not empty，if actual is <code>null</code>，throw <code>GatewayException</code>
	 *
	 * @throws GatewayException
	 */
	public static void isNotNull(Object object, GatewayliteErrorCode resutlCode,
								String message) throws GatewayException {
		if (object == null) {
			throw new GatewayException(resutlCode, message);
		}
	}

	/**
	 * Expect is not empty，if actual is <code>null</code>，throw <code>GatewayException</code>
	 *
	 * @throws GatewayException
	 */
	public static void isNotBlank(String text, ErrorCode errorCode) throws GatewayException {
		if (StringUtils.isBlank(text)) {
			throw new GatewayException(errorCode);
		}
	}

	/**
	 * Expect is not empty，if actual is <code>null</code>，throw <code>GatewayException</code>
	 */
	public static void isNotBlank(String text, ErrorCode errorCode, String message) {
		if (StringUtils.isBlank(text)) {
			throw new GatewayException(errorCode, message);
		}
	}

	/**
	 * Expect is true，if actual is <code>null</code>，throw <code>GatewayException</code>
	 */
	public static void isTrue(boolean result, ErrorCode errorCode, String message) {
		if (!result) {
			throw new GatewayException(errorCode, message);
		}
	}

	/**
	 * Expect is false，if actual is <code>null</code>，throw <code>GatewayException</code>
	 */
	public static void isFalse(boolean result, ErrorCode errorCode, String message) {
		if (result) {
			throw new GatewayException(errorCode, message);
		}
	}

}
