/*
 * #%L
 * LoadUp Outbox
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
package io.github.loadup.components.outbox;

import java.nio.charset.StandardCharsets;

/** A versioned business event; the tenant must originate from a trusted identity. */
public record OutboxMessage(String tenantId, String type, int version, String businessId, String payload) {
    public OutboxMessage {
        tenantId = required(tenantId, "tenantId", 64);
        type = required(type, "type", 128);
        businessId = required(businessId, "businessId", 128);
        if (version < 1) throw new IllegalArgumentException("version must be positive");
        if (payload == null || payload.getBytes(StandardCharsets.UTF_8).length > 1024 * 1024) {
            throw new IllegalArgumentException("payload must contain at most 1 MiB");
        }
    }

    static String required(String value, String name, int max) {
        if (value == null || value.isBlank() || value.length() > max) {
            throw new IllegalArgumentException(name + " must contain 1 to " + max + " characters");
        }
        return value;
    }
}
