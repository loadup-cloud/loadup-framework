package io.github.loadup.modules.upms.client.constant;

/*-
 * #%L
 * Loadup Modules UPMS Client Layer
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

import io.github.loadup.commons.result.ResultStatusEnum;
import io.github.loadup.framework.api.result.ResultCode;
import java.util.Arrays;
import org.apache.commons.lang3.Strings;

public enum UpmsResultCode implements ResultCode {
    USER_NOT_FOUND(ResultStatusEnum.FAIL, "用户不存在"),
    USER_LOCKED(ResultStatusEnum.FAIL, "用户已被锁定"),
    PASSWORD_ERROR(ResultStatusEnum.FAIL, "密码错误"),
    UNAUTHORIZED(ResultStatusEnum.FAIL, "无权访问该资源"),
    ROLE_NOT_FOUND(ResultStatusEnum.FAIL, "角色不存在");

    private final String status;

    private final String message;

    UpmsResultCode(ResultStatusEnum status, String message) {
        this.status = status.getCode();
        this.message = message;
    }

    public static UpmsResultCode getByResultCode(String resultCode) {
        return Arrays.stream(UpmsResultCode.values())
                .filter(value -> Strings.CI.equals(value.getCode(), resultCode))
                .findFirst()
                .orElse(null);
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
