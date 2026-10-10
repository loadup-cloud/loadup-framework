/*
 * #%L
 * LoadUp Transfer Domain
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
package io.github.loadup.modules.transfer.domain.model;

import io.github.loadup.commons.json.ToStringAsJson;
import java.time.LocalDateTime;

/** Durable state of one import or export job. */
public record TransferTask(
        String id,
        String tenantId,
        String ownerId,
        TransferKind kind,
        String handlerKey,
        String sourceFileId,
        String resultFileId,
        TransferStatus status,
        long processedCount,
        long totalCount,
        String errorMessage,
        LocalDateTime createdAt,
        LocalDateTime startedAt,
        LocalDateTime finishedAt) {
    @Override
    public String toString() {
        return ToStringAsJson.reflectionToString(this);
    }
}
