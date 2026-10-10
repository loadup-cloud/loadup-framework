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
package io.github.loadup.modules.notification.client.dto;

import io.github.loadup.commons.json.ToStringAsJson;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

public record NotificationViewDTO(
        @Schema(description = "Resource identifier") String id,
        @Schema(description = "Sender id") String senderId,
        @Schema(description = "Category") String category,
        @Schema(description = "Title") String title,
        @Schema(description = "Body") String body,
        @Schema(description = "Action url") String actionUrl,
        @Schema(description = "Read at") LocalDateTime readAt,
        @Schema(description = "Creation time in UTC") LocalDateTime createdAt) {
    @Override
    public String toString() {
        return ToStringAsJson.reflectionToString(this);
    }
}
