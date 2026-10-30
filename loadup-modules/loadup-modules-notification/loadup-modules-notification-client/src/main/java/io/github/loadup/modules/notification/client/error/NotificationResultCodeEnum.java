/*
 * #%L
 * LoadUp Notification Client
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
package io.github.loadup.modules.notification.client.error;

import io.github.loadup.commons.enums.ResultStatusEnum;
import io.github.loadup.commons.result.ResultCode;

/**
 * Stable public error codes for the notification module.
 */
public enum NotificationResultCodeEnum implements ResultCode {
    INVALID_PAGE("NOTIFICATION_INVALID_PAGE", "Page must be positive and size must be between 1 and 100"),
    RECIPIENTS_MUST_CONTAIN_1_TO_100_USERS(
            "NOTIFICATION_RECIPIENTS_MUST_CONTAIN_1_TO_100_USERS", "recipients must contain 1 to 100 users"),
    INVALID_ACTION_URL("NOTIFICATION_INVALID_ACTION_URL", "actionUrl must be an application-relative path"),
    NOTIFICATION_NOT_FOUND("NOTIFICATION_NOTIFICATION_NOT_FOUND", "notification not found"),
    AUTHENTICATED_USER_IS_REQUIRED("NOTIFICATION_AUTHENTICATED_USER_IS_REQUIRED", "authenticated user is required");
    private final String code;
    private final String message;

    NotificationResultCodeEnum(String code, String message) {
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
