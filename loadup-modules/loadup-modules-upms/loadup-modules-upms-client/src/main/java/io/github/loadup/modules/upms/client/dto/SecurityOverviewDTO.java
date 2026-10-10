/*
 * #%L
 * Loadup Modules UPMS Client Layer
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
package io.github.loadup.modules.upms.client.dto;

import io.github.loadup.commons.json.ToStringAsJson;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

public record SecurityOverviewDTO(
        @Schema(description = "User id") String userId,
        @Schema(description = "Username") String username,
        @Schema(description = "Active") boolean active,
        @Schema(description = "Account non locked") boolean accountNonLocked,
        @Schema(description = "Login fail count") int loginFailCount,

        @Schema(description = "Password updated at", accessMode = Schema.AccessMode.WRITE_ONLY)
        @com.fasterxml.jackson.annotation.JsonProperty(
                access = com.fasterxml.jackson.annotation.JsonProperty.Access.WRITE_ONLY)
        LocalDateTime passwordUpdatedAt,

        @Schema(description = "Last login at") LocalDateTime lastLoginAt,
        @Schema(description = "Last login ip") String lastLoginIp) {
    @Override
    public String toString() {
        return ToStringAsJson.reflectionToString(this);
    }
}
