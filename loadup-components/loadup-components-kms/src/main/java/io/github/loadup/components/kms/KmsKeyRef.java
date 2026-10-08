/*
 * #%L
 * LoadUp Kms
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
package io.github.loadup.components.kms;

/** An OpenBao key name and version; zero selects the latest version for new operations. */
public record KmsKeyRef(String name, int version) {
    public KmsKeyRef {
        validateName(name);
        if (version < 0) throw new IllegalArgumentException("key version must not be negative");
    }

    public static KmsKeyRef latest(String name) {
        return new KmsKeyRef(name, 0);
    }

    static void validateName(String name) {
        if (name == null || !name.matches("[A-Za-z0-9_-]{1,128}")) {
            throw new IllegalArgumentException("Invalid KMS key name");
        }
    }
}
