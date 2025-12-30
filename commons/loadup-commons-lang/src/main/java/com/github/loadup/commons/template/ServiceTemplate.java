package com.github.loadup.commons.template;

/*-
 * #%L
 * loadup-commons-dto
 * %%
 * Copyright (C) 2022 - 2024 loadup_cloud
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

import com.github.loadup.commons.error.AssertUtil;
import com.github.loadup.commons.error.CommonException;
import com.github.loadup.commons.result.*;
import lombok.extern.slf4j.Slf4j;

import java.util.function.*;

@Slf4j(topic = "SERVICE-LOGGER")
public final class ServiceTemplate {
    private ServiceTemplate() {}

    public static <T extends Response> T execute(
        Consumer<Void> checkParameter, // checkParameter
        Supplier<T> process, // process
        Function<Result, T> composeExceptionResponse, // 修改为 Function<Throwable, T>
        Consumer<Void> composeDigestLog // composeDigestLog
    ) {
        T response = null;
        try {
            checkParameter.accept(null); // 执行参数检查
            response = process.get(); // 执行业务逻辑
            AssertUtil.notNull(response);
            response.setResult(Result.buildSuccess());
        } catch (CommonException exception) {
            log.error("service process, exception occurred:", exception.getMessage());
            response = composeExceptionResponse.apply(Result.buildFailure(exception.getResultCode())); // 异常处理
        } catch (Exception throwable) {
            log.error("service process,  exception occurred:{}", throwable.getMessage());
            response = composeExceptionResponse.apply(Result.buildFailure(CommonResultCodeEnum.UNKNOWN)); // 异常处理
        } catch (Throwable throwable) {
            log.error("service process, unknown exception occurred:", throwable.getMessage());
            response = composeExceptionResponse.apply(Result.buildFailure(CommonResultCodeEnum.UNKNOWN)); // 异常处理
        } finally {
            composeDigestLog.accept(null); // 执行日志
        }
        return response;
    }
}
