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

import io.github.loadup.commons.dto.DTO;
import io.github.loadup.commons.enums.CommonResultCodeEnum;
import io.github.loadup.commons.json.ToStringAsJson;
import java.io.Serial;
import java.io.Serializable;

/** Immutable wire metadata copied from a declared result-code enum. */
public final class Result implements DTO, Serializable {
    @Serial
    private static final long serialVersionUID = -6017673453793484984L;

    private final String code;
    private final String status;
    private final String message;

    private Result(ResultCode value) {
        this.code = value.getCode();
        this.status = value.getStatus();
        this.message = value.getMessage();
    }

    @com.fasterxml.jackson.annotation.JsonCreator
    private Result(
            @com.fasterxml.jackson.annotation.JsonProperty("code") String code,
            @com.fasterxml.jackson.annotation.JsonProperty("status") String status,
            @com.fasterxml.jackson.annotation.JsonProperty("message") String message) {
        this.code = code;
        this.status = status;
        this.message = message;
    }

    public static Result buildSuccess() {
        return new Result(CommonResultCodeEnum.SUCCESS);
    }

    public static Result buildFailure() {
        return buildFailure(CommonResultCodeEnum.PROCESS_FAIL);
    }

    public static Result buildFailure(ResultCode value) {
        return new Result(ResultCode.requireFailure(value));
    }

    public String getCode() {
        return code;
    }

    public String getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public String toString() {
        return ToStringAsJson.reflectionToString(this);
    }
}
