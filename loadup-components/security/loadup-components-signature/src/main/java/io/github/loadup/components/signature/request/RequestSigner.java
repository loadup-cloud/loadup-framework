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
package io.github.loadup.components.signature.request;

import io.github.loadup.components.kms.KmsException;
import io.github.loadup.components.kms.KmsTemplate;
import io.github.loadup.components.kms.KmsVerificationKey;
import java.security.SecureRandom;
import java.time.Clock;
import java.util.Base64;
import java.util.Objects;

public final class RequestSigner {
    private final KmsTemplate kms;
    private final Clock clock;
    private final RequestSignatureProperties properties;
    private final SecureRandom random = new SecureRandom();

    public RequestSigner(KmsTemplate kms, Clock clock, RequestSignatureProperties properties) {
        this.kms = Objects.requireNonNull(kms);
        this.clock = Objects.requireNonNull(clock);
        this.properties = Objects.requireNonNull(properties);
    }

    public RequestSignature sign(RequestSigningIdentity identity, RequestContent content) {
        Objects.requireNonNull(identity, "identity");
        properties.validateContent(content);
        byte[] nonce = new byte[24];
        random.nextBytes(nonce);
        var current = identity.key().version() == 0
                ? kms.currentSigningKey(identity.key().name())
                : null;
        var parameters = new RequestSignatureParameters(
                identity.appId(),
                identity.audience(),
                identity.key().version(),
                identity.algorithm(),
                clock.instant().getEpochSecond(),
                Base64.getUrlEncoder().withoutPadding().encodeToString(nonce),
                current == null ? null : current.fingerprint());
        var input = RequestSignatureCanonicalizer.canonicalize(content, parameters);
        var signed = kms.sign(identity.key(), identity.algorithm(), input);
        var expected = current == null ? identity.key() : current.actual();
        if (signed == null || !expected.equals(signed.keyRef()) || identity.algorithm() != signed.algorithm()) {
            throw new KmsException(KmsException.Code.INVALID_RESPONSE);
        }
        if (current != null) {
            if (!KmsVerificationKey.verify(current.publicKey(), signed.algorithm(), input, signed.bytes())) {
                throw new KmsException(KmsException.Code.INVALID_RESPONSE);
            }
        }
        try {
            return new RequestSignature(parameters, signed.base64());
        } catch (RequestSignatureException invalid) {
            throw new KmsException(KmsException.Code.INVALID_RESPONSE);
        }
    }
}
