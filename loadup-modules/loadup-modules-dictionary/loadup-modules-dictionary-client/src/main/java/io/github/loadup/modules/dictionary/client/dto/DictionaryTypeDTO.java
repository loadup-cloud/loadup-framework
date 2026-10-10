/*
 * #%L
 * LoadUp Dictionary Client
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
package io.github.loadup.modules.dictionary.client.dto;

import io.github.loadup.commons.json.ToStringAsJson;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

/** Named collection of business dictionary items. */
public record DictionaryTypeDTO(
        @Schema(description = "Resource identifier") String id,

        @Schema(description = "Tenant identifier resolved from trusted context")
        String tenantId,

        @Schema(description = "Business code") String code,
        @Schema(description = "Name") String name,
        @Schema(description = "Description") String description,

        @Schema(description = "Whether the resource is enabled")
        boolean enabled,

        @Schema(description = "Creation time in UTC") LocalDateTime createdAt,
        @Schema(description = "Last update time in UTC") LocalDateTime updatedAt) {
    @Override
    public String toString() {
        return ToStringAsJson.reflectionToString(this);
    }
}
