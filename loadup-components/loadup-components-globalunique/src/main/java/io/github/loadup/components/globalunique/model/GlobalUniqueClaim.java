/*-
 * #%L
 * LoadUp Components :: Global Unique
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

package io.github.loadup.components.globalunique.model;

/** Data required to claim one tenant-scoped business key. */
public record GlobalUniqueClaim(String bizType, String uniqueKey, String bizId, String requestData) {
    private static final int BIZ_TYPE_MAX_LENGTH = 64;
    private static final int UNIQUE_KEY_MAX_LENGTH = 255;

    public GlobalUniqueClaim {
        bizType = requireText(bizType, "bizType", BIZ_TYPE_MAX_LENGTH);
        uniqueKey = requireText(uniqueKey, "uniqueKey", UNIQUE_KEY_MAX_LENGTH);
    }

    private static String requireText(String value, String fieldName, int maxLength) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
        String normalized = value.trim();
        if (normalized.length() > maxLength) {
            throw new IllegalArgumentException(fieldName + " must not exceed " + maxLength + " characters");
        }
        return normalized;
    }
}
