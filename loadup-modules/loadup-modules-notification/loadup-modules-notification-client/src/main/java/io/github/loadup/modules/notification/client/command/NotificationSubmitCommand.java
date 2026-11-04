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
package io.github.loadup.modules.notification.client.command;

import io.github.loadup.commons.error.CommonException;
import io.github.loadup.commons.json.ToStringAsJson;
import io.github.loadup.modules.notification.client.error.NotificationResultCodeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.List;

public record NotificationSubmitCommand(
        @Schema(description = "Stable source-scoped idempotency key", maxLength = 128)
        String requestKey,

        @Schema(description = "Notification scene", maxLength = 64)
        String sceneCode,

        @Schema(description = "Recipient user IDs, 1 to 100")
        List<String> recipients,

        @Schema(description = "Channel; currently only IN_APP is supported")
        String channel,

        @Schema(description = "Title", maxLength = 200) String title,

        @Schema(description = "Rendered message body", maxLength = 4000)
        String body,

        @Schema(description = "Optional application-relative action path", maxLength = 500)
        String actionUrl,

        @Schema(description = "Optional expiration in the application local time zone")
        LocalDateTime expiresAt) {
    public NotificationSubmitCommand {
        required(requestKey, 128);
        required(sceneCode, 64);
        required(title, 200);
        required(body, 4000);
        if (!"IN_APP".equals(channel)) {
            throw new CommonException(NotificationResultCodeEnum.UNSUPPORTED_CHANNEL);
        }
        if (recipients == null || recipients.isEmpty() || recipients.size() > 100) {
            throw new CommonException(NotificationResultCodeEnum.RECIPIENTS_MUST_CONTAIN_1_TO_100_USERS);
        }
        recipients.forEach(value -> required(value, 64));
        recipients = recipients.stream().map(String::trim).distinct().sorted().toList();
        if (actionUrl != null) {
            required(actionUrl, 500);
            if (!actionUrl.startsWith("/")
                    || actionUrl.startsWith("//")
                    || actionUrl.indexOf('\\') >= 0
                    || actionUrl.chars().anyMatch(Character::isISOControl)) {
                throw new CommonException(NotificationResultCodeEnum.INVALID_ACTION_URL);
            }
        }
    }

    private static void required(String value, int max) {
        if (value == null || value.isBlank() || value.length() > max) {
            throw new CommonException(NotificationResultCodeEnum.INVALID_SUBMISSION);
        }
    }

    @Override
    public String toString() {
        return ToStringAsJson.reflectionToString(this);
    }
}
