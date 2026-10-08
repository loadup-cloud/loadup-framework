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

import io.github.loadup.components.kms.KmsException;
import io.github.loadup.components.kms.KmsTemplate;
import java.security.SecureRandom;
import java.time.Clock;
import java.util.Base64;
import java.util.Objects;

/** Signs exact requests through KMS using an explicit, trusted key version. */
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
        var parameters = new RequestSignatureParameters(
                identity.appId(),
                identity.audience(),
                identity.key().version(),
                identity.algorithm(),
                clock.instant().getEpochSecond(),
                Base64.getUrlEncoder().withoutPadding().encodeToString(nonce));
        var signed = kms.sign(
                identity.key(), identity.algorithm(), RequestSignatureCanonicalizer.canonicalize(content, parameters));
        if (signed == null
                || !identity.key().name().equals(signed.keyName())
                || identity.key().version() != signed.version()
                || identity.algorithm() != signed.algorithm()) {
            throw new KmsException(KmsException.Code.INVALID_RESPONSE);
        }
        try {
            return new RequestSignature(parameters, signed.base64());
        } catch (RequestSignatureException invalid) {
            throw new KmsException(KmsException.Code.INVALID_RESPONSE);
        }
    }
}
