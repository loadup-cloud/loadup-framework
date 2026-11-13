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
package io.github.loadup.components.kms;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.github.loadup.components.authserver.config.TestKeyStores;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyStore;
import java.util.Arrays;
import java.util.Base64;
import javax.crypto.KeyGenerator;
import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class LocalKmsTemplateTest {
    private static final char[] PASSWORD = TestKeyStores.PASSWORD.toCharArray();
    private static final KmsKeyRef RSA = new KmsKeyRef("rsa", 1);
    private static final KmsKeyRef AES = new KmsKeyRef("aes", 1);
    private static final byte[] MESSAGE = "local KMS contract".getBytes(StandardCharsets.UTF_8);

    @TempDir
    static Path directory;

    private static KeyStore store;
    private static LocalKmsTemplate kms;

    @BeforeAll
    static void setUp() throws Exception {
        var path = TestKeyStores.create(directory, RSA.name());
        store = KeyStore.getInstance("PKCS12");
        try (var input = Files.newInputStream(path)) {
            store.load(input, PASSWORD);
        }
        var generator = KeyGenerator.getInstance("AES");
        generator.init(256);
        store.setEntry(
                AES.name(),
                new KeyStore.SecretKeyEntry(generator.generateKey()),
                new KeyStore.PasswordProtection(PASSWORD));
        kms = new LocalKmsTemplate(store, PASSWORD);
    }

    @Test
    void rsaAlgorithmsVerifyPinnedAndLatestVersionsAndRejectChangedMessages() {
        for (var algorithm : KmsSignatureAlgorithm.values()) {
            var signature = kms.sign(KmsKeyRef.latest(RSA.name()), algorithm, MESSAGE);
            assertThat(signature.keyName()).isEqualTo(RSA.name());
            assertThat(signature.version()).isEqualTo(1);
            assertThat(signature.algorithm()).isEqualTo(algorithm);
            assertThat(kms.verify(RSA, signature, MESSAGE)).isTrue();
            assertThat(kms.verify(KmsKeyRef.latest(RSA.name()), signature, MESSAGE))
                    .isTrue();
            assertThat(kms.verify(RSA, signature, new byte[] {0})).isFalse();
        }
        var metadata = kms.metadata(RSA.name());
        assertThat(metadata.type()).isEqualTo("rsa");
        assertThat(metadata.latestVersion()).isEqualTo(1);
        assertThat(metadata.supportsSigning()).isTrue();
        assertThat(metadata.supportsEncryption()).isFalse();
        assertThat(metadata.supportsDecryption()).isFalse();
        assertThat(metadata.publicKeys()).containsEntry(1, kms.publicKey(RSA));
        assertThat(kms.publicKey(RSA))
                .startsWith("-----BEGIN PUBLIC KEY-----\n")
                .endsWith("\n-----END PUBLIC KEY-----\n");
    }

    @Test
    void aesGcmRoundTripsEmptyAndNonemptyPayloadsWithFreshNonces() {
        for (byte[] plaintext : new byte[][] {new byte[0], MESSAGE}) {
            var encrypted = kms.encrypt(KmsKeyRef.latest(AES.name()), plaintext);
            assertThat(encrypted.keyName()).isEqualTo(AES.name());
            assertThat(encrypted.version()).isEqualTo(1);
            assertThat(encrypted.value()).startsWith("local:v1:");
            assertThat(kms.decrypt(AES, encrypted)).containsExactly(plaintext);
            assertThat(kms.decrypt(KmsKeyRef.latest(AES.name()), encrypted)).containsExactly(plaintext);
            assertThat(kms.encrypt(AES, plaintext).value()).isNotEqualTo(encrypted.value());
        }
        var metadata = kms.metadata(AES.name());
        assertThat(metadata.type()).isEqualTo("aes256-gcm96");
        assertThat(metadata.supportsEncryption()).isTrue();
        assertThat(metadata.supportsDecryption()).isTrue();
        assertThat(metadata.supportsSigning()).isFalse();
        assertThat(metadata.publicKeys()).isEmpty();
    }

    @Test
    void missingInputsAndUnknownKeysFailAsArgumentErrors() {
        assertThatThrownBy(() -> kms.sign(RSA, null, MESSAGE)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> kms.sign(RSA, KmsSignatureAlgorithm.RSA_SHA256_PKCS1, null))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> kms.verify(RSA, null, MESSAGE)).isInstanceOf(IllegalArgumentException.class);
        var signature = kms.sign(RSA, KmsSignatureAlgorithm.RSA_SHA256_PKCS1, MESSAGE);
        assertThatThrownBy(() -> kms.verify(RSA, signature, null)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> kms.encrypt(AES, null)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> kms.decrypt(AES, null)).isInstanceOf(IllegalArgumentException.class);

        for (KmsKeyRef key : Arrays.asList(null, new KmsKeyRef(RSA.name(), 2), KmsKeyRef.latest("missing"))) {
            assertThatThrownBy(() -> kms.sign(key, KmsSignatureAlgorithm.RSA_SHA256_PKCS1, MESSAGE))
                    .isInstanceOf(IllegalArgumentException.class);
            assertThatThrownBy(() -> kms.encrypt(key, MESSAGE)).isInstanceOf(IllegalArgumentException.class);
            assertThatThrownBy(() -> kms.publicKey(key)).isInstanceOf(IllegalArgumentException.class);
        }
        assertThatThrownBy(() -> kms.metadata("missing")).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void signatureAndCiphertextIdentitiesMustMatchTheRequestedKey() {
        var signature = kms.sign(RSA, KmsSignatureAlgorithm.RSA_SHA256_PKCS1, MESSAGE);
        var ciphertext = kms.encrypt(AES, MESSAGE);
        for (KmsKeyRef key : Arrays.asList(null, KmsKeyRef.latest("other"), new KmsKeyRef(RSA.name(), 2))) {
            assertThatThrownBy(() -> kms.verify(key, signature, MESSAGE))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Key mismatch");
        }
        for (KmsKeyRef key : Arrays.asList(null, KmsKeyRef.latest("other"), new KmsKeyRef(AES.name(), 2))) {
            assertThatThrownBy(() -> kms.decrypt(key, ciphertext))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Key mismatch");
        }
        var futureSignature = new KmsSignature(RSA.name(), 2, signature.algorithm(), signature.bytes());
        assertThatThrownBy(() -> kms.verify(RSA, futureSignature, MESSAGE))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Key mismatch");
        assertThatThrownBy(() -> kms.decrypt(AES, new KmsCiphertext(AES.name(), 2, "local:v2:AAAA")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Key mismatch");
    }

    @Test
    void malformedAndTamperedCiphertextsAreRejected() {
        for (String value : new String[] {"vault:v1:AAAA", "local:v1:AAAA", "local:v1:A"}) {
            var ciphertext = new KmsCiphertext(AES.name(), 1, value);
            assertThatThrownBy(() -> kms.decrypt(AES, ciphertext)).isInstanceOf(IllegalArgumentException.class);
        }
        var encrypted = kms.encrypt(AES, MESSAGE);
        byte[] damaged = Base64.getDecoder().decode(encrypted.value().substring(9));
        damaged[damaged.length - 1] ^= 1;
        var tampered = new KmsCiphertext(
                AES.name(), 1, "local:v1:" + Base64.getEncoder().encodeToString(damaged));
        invalidRequest(() -> kms.decrypt(AES, tampered));

        var unknown = new KmsCiphertext("missing", 1, encrypted.value());
        assertThatThrownBy(() -> kms.decrypt(KmsKeyRef.latest("missing"), unknown))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Unknown key");
        var unknownSignature = new KmsSignature("missing", 1, KmsSignatureAlgorithm.RSA_SHA256_PKCS1, new byte[] {1});
        assertThatThrownBy(() -> kms.verify(KmsKeyRef.latest("missing"), unknownSignature, MESSAGE))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Unknown key");
    }

    @Test
    void incompatibleKeyTypesAndUnreadableEntriesUseSanitizedKmsErrors() {
        invalidRequest(() -> kms.sign(AES, KmsSignatureAlgorithm.RSA_SHA256_PKCS1, MESSAGE));
        invalidRequest(() -> kms.verify(
                AES, new KmsSignature(AES.name(), 1, KmsSignatureAlgorithm.RSA_SHA256_PKCS1, new byte[] {1}), MESSAGE));
        invalidRequest(() -> kms.encrypt(RSA, MESSAGE));
        var ciphertext = kms.encrypt(AES, MESSAGE);
        invalidRequest(() -> kms.decrypt(RSA, new KmsCiphertext(RSA.name(), 1, ciphertext.value())));
        invalidRequest(() -> kms.publicKey(AES));

        var unreadable = new LocalKmsTemplate(store, "wrong-password".toCharArray());
        invalidRequest(() -> unreadable.metadata(RSA.name()));
    }

    @Test
    void passwordCopiesPreventCallersFromChangingStoredCredentials() {
        char[] supplied = PASSWORD.clone();
        var keys = new LocalKmsKeyStore(store, supplied);
        var protectedTemplate = new LocalKmsTemplate(store, supplied);
        Arrays.fill(supplied, 'x');
        char[] returned = keys.password();
        Arrays.fill(returned, 'y');
        assertThat(keys.store()).isSameAs(store);
        assertThat(keys.password()).containsExactly(PASSWORD);
        var signature = protectedTemplate.sign(RSA, KmsSignatureAlgorithm.RSA_SHA256_PKCS1, MESSAGE);
        assertThat(protectedTemplate.verify(RSA, signature, MESSAGE)).isTrue();
    }

    private static void invalidRequest(ThrowingCallable action) {
        assertThatThrownBy(action)
                .isInstanceOfSatisfying(
                        KmsException.class,
                        failure -> assertThat(failure.getCode()).isEqualTo(KmsException.Code.INVALID_REQUEST))
                .hasNoCause();
    }
}
