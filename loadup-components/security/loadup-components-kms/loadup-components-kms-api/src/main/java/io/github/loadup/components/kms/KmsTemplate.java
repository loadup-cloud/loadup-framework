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

import java.util.List;
import java.util.stream.Stream;

public interface KmsTemplate {
    KmsCiphertext encrypt(KmsKeyRef key, byte[] plaintext);

    byte[] decrypt(KmsKeyRef expectedKey, KmsCiphertext ciphertext);

    KmsSignature sign(KmsKeyRef key, KmsSignatureAlgorithm algorithm, byte[] message);

    boolean verify(KmsKeyRef expectedKey, KmsSignature signature, byte[] message);

    KmsKeyMetadata metadata(String name);

    String publicKey(KmsKeyRef key);

    default KmsVerificationKey currentSigningKey(String alias) {
        var metadata = signingMetadata(alias);
        return verificationKey(metadata, metadata.latestVersion());
    }

    /** Keys currently trusted for verification under this alias, including retained generations. */
    default List<KmsVerificationKey> verificationKeys(String alias) {
        var metadata = signingMetadata(alias);
        var versions = metadata.publicKeys().isEmpty()
                ? Stream.of(metadata.latestVersion())
                : metadata.publicKeys().keySet().stream();
        return versions.filter(
                        version -> version >= metadata.minDecryptionVersion() && version <= metadata.latestVersion())
                .sorted()
                .map(version -> verificationKey(metadata, version))
                .toList();
    }

    private KmsKeyMetadata signingMetadata(String alias) {
        KmsKeyRef.validateName(alias);
        var metadata = metadata(alias);
        if (metadata == null
                || !alias.equals(metadata.name())
                || !metadata.supportsSigning()
                || (metadata.latestVersion() == 0 && metadata.providerVersion() == null)) {
            throw new KmsException(KmsException.Code.INVALID_RESPONSE);
        }
        return metadata;
    }

    private KmsVerificationKey verificationKey(KmsKeyMetadata metadata, int version) {
        var ref = new KmsKeyRef(metadata.name(), version, version == 0 ? metadata.providerVersion() : null);
        String pem = metadata.publicKeys().get(version);
        return KmsVerificationKey.fromPem(ref, pem == null ? publicKey(ref) : pem);
    }

    /** The alias comes from trusted policy. The fingerprint from the wire can only select within that alias. */
    default boolean verify(
            String alias, String fingerprint, KmsSignatureAlgorithm algorithm, byte[] message, byte[] signature) {
        for (var key : verificationKeys(alias)) {
            if (key.fingerprint().equals(fingerprint)) {
                var ref = key.actual();
                return verify(
                        ref,
                        new KmsSignature(alias, ref.version(), algorithm, signature, ref.providerVersion()),
                        message);
            }
        }
        return false;
    }
}
