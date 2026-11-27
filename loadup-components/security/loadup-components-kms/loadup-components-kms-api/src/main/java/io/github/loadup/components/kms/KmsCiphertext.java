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
package io.github.loadup.components.kms;

import io.github.loadup.commons.json.DiagnosticHidden;
import io.github.loadup.commons.json.ToStringAsJson;
import java.util.Map;

public record KmsCiphertext(
        String keyName, int version, @DiagnosticHidden String value, KmsProviderVersion providerVersion) {
    public KmsCiphertext(String keyName, int version, String value) {
        this(keyName, version, value, null);
    }

    public KmsCiphertext {
        KmsKeyRef.validateName(keyName);
        boolean valid = providerVersion == null
                ? version > 0 && value != null && value.matches("(vault|local|aliyun):v" + version + ":[A-Za-z0-9+/=]+")
                : version == 0 && value != null && value.matches("aliyun:[A-Za-z0-9+/=]+");
        if (!valid) {
            throw new IllegalArgumentException("Invalid KMS ciphertext");
        }
    }

    /**
     * Retain this reference from the original, trusted operation for use after an alias is rebound.
     */
    public KmsKeyRef keyRef() {
        return new KmsKeyRef(keyName, version, providerVersion);
    }

    @Override
    public String toString() {
        return ToStringAsJson.reflectionToString(Map.of("keyName", keyName, "version", version));
    }
}
