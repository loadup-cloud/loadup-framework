/*
 * Copyright (C) 2026 LoadUp Cloud
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
package io.github.loadup.components.kms;

import java.nio.charset.StandardCharsets;

/**
 * Stores named UTF-8 secrets in the selected binder's secret backend. Secret names are metadata.
 * Keep sensitive data in values only.
 */
public interface KmsSecretManager {
    int MAX_VALUE_BYTES = 30 * 1024;

    /** Creates the initial value for a new secret. An existing secret is left untouched. */
    void create(String name, String value);

    /** Stores a new current value for an existing secret. */
    void update(String name, String value);

    /**
     * Returns the current plaintext value. A missing secret fails with {@link
     * KmsException.Code#NOT_FOUND}.
     */
    String get(String name);

    static void validateName(String name) {
        if (name == null || !name.matches("[A-Za-z0-9_-]{1,128}")) {
            throw new IllegalArgumentException("Invalid KMS secret name");
        }
    }

    static void validateValue(String value) {
        if (value == null
                || value.isEmpty()
                || value.length() > MAX_VALUE_BYTES
                || !StandardCharsets.UTF_8.newEncoder().canEncode(value)
                || value.getBytes(StandardCharsets.UTF_8).length > MAX_VALUE_BYTES) {
            throw new IllegalArgumentException("KMS secret must contain 1 to 30720 UTF-8 bytes");
        }
    }
}
