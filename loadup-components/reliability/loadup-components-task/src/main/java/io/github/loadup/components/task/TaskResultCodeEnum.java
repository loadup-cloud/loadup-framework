/*
 * Copyright (C) 2026 LoadUp Cloud
 *
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
 */
package io.github.loadup.components.task;

import io.github.loadup.commons.enums.ResultStatusEnum;
import io.github.loadup.commons.result.ResultCode;

public enum TaskResultCodeEnum implements ResultCode {
    INVALID_ARGUMENT("Task argument is invalid"),
    TRANSACTION_REQUIRED("Task registration requires an active transaction"),
    INVALID_STATE("Task state does not permit this operation"),
    CONFLICT("Task deduplication key conflicts with the submitted payload"),
    HANDLER_MISSING("Task handler is not registered"),
    HANDLER_FAILED("Task handler failed");
    private final String message;

    TaskResultCodeEnum(String message) {
        this.message = message;
    }

    public String getCode() {
        return "TASK_" + name();
    }

    public String getStatus() {
        return ResultStatusEnum.FAIL.getCode();
    }

    public String getMessage() {
        return message;
    }
}
