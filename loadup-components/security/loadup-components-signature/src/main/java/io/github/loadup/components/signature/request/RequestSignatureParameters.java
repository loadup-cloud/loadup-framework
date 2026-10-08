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

import io.github.loadup.components.kms.KmsSignatureAlgorithm;
import java.time.DateTimeException;
import java.time.Instant;

/** All transmitted signature metadata is also covered by the signature. */
public record RequestSignatureParameters(
        String appId, String audience, int keyVersion, KmsSignatureAlgorithm algorithm, long timestamp, String nonce) {
    public RequestSignatureParameters {
        validateIdentifier(appId);
        validateIdentifier(audience);
        if (keyVersion < 1
                || algorithm == null
                || timestamp < 0
                || nonce == null
                || !nonce.matches("[A-Za-z0-9_-]{22,128}")) {
            throw new RequestSignatureException(RequestSignatureException.Code.MALFORMED);
        }
        try {
            Instant.ofEpochSecond(timestamp);
        } catch (DateTimeException invalid) {
            throw new RequestSignatureException(RequestSignatureException.Code.MALFORMED);
        }
    }

    static void validateIdentifier(String value) {
        if (value == null || !value.matches("[A-Za-z0-9._:-]{1,128}")) {
            throw new RequestSignatureException(RequestSignatureException.Code.MALFORMED);
        }
    }
}
