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

import io.github.loadup.components.kms.KmsSignature;
import io.github.loadup.components.kms.KmsTemplate;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.DateTimeException;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Objects;

/** Verifies identity, time and signature before consuming a nonce atomically. */
public final class RequestVerifier {
    private final KmsTemplate kms;
    private final RequestNonceStore nonces;
    private final Clock clock;
    private final RequestSignatureProperties properties;

    public RequestVerifier(
            KmsTemplate kms, RequestNonceStore nonces, Clock clock, RequestSignatureProperties properties) {
        this.kms = Objects.requireNonNull(kms);
        this.nonces = Objects.requireNonNull(nonces);
        this.clock = Objects.requireNonNull(clock);
        this.properties = Objects.requireNonNull(properties);
    }

    public void verify(RequestSigningIdentity trusted, RequestContent content, RequestSignature signature) {
        Objects.requireNonNull(trusted, "trusted identity");
        properties.validateContent(content);
        if (signature == null) throw new RequestSignatureException(RequestSignatureException.Code.MALFORMED);
        var parameters = signature.parameters();
        if (!trusted.appId().equals(parameters.appId())
                || !trusted.audience().equals(parameters.audience())
                || trusted.key().version() != parameters.keyVersion()
                || trusted.algorithm() != parameters.algorithm()) {
            throw new RequestSignatureException(RequestSignatureException.Code.IDENTITY_MISMATCH);
        }
        Instant expiresAt = validateTime(parameters);
        var value = new KmsSignature(
                trusted.key().name(),
                parameters.keyVersion(),
                parameters.algorithm(),
                Base64.getDecoder().decode(signature.signature()));
        if (!kms.verify(trusted.key(), value, RequestSignatureCanonicalizer.canonicalize(content, parameters))) {
            throw new RequestSignatureException(RequestSignatureException.Code.INVALID_SIGNATURE);
        }
        validateTime(parameters);
        Duration ttl = Duration.between(clock.instant(), expiresAt);
        if (ttl.isNegative() || ttl.isZero())
            throw new RequestSignatureException(RequestSignatureException.Code.EXPIRED);
        // Retain claims beyond expiry for nodes whose clocks lag within the configured tolerance.
        ttl = ttl.plus(properties.futureSkew());
        String replayKey = RequestSignatureCanonicalizer.sha256(
                (parameters.appId() + "\n" + parameters.audience() + "\n" + parameters.nonce())
                        .getBytes(StandardCharsets.UTF_8));
        boolean claimed;
        try {
            claimed = nonces.claim(replayKey, ttl);
        } catch (RuntimeException failure) {
            throw new RequestSignatureException(RequestSignatureException.Code.REPLAY_STORE_UNAVAILABLE);
        }
        if (!claimed) throw new RequestSignatureException(RequestSignatureException.Code.REPLAY);
    }

    private Instant validateTime(RequestSignatureParameters parameters) {
        try {
            Instant now = clock.instant();
            Instant timestamp = Instant.ofEpochSecond(parameters.timestamp());
            if (timestamp.isAfter(now.plus(properties.futureSkew())))
                throw new RequestSignatureException(RequestSignatureException.Code.FUTURE_TIMESTAMP);
            Instant expires = timestamp.plus(properties.maxAge());
            if (!now.isBefore(expires)) throw new RequestSignatureException(RequestSignatureException.Code.EXPIRED);
            return expires;
        } catch (DateTimeException invalid) {
            throw new RequestSignatureException(RequestSignatureException.Code.MALFORMED);
        }
    }
}
