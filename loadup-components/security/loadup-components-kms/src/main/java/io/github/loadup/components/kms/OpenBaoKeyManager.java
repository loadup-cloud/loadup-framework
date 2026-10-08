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

import java.util.Base64;
import java.util.Map;

/** Explicit key administration; export, plaintext backup and deletion are not exposed. */
public final class OpenBaoKeyManager implements KmsKeyManager, AutoCloseable {
    private final OpenBaoTransitClient client;

    OpenBaoKeyManager(OpenBaoTransitClient client) {
        this.client = client;
    }

    @Override
    public void create(String name, KmsKeyType type) {
        requireType(type);
        client.call(
                "create",
                name,
                Map.of("type", type.transitName(), "exportable", false, "allow_plaintext_backup", false),
                false);
    }

    @Override
    public void rotate(String name) {
        client.call("rotate", name, Map.of(), false);
    }

    @Override
    public void setMinimumVersions(String name, int minEncryptionVersion, int minDecryptionVersion) {
        if (minEncryptionVersion < 0 || minDecryptionVersion < 1)
            throw new IllegalArgumentException("Invalid minimum key versions");
        client.call(
                "configure",
                name,
                Map.of("min_encryption_version", minEncryptionVersion, "min_decryption_version", minDecryptionVersion),
                false);
    }

    @Override
    public String wrappingPublicKey() {
        return OpenBaoTransitClient.text(client.call("wrapping", null, null, true), "public_key");
    }

    @Override
    public void importWrappedKey(String name, KmsKeyType type, byte[] wrappedKey) {
        requireType(type);
        client.validateInput(wrappedKey);
        if (wrappedKey.length == 0) throw new IllegalArgumentException("wrappedKey is required");
        client.call(
                "import",
                name,
                Map.of(
                        "type",
                        type.transitName(),
                        "ciphertext",
                        Base64.getEncoder().encodeToString(wrappedKey),
                        "hash_function",
                        "SHA256",
                        "exportable",
                        false,
                        "allow_plaintext_backup",
                        false,
                        "allow_rotation",
                        false),
                false);
    }

    @Override
    public void importPublicKey(String name, KmsKeyType type, String publicKeyPem) {
        requireType(type);
        if (type == KmsKeyType.AES_256_GCM
                || publicKeyPem == null
                || !publicKeyPem.startsWith("-----BEGIN PUBLIC KEY-----")
                || publicKeyPem.length() > 16384)
            throw new IllegalArgumentException("A PEM RSA public key is required");
        client.call(
                "import",
                name,
                Map.of(
                        "type",
                        type.transitName(),
                        "public_key",
                        publicKeyPem,
                        "exportable",
                        false,
                        "allow_plaintext_backup",
                        false,
                        "allow_rotation",
                        false),
                false);
    }

    private static void requireType(KmsKeyType type) {
        if (type == null) throw new IllegalArgumentException("key type is required");
    }

    @Override
    public void close() {
        client.close();
    }
}
