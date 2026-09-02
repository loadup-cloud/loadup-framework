/*-
 * #%L
 * loadup-commons-util
 * %%
 * Copyright (C) 2022 - 2026 loadup_cloud
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

package io.github.loadup.commons.util.security;

import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Set;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

/** Validates shared secrets used for HMAC JWT signing and verification. */
public final class JwtSecretValidator {
    private static final int MINIMUM_BYTES = 32;
    private static final Set<String> KNOWN_WEAK_VALUES =
            Set.of("loadup-secret-key-change-in-production", "loadup-gateway-secret-key-must-be-long-enough-32bytes");
    private static final Set<String> WEAK_PREFIXES = Set.of("changeme", "password", "secret");

    private JwtSecretValidator() {}

    /**
     * Returns an HS256 key after rejecting missing, short, or known weak secrets.
     *
     * @param propertyName configuration property used in the validation message
     * @param secret configured secret
     * @return validated HMAC key
     */
    public static SecretKey requireStrong(String propertyName, String secret) {
        if (secret == null || secret.isBlank()) {
            throw invalid(propertyName, "must be configured");
        }

        byte[] bytes = secret.getBytes(StandardCharsets.UTF_8);
        if (bytes.length < MINIMUM_BYTES) {
            throw invalid(propertyName, "must contain at least " + MINIMUM_BYTES + " bytes (UTF-8)");
        }

        String normalized = secret.trim().toLowerCase(Locale.ROOT);
        if (KNOWN_WEAK_VALUES.contains(normalized) || WEAK_PREFIXES.stream().anyMatch(normalized::startsWith)) {
            throw invalid(propertyName, "uses a known weak value");
        }
        return new SecretKeySpec(bytes, "HmacSHA256");
    }

    private static IllegalStateException invalid(String propertyName, String reason) {
        return new IllegalStateException(
                propertyName + " " + reason + "; generate a secret with: openssl rand -base64 48");
    }
}
