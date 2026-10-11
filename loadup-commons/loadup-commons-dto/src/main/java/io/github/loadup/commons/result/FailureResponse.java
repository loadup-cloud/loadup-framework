/*
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
package io.github.loadup.commons.result;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.github.loadup.commons.json.ToStringAsJson;
import io.swagger.v3.oas.annotations.media.Schema;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({"result", "data", "traceId"})
public record FailureResponse<T>(
        @Schema(description = "Result metadata") @JsonProperty("result")
        Result result,

        @Schema(description = "Business data") @JsonProperty("data") @JsonInclude(JsonInclude.Include.ALWAYS)
        T data,

        @Schema(description = "Request trace identifier") @JsonProperty("traceId")
        String traceId)
        implements IResponse<T> {

    @Override
    public Result getResult() {
        return result;
    }

    public static <T> FailureResponse<T> of(ResultCode code) {
        return new FailureResponse<>(Result.buildFailure(code), null, null);
    }

    public static <T> FailureResponse<T> of(String errCode, String errMessage) {
        return new FailureResponse<>(Result.buildFailure(errCode, errMessage), null, null);
    }

    public static <T> FailureResponse<T> of(String errCode, String errMessage, String traceId) {
        return new FailureResponse<>(Result.buildFailure(errCode, errMessage), null, traceId);
    }

    public static <T> FailureResponse<T> of(ResultCode code, String errMessage, String traceId) {
        return new FailureResponse<>(Result.buildFailure(code, errMessage), null, traceId);
    }

    @Override
    public String toString() {
        return ToStringAsJson.reflectionToString(this);
    }
}
