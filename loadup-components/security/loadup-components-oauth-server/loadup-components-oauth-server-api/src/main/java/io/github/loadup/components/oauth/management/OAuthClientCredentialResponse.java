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
import java.time.Instant;

/** Only returned to an authorized caller after create/rotation. Never put this payload in audit. */
public record OAuthClientCredentialResponse(String registrationId, String clientId, String secret, Instant expiresAt) {
    @Override
    public String toString() {
        return ToStringAsJson.reflectionToString(this);
    }
}
