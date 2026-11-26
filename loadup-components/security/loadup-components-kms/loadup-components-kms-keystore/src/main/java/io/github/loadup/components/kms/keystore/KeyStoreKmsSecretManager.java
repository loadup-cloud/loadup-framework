/*
 * Copyright (C) 2026 LoadUp Cloud
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
package io.github.loadup.components.kms.keystore;

import io.github.loadup.components.kms.KmsCiphertext;
import io.github.loadup.components.kms.KmsException;
import io.github.loadup.components.kms.KmsKeyRef;
import io.github.loadup.components.kms.KmsSecretManager;
import io.github.loadup.components.kms.KmsTemplate;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.HexFormat;
import java.util.Optional;

/** Protects named secrets with the KeyStore binders' AES-GCM format. */
public final class KeyStoreKmsSecretManager implements KmsSecretManager {
    public static final int MAX_CIPHERTEXT_LENGTH = 42_000;
    private static final String CIPHERTEXT_PREFIX = "local:v1:";
    private final KmsTemplate kms;
    private final KmsKeyRef key;
    private final Storage storage;

    public KeyStoreKmsSecretManager(KmsTemplate kms, String keyAlias, Storage storage) {
        if (keyAlias == null || keyAlias.isBlank()) {
            throw new IllegalArgumentException("loadup.kms.secret-key-alias is required");
        }
        this.kms = kms;
        this.key = KmsKeyRef.latest(keyAlias);
        this.storage = storage;
        var metadata = kms.metadata(keyAlias);
        if (!metadata.supportsEncryption() || !metadata.supportsDecryption()) {
            throw new IllegalArgumentException("KMS secret key must support both encryption and decryption");
        }
    }

    @Override
    public void create(String name, String value) {
        String id = id(name);
        if (!storage.create(id, encrypt(id, value))) {
            throw new KmsException(KmsException.Code.INVALID_REQUEST);
        }
    }

    @Override
    public void update(String name, String value) {
        String id = id(name);
        if (!storage.update(id, encrypt(id, value))) {
            throw new KmsException(KmsException.Code.NOT_FOUND);
        }
    }

    @Override
    public String get(String name) {
        String id = id(name);
        String storedValue = storage.get(id).orElseThrow(() -> new KmsException(KmsException.Code.NOT_FOUND));
        if (storedValue.length() > MAX_CIPHERTEXT_LENGTH || !storedValue.startsWith(CIPHERTEXT_PREFIX)) {
            throw new KmsException(KmsException.Code.INVALID_RESPONSE);
        }
        byte[] plaintext;
        try {
            plaintext = kms.decrypt(key, new KmsCiphertext(key.name(), 1, storedValue));
        } catch (IllegalArgumentException failure) {
            throw new KmsException(KmsException.Code.INVALID_RESPONSE);
        }
        try {
            byte[] prefix = prefix(id);
            if (plaintext.length <= prefix.length
                    || !Arrays.equals(prefix, 0, prefix.length, plaintext, 0, prefix.length)) {
                throw new KmsException(KmsException.Code.INVALID_RESPONSE);
            }
            String value = StandardCharsets.UTF_8
                    .newDecoder()
                    .decode(ByteBuffer.wrap(plaintext, prefix.length, plaintext.length - prefix.length))
                    .toString();
            KmsSecretManager.validateValue(value);
            return value;
        } catch (CharacterCodingException | IllegalArgumentException failure) {
            throw new KmsException(KmsException.Code.INVALID_RESPONSE);
        } finally {
            Arrays.fill(plaintext, (byte) 0);
        }
    }

    private String encrypt(String id, String value) {
        KmsSecretManager.validateValue(value);
        byte[] utf8 = value.getBytes(StandardCharsets.UTF_8);
        byte[] prefix = prefix(id);
        byte[] plaintext = Arrays.copyOf(prefix, prefix.length + utf8.length);
        try {
            System.arraycopy(utf8, 0, plaintext, prefix.length, utf8.length);
            var ciphertext = kms.encrypt(key, plaintext);
            if (!key.name().equals(ciphertext.keyName())
                    || !ciphertext.value().startsWith(CIPHERTEXT_PREFIX)
                    || ciphertext.value().length() > MAX_CIPHERTEXT_LENGTH) {
                throw new KmsException(KmsException.Code.INVALID_RESPONSE);
            }
            return ciphertext.value();
        } finally {
            Arrays.fill(utf8, (byte) 0);
            Arrays.fill(plaintext, (byte) 0);
        }
    }

    private static byte[] prefix(String id) {
        return ("loadup-kms-secret:" + id + "\n").getBytes(StandardCharsets.US_ASCII);
    }

    private static String id(String name) {
        KmsSecretManager.validateName(name);
        try {
            byte[] hash = MessageDigest.getInstance("SHA-256").digest(name.getBytes(StandardCharsets.US_ASCII));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException failure) {
            throw new IllegalStateException("SHA-256 is unavailable");
        }
    }

    /** A store of opaque ciphertexts indexed by the SHA-256 digest of each secret name. */
    public interface Storage {
        boolean create(String id, String ciphertext);

        boolean update(String id, String ciphertext);

        Optional<String> get(String id);
    }
}
