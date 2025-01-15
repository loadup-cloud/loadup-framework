package com.github.loadup.components.gateway.message.common.errorr;

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
import com.github.loadup.components.gateway.core.common.GatewayliteErrorCode;
import org.apache.commons.lang3.StringUtils;

/**
 * <p>
 * ParserErrorCode.java
 * </p>
 */
public enum ParserErrorCode implements ErrorCode {

	/**
	 * PARSE_ERROR
	 */
	PARSE_ERROR("PARSE_ERROR", "parse error", "U"),

	/**
	 * NOT_EXIST_SCRIPT
	 */
	NOT_EXIST_SCRIPT("NOT_EXIST_SCRIPT", "not exist script", "F");

	/**
	 * error message
	 */
	private final String message;

	/**
	 * error code
	 */
	private final String code;

	private final String status;

	ParserErrorCode(String code, String message, String status) {
		this.code = code;
		this.message = message;
		this.status = status;
	}

	/**
	 * 获取枚举值
	 */
	public static GatewayliteErrorCode getEnumByCode(String code) {
		for (GatewayliteErrorCode codeEnum : GatewayliteErrorCode.values()) {
			if (StringUtils.equals(code, codeEnum.getCode())) {
				return codeEnum;
			}
		}
		return null;
	}

	/**
	 * Gets get code.
	 */
	@Override
	public String getCode() {
		return this.code;
	}

	/**
	 * Gets get message.
	 */
	@Override
	public String getMessage() {
		return this.message;
	}

	/**
	 * Get the status
	 */
	@Override
	public String getStatus() {
		return this.status;
	}

}
