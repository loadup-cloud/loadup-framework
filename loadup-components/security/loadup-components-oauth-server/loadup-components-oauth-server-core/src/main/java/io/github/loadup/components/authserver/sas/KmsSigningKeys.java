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
package io.github.loadup.components.authserver.sas;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.KeyUse;
import com.nimbusds.jose.jwk.RSAKey;
import io.github.loadup.components.authserver.properties.LoadUpAuthServerProperties.Signing;
import io.github.loadup.components.authserver.properties.LoadUpAuthServerProperties.SigningKey;
import io.github.loadup.components.kms.KmsKeyRef;
import io.github.loadup.components.kms.KmsTemplate;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.security.interfaces.RSAPublicKey;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.security.converter.RsaKeyConverters;

final class KmsSigningKeys {
    record Active(KmsKeyRef request, KmsKeyRef actual, RSAKey jwk) {}

    private final KmsTemplate kms;
    private final KmsKeyRef activeKey;
    private final Map<String, JWK> pinned = new LinkedHashMap<>();
    private final Set<String> aliases = new HashSet<>();
    private final Active fixed;

    KmsSigningKeys(KmsTemplate kms, Signing signing) {
        if (signing == null || signing.getVerificationKeys() == null) {
            throw new IllegalArgumentException("Authorization signing configuration is required");
        }
        this.kms = kms;
        activeKey = reference(signing.getActiveKey());
        if (activeKey.version() == 0) {
            fixed = null;
            aliases.add(activeKey.name());
            active();
        } else {
            var metadata = kms.metadata(activeKey.name());
            if (!activeKey.name().equals(metadata.name())
                    || !metadata.supportsSigning()
                    || activeKey.version() > metadata.latestVersion()) {
                throw new IllegalArgumentException(
                        "Authorization active key must support signing at the pinned version");
            }
            var jwk = publicKey(kms, activeKey);
            fixed = new Active(activeKey, activeKey, jwk);
            pinned.put(jwk.getKeyID(), jwk);
        }
        for (SigningKey retained : signing.getVerificationKeys()) {
            var key = reference(retained);
            if (key.version() == 0) {
                if (!aliases.add(key.name())) {
                    throw new IllegalArgumentException("Duplicate authorization signing/verification key");
                }
                if (kms.verificationKeys(key.name()).isEmpty()) {
                    throw new IllegalArgumentException("Retained alias has no trusted verification keys");
                }
            } else {
                if (pinned.containsKey(kid(key))) {
                    throw new IllegalArgumentException("Duplicate authorization signing/verification key");
                }
                pinned.put(kid(key), publicKey(kms, key));
            }
        }
    }

    private static KmsKeyRef reference(SigningKey key) {
        if (key == null || key.getVersion() < 0) {
            throw new IllegalArgumentException("Authorization keys require a name and a non-negative version");
        }
        return new KmsKeyRef(key.getName(), key.getVersion());
    }

    private static String kid(KmsKeyRef key) {
        return key.name() + "-v" + key.version();
    }

    private static RSAKey publicKey(KmsTemplate kms, KmsKeyRef key) {
        var pem = kms.publicKey(key);
        var rsa = RsaKeyConverters.x509().convert(new ByteArrayInputStream(pem.getBytes(StandardCharsets.US_ASCII)));
        if (rsa.getModulus().bitLength() < 2048 || rsa.getModulus().bitLength() > 4096) {
            throw new IllegalArgumentException("Authorization signing requires RSA-2048 through RSA-4096");
        }
        return jwk(kid(key), rsa);
    }

    private static RSAKey jwk(String kid, RSAPublicKey rsa) {
        return new RSAKey.Builder(rsa)
                .keyID(kid)
                .keyUse(KeyUse.SIGNATURE)
                .algorithm(JWSAlgorithm.RS256)
                .build();
    }

    Active active() {
        if (fixed != null) {
            return fixed;
        }
        var current = kms.currentSigningKey(activeKey.name());
        return new Active(
                activeKey, current.actual(), jwk(activeKey.name() + "." + current.fingerprint(), current.publicKey()));
    }

    JWKSet publicKeys() {
        Map<String, JWK> keys = new LinkedHashMap<>(pinned);
        for (String alias : aliases) {
            for (var key : kms.verificationKeys(alias)) {
                String id = alias + "." + key.fingerprint();
                keys.put(id, jwk(id, key.publicKey()));
            }
        }
        return new JWKSet(List.copyOf(keys.values()));
    }
}
