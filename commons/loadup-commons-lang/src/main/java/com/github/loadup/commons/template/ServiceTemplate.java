package com.github.loadup.commons.template;

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

import com.github.loadup.commons.error.AssertUtil;
import com.github.loadup.commons.error.CommonException;
import com.github.loadup.commons.result.*;
import lombok.extern.slf4j.Slf4j;

import java.util.Objects;
import java.util.function.*;

@Slf4j(topic = "SERVICE-LOGGER")
public final class ServiceTemplate {
	private ServiceTemplate() {
	}

	public static <T extends Response> T execute(Consumer<Void> checkParameter, //checkParameter
												Supplier<T> process, //process
												Function<Exception, Result> composeExceptionResponse, // 修改为 Function<Throwable, T>
												Consumer<Void> composeDigestLog //composeDigestLog
	) {
		T response = null;
		try {
			checkParameter.accept(null); // 执行参数检查
			response = process.get();   // 执行业务逻辑
			AssertUtil.notNull(response);
			response.setResult(Result.buildSuccess());
		} catch (CommonException exception) {
			log.error("service process, exception occurred:", exception.getMessage());
			response.setResult(Result.buildFailure(exception.getResultCode()));
		} catch (Exception throwable) {
			log.error("service process,  exception occurred:", throwable.getMessage());
			Result result = composeExceptionResponse.apply(throwable); // 异常处理
			if (Objects.isNull(result)) {
				result = Result.buildFailure(CommonResultCodeEnum.UNKNOWN);
			}
			response.setResult(result);
		} catch (Throwable throwable) {
			log.error("service process, unknown exception occurred:", throwable.getMessage());
			response.setResult(Result.buildFailure(CommonResultCodeEnum.UNKNOWN));
		} finally {
			composeDigestLog.accept(null);  // 执行日志
		}
		return response;
	}

}
