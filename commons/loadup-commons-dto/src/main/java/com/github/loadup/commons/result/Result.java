package com.github.loadup.commons.result;

/*-
 * #%L
 * loadup-commons-lang
 * %%
 * Copyright (C) 2022 - 2024 loadup_cloud
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

import com.github.loadup.commons.dto.DTO;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

import java.io.Serial;
import java.io.Serializable;

/**
 * @author Laysan
 * @since 1.0.0
 */
@Getter
@Setter
public class Result extends DTO implements ResultCode, Serializable {

	@Serial
	private static final long serialVersionUID = -6017673453793484984L;

	private String code;

	/**
	 * resultCode
	 */
	private String status;

	/**
	 * resultMsg
	 */
	private String message;

	public static Result buildSuccess() {
		Result result = new Result();
		result.setCode(CommonResultCodeEnum.SUCCESS.getCode());
		result.setStatus(ResultStatusEnum.SUCCESS.getCode());
		result.setMessage(CommonResultCodeEnum.SUCCESS.getMessage());
		return result;
	}

	public static Result buildFailure() {
		Result result = new Result();
		result.setCode(CommonResultCodeEnum.PROCESS_FAIL.getCode());
		result.setStatus(CommonResultCodeEnum.PROCESS_FAIL.getStatus());
		result.setMessage(CommonResultCodeEnum.PROCESS_FAIL.getMessage());
		return result;
	}

	public static Result buildFailure(String errorCode, String errorMessage) {
		Result result = new Result();
		CommonResultCodeEnum resultCodeEnum = CommonResultCodeEnum.getByResultCode(errorCode);
		result.setCode(resultCodeEnum.getCode());
		result.setStatus(resultCodeEnum.getStatus());
		result.setMessage(resultCodeEnum.getMessage());
		if (StringUtils.isNotBlank(errorMessage)) {
			result.setMessage(errorMessage);
		}
		return result;
	}

	public static Result buildFailure(String errorCode) {
		Result result = new Result();
		CommonResultCodeEnum resultCodeEnum = CommonResultCodeEnum.getByResultCode(errorCode);
		result.setCode(resultCodeEnum.getCode());
		result.setStatus(resultCodeEnum.getStatus());
		result.setMessage(resultCodeEnum.getMessage());
		return result;
	}

	public static Result buildFailure(ResultCode errorCode, String errorMessage) {
		Result result = new Result();
		result.setCode(errorCode.getCode());
		result.setStatus(errorCode.getStatus());
		result.setMessage(errorCode.getMessage());
		if (StringUtils.isNotBlank(errorMessage)) {
			result.setMessage(errorMessage);
		}
		return result;
	}

	public static Result buildFailure(ResultCode errorCode) {
		Result result = new Result();
		result.setCode(errorCode.getCode());
		result.setStatus(errorCode.getStatus());
		result.setMessage(errorCode.getMessage());
		return result;
	}
}
