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
package io.github.loadup.components.authserver.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.github.loadup.components.kms.KmsException;
import io.github.loadup.components.kms.KmsKeyRef;
import io.github.loadup.components.kms.KmsSignature;
import io.github.loadup.components.kms.KmsSignatureAlgorithm;
import io.github.loadup.components.kms.KmsTemplate;
import io.github.loadup.components.kms.LocalKmsKeyStore;
import io.github.loadup.components.kms.LocalKmsTemplate;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwtEncodingException;

class KmsSigningFailureTest {
    @TempDir
    Path directory;

    @Test
    void missingFileMissingPasswordAndWrongPasswordFailWithoutReplacingTheStore() throws Exception {
        var file = TestKeyStores.create(directory, "auth-current");
        byte[] original = Files.readAllBytes(file);
        for (String override : new String[] {
            "loadup.kms.local.key-store-location="
                    + directory.resolve("missing.p12").toUri(),
            "loadup.kms.local.key-store-password-env=MISSING_TEST_KMS_PASSWORD",
            TestKeyStores.PASSWORD_ENV + "=wrong-password"
        }) {
            TestKeyStores.runner(file)
                    .withPropertyValues(override)
                    .run(context -> assertThat(context).hasFailed());
            assertThat(Files.readAllBytes(file)).containsExactly(original);
        }
    }

    @Test
    void callerSuppliedKeyStoreDoesNotReadConfiguredFileOrPassword() throws Exception {
        var file = TestKeyStores.create(directory, "auth-current");
        var store = new LocalKmsKeyStore(TestKeyStores.load(file), TestKeyStores.PASSWORD.toCharArray());
        TestKeyStores.runner(file)
                .withBean(LocalKmsKeyStore.class, () -> store)
                .withPropertyValues(
                        "loadup.kms.local.key-store-location=file:/not-used.p12",
                        "loadup.kms.local.key-store-password-env=MISSING_TEST_KMS_PASSWORD")
                .run(context -> assertThat(context).hasNotFailed().hasSingleBean(KmsTemplate.class));
    }

    @Test
    void providerFailureOrUnexpectedKeyMetadataCannotFallBackToAnotherSigningKey() throws Exception {
        var file = TestKeyStores.create(directory, "auth-current");
        var local = new LocalKmsTemplate(TestKeyStores.load(file), TestKeyStores.PASSWORD.toCharArray());
        var key = new KmsKeyRef("auth-current", 1);
        var kms = mock(KmsTemplate.class);
        when(kms.metadata(key.name())).thenReturn(local.metadata(key.name()));
        when(kms.publicKey(key)).thenReturn(local.publicKey(key));
        when(kms.sign(eq(key), eq(KmsSignatureAlgorithm.RSA_SHA256_PKCS1), any(byte[].class)))
                .thenThrow(new KmsException(KmsException.Code.INVALID_REQUEST))
                .thenReturn(
                        new KmsSignature("unexpected-key", 1, KmsSignatureAlgorithm.RSA_SHA256_PKCS1, new byte[] {1}));
        TestKeyStores.runner(file).withBean(KmsTemplate.class, () -> kms).run(context -> {
            assertThat(context).hasNotFailed().doesNotHaveBean(LocalKmsKeyStore.class);
            var encoder = context.getBean(JwtEncoder.class);
            var now = Instant.now();
            var claims = JwtClaimsSet.builder()
                    .subject("client")
                    .issuedAt(now)
                    .expiresAt(now.plusSeconds(60))
                    .build();
            for (int attempt = 0; attempt < 2; attempt++) {
                assertThatThrownBy(() -> encoder.encode(JwtEncoderParameters.from(claims)))
                        .isInstanceOf(JwtEncodingException.class)
                        .hasMessage("KMS authorization token signing failed")
                        .hasNoCause();
            }
            verify(kms, times(2)).sign(eq(key), eq(KmsSignatureAlgorithm.RSA_SHA256_PKCS1), any(byte[].class));
            assertThatThrownBy(() -> encoder.encode(JwtEncoderParameters.from(
                            JwsHeader.with(SignatureAlgorithm.RS512).build(), claims)))
                    .isInstanceOf(JwtEncodingException.class);
        });
    }

    @Test
    void unknownOrDuplicateRetainedKeyFailsStartup() throws Exception {
        var file = TestKeyStores.create(directory, "auth-current");
        for (String name : new String[] {"auth-current", "missing"}) {
            TestKeyStores.runner(file)
                    .withPropertyValues("loadup.security.auth-server.signing.verification-keys[0].name=" + name)
                    .run(context -> assertThat(context).hasFailed());
        }
    }
}
