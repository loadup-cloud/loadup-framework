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
package io.github.loadup.modules.file.client.dto;

import io.github.loadup.commons.json.ToStringAsJson;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

/** A business object's claim on a file; claims prevent deletion. */
public record FileReferenceDTO(
        @Schema(description = "Resource identifier") String id,

        @Schema(description = "Tenant identifier resolved from trusted context")
        String tenantId,

        @Schema(description = "File id") String fileId,
        @Schema(description = "Reference type") String referenceType,
        @Schema(description = "Reference id") String referenceId,
        @Schema(description = "Creation time in UTC") LocalDateTime createdAt) {
    @Override
    public String toString() {
        return ToStringAsJson.reflectionToString(this);
    }
}
