/*
 * #%L
 * LoadUp Components Signature
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
package io.github.loadup.components.signature.request;

import io.github.loadup.components.kms.KmsKeyRef;
import io.github.loadup.components.kms.KmsSignatureAlgorithm;

/** Trusted application binding; never construct it from unverified signature headers alone. */
public record RequestSigningIdentity(String appId, String audience, KmsKeyRef key, KmsSignatureAlgorithm algorithm) {
    public RequestSigningIdentity {
        RequestSignatureParameters.validateIdentifier(appId);
        RequestSignatureParameters.validateIdentifier(audience);
        if (key == null || key.version() < 1 || algorithm == null) {
            throw new IllegalArgumentException("A trusted key with an explicit version and algorithm is required");
        }
    }
}
