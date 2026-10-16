/*
 * #%L
 * LoadUp Async Result
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
package io.github.loadup.components.asyncresult;

import io.github.loadup.commons.result.ResultCode;

public enum AsyncResultCodeEnum implements ResultCode {
    TIMEOUT("Waiting for the asynchronous result timed out."),
    CAPACITY_EXCEEDED("Asynchronous result capacity is exhausted."),
    CLOSED("Asynchronous result coordination is closed."),
    ACTIVE_TRANSACTION("Dispatch and commit the business transaction before waiting."),
    DISPATCH_FAILED("Message dispatch failed or its outcome is unknown."),
    UNAVAILABLE("Asynchronous result storage is unavailable."),
    INVALID_REPLY("The asynchronous reply is invalid."),
    REMOTE_FAILURE("Asynchronous business processing failed."),
    INTERRUPTED("Waiting for the asynchronous result was interrupted.");
    private final String message;

    AsyncResultCodeEnum(String message) {
        this.message = message;
    }

    public String getCode() {
        return "ASYNC_RESULT_" + name();
    }

    public String getStatus() {
        return "F";
    }

    public String getMessage() {
        return message;
    }
}
