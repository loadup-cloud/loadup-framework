/*
 * Copyright (C) 2025-2026 LoadUp Cloud
 *
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
 */
package io.github.loadup.components.oauth.management;

import io.github.loadup.commons.json.ToStringAsJson;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.Duration;
import java.util.Map;
import java.util.Set;

/** Shared-secret client_credentials authorization. Application identity is represented by attributes. */
public record OAuthClientAuthorization(
        @NotBlank @Size(max = 200) String clientName,
        @NotEmpty @Size(max = 100) Set<@NotBlank @Size(max = 128) String> scopes,
        @NotBlank @Size(max = 128) String audience,
        @NotNull @Size(max = 8) Map<@NotBlank @Size(max = 100) String, @NotBlank @Size(max = 256) String> attributes,
        @NotNull Duration accessTokenTtl) {
    @Override
    public String toString() {
        return ToStringAsJson.reflectionToString(this);
    }
}
