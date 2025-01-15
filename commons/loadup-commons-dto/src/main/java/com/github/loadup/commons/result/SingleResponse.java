package com.github.loadup.commons.result;

/*-
 * #%L
 * loadup-commons-dto
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

import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.util.Objects;

@Getter
@Setter
public class SingleResponse<T> extends Response {
	@Serial
	private static final long serialVersionUID = -3426130425512449976L;

	private T data;

	public static <T> SingleResponse<T> of(T data) {
		SingleResponse<T> response = new SingleResponse<>();
		if (Objects.isNull(data)) {
			response.setResult(Result.buildFailure(CommonResultCodeEnum.NOT_FOUND));
			return response;
		}
		response.setResult(Result.buildSuccess());
		response.setData(data);
		return response;
	}

	public static SingleResponse buildSuccess() {
		SingleResponse response = new SingleResponse();
		response.setResult(Result.buildSuccess());
		return response;
	}

	public static SingleResponse buildFailure() {
		SingleResponse response = new SingleResponse();
		response.setResult(Result.buildFailure());
		return response;
	}

	public static SingleResponse buildFailure(String errCode) {
		SingleResponse response = new SingleResponse();
		response.setResult(Result.buildFailure(errCode));
		return response;
	}

	public static SingleResponse buildFailure(ResultCode errCode) {
		SingleResponse response = new SingleResponse();
		response.setResult(Result.buildFailure(errCode));
		return response;
	}

	public static SingleResponse buildFailure(ResultCode errCode, String errMessage) {
		SingleResponse response = new SingleResponse();
		response.setResult(Result.buildFailure(errCode, errMessage));
		return response;
	}

	public static Response buildFailure(String errCode, String errMessage) {
		Response response = new Response();
		response.setResult(Result.buildFailure(errCode, errMessage));
		return response;
	}

}
