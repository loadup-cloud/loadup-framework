package io.github.loadup.commons.result;

/*-
 * #%L
 * Loadup Common DTO
 * %%
 * Copyright (C) 2025 - 2026 LoadUp Cloud
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

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.github.loadup.framework.api.result.ResultCode;
import io.swagger.v3.oas.annotations.media.Schema;

/** 失败响应实现 使用 Record 保证数据不可变，通过 Jackson 注解确保 JSON 格式平整 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({"result", "traceId"})
public record FailureResponse<T>(
        @Schema(description = "结果元数据") @JsonProperty("result")
        Result result,

        @Schema(description = "请求追踪ID") @JsonProperty("traceId")
        String traceId)
        implements IResponse<T> {

    @Override
    public Result getResult() {
        return result;
    }

    // --- 静态工厂方法 ---

    public static <T> FailureResponse<T> of(ResultCode code) {
        return new FailureResponse<>(Result.buildFailure(code), null);
    }

    public static <T> FailureResponse<T> of(String errCode, String errMessage) {
        return new FailureResponse<>(Result.buildFailure(errCode, errMessage), null);
    }

    /** 带 TraceId 的工厂方法（推荐在 GlobalExceptionHandler 中使用） */
    public static <T> FailureResponse<T> of(String errCode, String errMessage, String traceId) {
        return new FailureResponse<>(Result.buildFailure(errCode, errMessage), traceId);
    }

    public static <T> FailureResponse<T> of(ResultCode code, String errMessage, String traceId) {
        return new FailureResponse<>(Result.buildFailure(code, errMessage), traceId);
    }
}
