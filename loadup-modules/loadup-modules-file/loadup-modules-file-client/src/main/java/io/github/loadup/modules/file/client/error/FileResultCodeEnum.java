/*
 * #%L
 * LoadUp File Client
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
package io.github.loadup.modules.file.client.error;

import io.github.loadup.commons.enums.ResultStatusEnum;
import io.github.loadup.commons.result.ResultCode;

/** Stable public error codes for the file module. */
public enum FileResultCodeEnum implements ResultCode {
    INVALID_PAGE("FILE_INVALID_PAGE", "Page must be positive and size must be between 1 and 100"),
    AUTHENTICATED_USER_IS_REQUIRED("FILE_AUTHENTICATED_USER_IS_REQUIRED", "authenticated user is required"),
    FILENAME_CONTAINS_INVALID_CHARACTERS(
            "FILE_FILENAME_CONTAINS_INVALID_CHARACTERS", "filename contains invalid characters"),
    INVALID_FILE_CONTENT("FILE_INVALID_FILE_CONTENT", "content and nonnegative size are required"),
    FILE_NOT_FOUND("FILE_FILE_NOT_FOUND", "file not found"),
    FILE_IS_REFERENCED_BY_A_BUSINESS_OBJECT(
            "FILE_FILE_IS_REFERENCED_BY_A_BUSINESS_OBJECT", "file is referenced by a business object"),
    LIMIT_MUST_BE_BETWEEN_1_AND_1000("FILE_LIMIT_MUST_BE_BETWEEN_1_AND_1000", "limit must be between 1 and 1000"),
    SOME_PENDING_FILE_DELETIONS_FAILED("FILE_SOME_PENDING_FILE_DELETIONS_FAILED", "some pending file deletions failed"),
    FILE_IS_NOT_PENDING_DELETION("FILE_FILE_IS_NOT_PENDING_DELETION", "file is not pending deletion");
    private final String code;
    private final String message;

    FileResultCodeEnum(String code, String message) {
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
