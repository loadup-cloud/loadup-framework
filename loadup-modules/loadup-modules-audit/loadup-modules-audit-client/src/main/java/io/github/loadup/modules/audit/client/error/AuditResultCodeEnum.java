/*
 * #%L
 * LoadUp Audit Client
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
package io.github.loadup.modules.audit.client.error;

import io.github.loadup.commons.enums.ResultStatusEnum;
import io.github.loadup.commons.result.ResultCode;

/**
 * Stable public error codes for the audit module.
 */
public enum AuditResultCodeEnum implements ResultCode {
    INVALID_TIME_RANGE("AUDIT_INVALID_TIME_RANGE", "from must not be after to"),
    INVALID_PAGE("AUDIT_INVALID_PAGE", "Page must be positive and size must be between 1 and 100"),
    AUDIT_ACTION_IS_REQUIRED("AUDIT_AUDIT_ACTION_IS_REQUIRED", "audit action is required");
    private final String code;
    private final String message;

    AuditResultCodeEnum(String code, String message) {
        this.code = code;
        this.message = message;
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public String getStatus() {
        return ResultStatusEnum.FAIL.getCode();
    }
}
