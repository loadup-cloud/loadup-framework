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

/** Plaintext output reserved for the authorized and audited read operation. */
public record UserSensitiveDTO(
        @Schema(description = "Resource identifier") String id,

        @Schema(description = "Real name") @io.github.loadup.commons.json.DiagnosticHidden
        String realName,

        @Schema(description = "Email") @io.github.loadup.commons.json.DiagnosticHidden
        String email,

        @Schema(description = "Mobile") @io.github.loadup.commons.json.DiagnosticHidden
        String mobile) {
    @Override
    public String toString() {
        return ToStringAsJson.reflectionToString(this);
    }
}
