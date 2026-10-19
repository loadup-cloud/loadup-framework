/*
 * #%L
 * LoadUp Components DelayedTask
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
package io.github.loadup.components.delayedtask;

import io.github.loadup.commons.enums.ResultStatusEnum;
import io.github.loadup.commons.result.ResultCode;

public enum DelayedTaskResultCodeEnum implements ResultCode {
    INVALID_ARGUMENT("Delayed task argument is invalid"),
    TRANSACTION_REQUIRED("Delayed task registration requires an active transaction"),
    INVALID_STATE("Delayed task state does not permit this operation"),
    CONFLICT("Delayed task already exists with a different deadline"),
    HANDLER_MISSING("Delayed task handler is not registered"),
    HANDLER_FAILED("Delayed task handler failed");
    private final String message;

    DelayedTaskResultCodeEnum(String message) {
        this.message = message;
    }

    public String getCode() {
        return "DELAYED_TASK_" + name();
    }

    public String getStatus() {
        return ResultStatusEnum.FAIL.getCode();
    }

    public String getMessage() {
        return message;
    }
}
