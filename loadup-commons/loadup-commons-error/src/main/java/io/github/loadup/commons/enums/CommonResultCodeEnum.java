/*
 * #%L
 * LoadUp Common Error
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
package io.github.loadup.commons.enums;

import io.github.loadup.commons.result.ResultCode;
import java.util.Arrays;

public enum CommonResultCodeEnum implements ResultCode {
    SUCCESS(ResultStatusEnum.SUCCESS, "Success."),
    UNKNOWN(ResultStatusEnum.UNKNOWN, "Unknown failed."),
    PARAM_ILLEGAL(ResultStatusEnum.FAIL, "Parameter illegal."),
    PROCESS_FAIL(ResultStatusEnum.FAIL, "Process fail."),
    ACCESS_DENIED(ResultStatusEnum.FAIL, "Access denied."),
    UNAUTHENTICATED(ResultStatusEnum.FAIL, "Authentication required or invalid token."),
    INVALID_CLIENT(ResultStatusEnum.FAIL, "Invalid client."),
    NOT_FOUND(ResultStatusEnum.FAIL, "Key is not found."),
    CONFLICT(ResultStatusEnum.FAIL, "Resource conflict."),
    INVALID_STATE(ResultStatusEnum.FAIL, "Operation not allowed in the current state."),
    RATE_LIMITED(ResultStatusEnum.FAIL, "Request rate limit exceeded."),
    SYS_ERROR(ResultStatusEnum.FAIL, "System error."),
    ;
    private final String status;

    private final String message;

    CommonResultCodeEnum(ResultStatusEnum status, String message) {
        this.status = status.getCode();
        this.message = message;
    }

    public static CommonResultCodeEnum getByResultCode(String resultCode) {
        return Arrays.stream(CommonResultCodeEnum.values())
                .filter(value -> value.getCode().equalsIgnoreCase(resultCode))
                .findFirst()
                .orElse(CommonResultCodeEnum.SYS_ERROR);
    }

    @Override
    public String getCode() {
        return name();
    }

    @Override
    public String getStatus() {
        return status;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
