package io.github.loadup.commons.enums;

/*-
 * #%L
 * loadup-commons-dto
 * %%
 * Copyright (C) 2022 - 2024 loadup_cloud
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

import io.github.loadup.commons.result.ResultStatusEnum;
import io.github.loadup.framework.api.result.ResultCode;
import java.util.Arrays;
import org.apache.commons.lang3.Strings;

public enum CommonResultCodeEnum implements ResultCode {
    SUCCESS(ResultStatusEnum.SUCCESS, "Success."),
    UNKNOWN(ResultStatusEnum.UNKNOWN, "Unknown failed."),
    PARAM_ILLEGAL(ResultStatusEnum.FAIL, "Parameter illegal."),
    PROCESS_FAIL(ResultStatusEnum.FAIL, "Process fail."),
    ACCESS_DENIED(ResultStatusEnum.FAIL, "Access denied."),
    INVALID_CLIENT(ResultStatusEnum.FAIL, "Invalid client."),
    NOT_FOUND(ResultStatusEnum.FAIL, "Key is not found."),
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
                .filter(value -> Strings.CI.equals(value.getCode(), resultCode))
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
