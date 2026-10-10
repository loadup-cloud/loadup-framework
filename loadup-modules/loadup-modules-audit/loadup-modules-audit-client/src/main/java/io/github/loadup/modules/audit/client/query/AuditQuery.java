/*
 * #%L
 * LoadUp Audit Client
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
package io.github.loadup.modules.audit.client.query;

import io.github.loadup.commons.json.ToStringAsJson;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

/** Tenant-scoped audit search. Tenant ID must come from trusted request context. */
public record AuditQuery(
        @Schema(description = "Tenant identifier resolved from trusted context")
        String tenantId,

        @Schema(description = "Actor identifier") String actorId,
        @Schema(description = "Action") String action,
        @Schema(description = "Outcome") String outcome,
        @Schema(description = "Inclusive start time in UTC") LocalDateTime from,
        @Schema(description = "Inclusive end time in UTC") LocalDateTime to,

        @Schema(description = "Page number, starting at 1", minimum = "1")
        int page,

        @Schema(description = "Page size; list queries allow at most 100", minimum = "1")
        int size) {
    public AuditQuery {
        if (page < 1 || size < 1 || size > 100) {
            throw new IllegalArgumentException("page must be positive and size must be between 1 and 100");
        }
        if (from != null && to != null && from.isAfter(to)) {
            throw new IllegalArgumentException("from must not be after to");
        }
    }

    @Override
    public String toString() {
        return ToStringAsJson.reflectionToString(this);
    }
}
