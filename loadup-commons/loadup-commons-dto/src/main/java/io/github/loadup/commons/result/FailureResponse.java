package io.github.loadup.commons.result;

/*-
 * #%L
 * Loadup Common DTO
 * %%
 * Copyright (C) 2025 - 2026 LoadUp Cloud
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 失败响应实现 使用 Record 保证数据不可变，通过 Jackson 注解确保 JSON 格式平整
 */
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

    /**
     * 带 TraceId 的工厂方法（推荐在 GlobalExceptionHandler 中使用）
     */
    public static <T> FailureResponse<T> of(String errCode, String errMessage, String traceId) {
        return new FailureResponse<>(Result.buildFailure(errCode, errMessage), traceId);
    }

    public static <T> FailureResponse<T> of(ResultCode code, String errMessage, String traceId) {
        return new FailureResponse<>(Result.buildFailure(code, errMessage), traceId);
    }
}
