/*
 * #%L
 * LoadUp Async Result
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
package io.github.loadup.components.asyncresult;

import io.github.loadup.commons.json.ToStringAsJson;
import java.util.UUID;

public record AsyncReplyKey(String tenantId, String operation, String businessId, String correlationId) {
    public AsyncReplyKey {
        required(tenantId, 64);
        required(operation, 128);
        required(businessId, 128);
        if (!UUID.fromString(correlationId).toString().equals(correlationId)) {
            throw new IllegalArgumentException("Canonical UUID correlationId is required");
        }
    }

    public static void required(String value, int max) {
        if (value == null || value.isBlank() || value.length() > max) {
            throw new IllegalArgumentException("Invalid coordination identifier");
        }
    }

    @Override
    public String toString() {
        return ToStringAsJson.reflectionToString(this);
    }
}
