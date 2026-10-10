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
package io.github.loadup.modules.upms.client.command;

import io.github.loadup.commons.json.ToStringAsJson;
import io.swagger.v3.oas.annotations.media.Schema;

public record AccountPasswordChangeCommand(
        @Schema(description = "Current password", accessMode = Schema.AccessMode.WRITE_ONLY)
        @com.fasterxml.jackson.annotation.JsonProperty(
                access = com.fasterxml.jackson.annotation.JsonProperty.Access.WRITE_ONLY)
        String oldPassword,

        @Schema(description = "New password", accessMode = Schema.AccessMode.WRITE_ONLY)
        @com.fasterxml.jackson.annotation.JsonProperty(
                access = com.fasterxml.jackson.annotation.JsonProperty.Access.WRITE_ONLY)
        String newPassword,

        @Schema(description = "Confirmation of the new password", accessMode = Schema.AccessMode.WRITE_ONLY)
        @com.fasterxml.jackson.annotation.JsonProperty(
                access = com.fasterxml.jackson.annotation.JsonProperty.Access.WRITE_ONLY)
        String confirmPassword) {
    @Override
    public String toString() {
        return ToStringAsJson.reflectionToString(this);
    }
}
