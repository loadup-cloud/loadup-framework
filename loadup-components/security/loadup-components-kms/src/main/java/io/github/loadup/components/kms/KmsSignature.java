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

import io.github.loadup.commons.json.ToStringAsJson;
import java.util.Base64;

/** Raw signature bytes plus the metadata required to reconstruct a Transit signature. */
public record KmsSignature(String keyName, int version, KmsSignatureAlgorithm algorithm, byte[] bytes) {
    public KmsSignature {
        KmsKeyRef.validateName(keyName);
        if (version < 1 || algorithm == null || bytes == null || bytes.length == 0 || bytes.length > 512) {
            throw new IllegalArgumentException("Invalid KMS signature");
        }
        bytes = bytes.clone();
    }

    @Override
    public byte[] bytes() {
        return bytes.clone();
    }

    public String base64() {
        return Base64.getEncoder().encodeToString(bytes);
    }

    String transitValue() {
        return "vault:v" + version + ":" + base64();
    }

    @Override
    public String toString() {
        return ToStringAsJson.reflectionToString(java.util.Map.of("keyName", keyName, "version", version));
    }
}
