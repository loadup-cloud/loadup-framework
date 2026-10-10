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
package io.github.loadup.modules.audit.client.command;

import io.github.loadup.commons.json.ToStringAsJson;
import io.swagger.v3.oas.annotations.media.Schema;

/** Safe, metadata-only audit input. Never put credentials or request bodies in this record. */
public record AuditRecordCommand(
        @Schema(description = "Tenant identifier resolved from trusted context")
        String tenantId,

        @Schema(description = "Actor identifier") String actorId,
        @Schema(description = "Action") String action,
        @Schema(description = "HTTP request method") String method,
        @Schema(description = "HTTP request path") String path,
        @Schema(description = "Outcome") String outcome,

        @Schema(description = "Trace identifier for request diagnostics")
        String traceId) {
    @Override
    public String toString() {
        return ToStringAsJson.reflectionToString(this);
    }
}
