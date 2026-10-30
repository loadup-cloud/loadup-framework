/*
 * #%L
 * LoadUp Transfer Client
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
package io.github.loadup.modules.transfer.client.error;

import io.github.loadup.commons.enums.ResultStatusEnum;
import io.github.loadup.commons.result.ResultCode;

/**
 * Stable public error codes for the transfer module.
 */
public enum TransferResultCodeEnum implements ResultCode {
    REQUEST_IS_REQUIRED("TRANSFER_REQUEST_IS_REQUIRED", "request is required"),
    TASK_HAS_NO_RESULT_FILE("TRANSFER_TASK_HAS_NO_RESULT_FILE", "task has no result file"),
    AUTHENTICATED_USER_IS_REQUIRED("TRANSFER_AUTHENTICATED_USER_IS_REQUIRED", "authenticated user is required"),
    MAXINPUTBYTES_MUST_BE_POSITIVE("TRANSFER_MAXINPUTBYTES_MUST_BE_POSITIVE", "maxInputBytes must be positive"),
    MAXOUTPUTBYTES_MUST_BE_POSITIVE("TRANSFER_MAXOUTPUTBYTES_MUST_BE_POSITIVE", "maxOutputBytes must be positive"),
    SOURCE_FILE_EXCEEDS_IMPORT_LIMIT("TRANSFER_SOURCE_FILE_EXCEEDS_IMPORT_LIMIT", "source file exceeds import limit"),
    EXPORT_SOURCE_FORBIDDEN("TRANSFER_EXPORT_SOURCE_FORBIDDEN", "export task must not specify sourceFileId"),
    TASK_NOT_FOUND("TRANSFER_TASK_NOT_FOUND", "task not found"),
    INVALID_PAGE("TRANSFER_INVALID_PAGE", "Page must be positive and size must be between 1 and 100"),
    TASK_NOT_DISPATCHABLE("TRANSFER_TASK_NOT_DISPATCHABLE", "only queued or failed tasks can be dispatched"),
    TASK_WORKER_HAS_NOT_FINISHED("TRANSFER_TASK_WORKER_HAS_NOT_FINISHED", "task worker has not finished"),
    TASK_STATUS_CHANGED("TRANSFER_TASK_STATUS_CHANGED", "task status changed"),
    INVALID_TASK_PROGRESS("TRANSFER_INVALID_TASK_PROGRESS", "invalid task progress"),
    KIND_IS_REQUIRED("TRANSFER_KIND_IS_REQUIRED", "kind is required"),
    UNKNOWN_TRANSFER_HANDLER("TRANSFER_UNKNOWN_TRANSFER_HANDLER", "unknown transfer handler"),
    TOO_MANY_TASK_OPTIONS("TRANSFER_TOO_MANY_TASK_OPTIONS", "too many task options"),
    INVALID_OPTION_NAME("TRANSFER_INVALID_OPTION_NAME", "invalid option name"),
    INVALID_OPTION_VALUE("TRANSFER_INVALID_OPTION_VALUE", "invalid option value"),
    TRANSFER_TASK_IS_NO_LONGER_RUNNING(
            "TRANSFER_TRANSFER_TASK_IS_NO_LONGER_RUNNING", "transfer task is no longer running");
    private final String code;
    private final String message;

    TransferResultCodeEnum(String code, String message) {
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
