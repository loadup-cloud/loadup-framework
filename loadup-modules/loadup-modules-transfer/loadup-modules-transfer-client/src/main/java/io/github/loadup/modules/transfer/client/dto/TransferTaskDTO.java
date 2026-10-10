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
package io.github.loadup.modules.transfer.client.dto;

import io.github.loadup.commons.json.ToStringAsJson;
import io.github.loadup.modules.transfer.client.enums.TransferKind;
import io.github.loadup.modules.transfer.client.enums.TransferStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

/** Durable state of one import or export job. */
public record TransferTaskDTO(
        @Schema(description = "Resource identifier") String id,

        @Schema(description = "Tenant identifier resolved from trusted context")
        String tenantId,

        @Schema(description = "Owner user identifier") String ownerId,
        @Schema(description = "Task or catalog category") TransferKind kind,
        @Schema(description = "Handler key") String handlerKey,

        @Schema(description = "Uploaded source file identifier")
        String sourceFileId,

        @Schema(description = "Generated output file identifier")
        String resultFileId,

        @Schema(description = "Current lifecycle status") TransferStatus status,
        @Schema(description = "Number of entries processed") long processedCount,
        @Schema(description = "Total entries to process") long totalCount,
        @Schema(description = "Error message") String errorMessage,
        @Schema(description = "Creation time in UTC") LocalDateTime createdAt,
        @Schema(description = "Started at") LocalDateTime startedAt,
        @Schema(description = "Finished at") LocalDateTime finishedAt) {
    @Override
    public String toString() {
        return ToStringAsJson.reflectionToString(this);
    }
}
