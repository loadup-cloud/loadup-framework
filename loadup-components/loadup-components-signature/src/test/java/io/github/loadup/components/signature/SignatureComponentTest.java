package io.github.loadup.components.signature;

/*-
 * #%L
 * LoadUp Components :: Signature
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

import static org.assertj.core.api.Assertions.assertThat;

import io.github.loadup.components.signature.enums.DigestAlgorithm;
import io.github.loadup.components.signature.enums.KeyAlgorithm;
import io.github.loadup.components.signature.enums.SignatureAlgorithm;
import io.github.loadup.components.signature.model.KeyPairInfo;
import io.github.loadup.components.signature.service.DigestService;
import io.github.loadup.components.signature.service.KeyPairService;
import io.github.loadup.components.signature.service.SignatureService;
import io.github.loadup.components.signature.util.DigestUtils;
import io.github.loadup.components.signature.util.SignatureUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/** End-to-end tests for the signature component (service + static utils). */
@SpringBootTest
class SignatureComponentTest {

    @Autowired
    private SignatureService signatureService;

    @Autowired
    private DigestService digestService;

    @Autowired
    private KeyPairService keyPairService;

    @Test
    @DisplayName("RSA sign/verify via service")
    void testRSASignatureWithService() {
        // given
        String data = "Hello, LoadUp Signature!";
        KeyPairInfo keyPair = keyPairService.generateKeyPair(KeyAlgorithm.RSA, 2048);

        // when - sign
        String signature = signatureService.sign(data, keyPair.getPrivateKey(), SignatureAlgorithm.SHA256_WITH_RSA);

        // then - verify
        boolean valid =
                signatureService.verify(data, signature, keyPair.getPublicKey(), SignatureAlgorithm.SHA256_WITH_RSA);

        assertThat(signature).isNotEmpty();
        assertThat(valid).isTrue();
    }

    @Test
    @DisplayName("RSA sign/verify via static utils")
    void testRSASignatureWithUtils() {
        // given
        String data = "Test data for RSA";
        KeyPairInfo keyPair = keyPairService.generateKeyPair(KeyAlgorithm.RSA);

        // when
        String signature = SignatureUtils.signRSA(data, keyPair.getPrivateKey());
        boolean valid = SignatureUtils.verifyRSA(data, signature, keyPair.getPublicKey());

        // then
        assertThat(signature).isNotEmpty();
        assertThat(valid).isTrue();
    }

    @Test
    @DisplayName("DSA sign/verify")
    void testDSASignature() {
        // given
        String data = "Test DSA signature";
        KeyPairInfo keyPair = keyPairService.generateKeyPair(KeyAlgorithm.DSA);

        // when
        String signature = signatureService.sign(data, keyPair.getPrivateKey(), SignatureAlgorithm.SHA256_WITH_DSA);
        boolean valid =
                signatureService.verify(data, signature, keyPair.getPublicKey(), SignatureAlgorithm.SHA256_WITH_DSA);

        // then
        assertThat(signature).isNotEmpty();
        assertThat(valid).isTrue();
    }

    @Test
    @DisplayName("ECDSA sign/verify")
    void testECDSASignature() {
        // given
        String data = "Test ECDSA signature";
        KeyPairInfo keyPair = keyPairService.generateKeyPair(KeyAlgorithm.EC, 256);

        // when
        String signature = signatureService.sign(data, keyPair.getPrivateKey(), SignatureAlgorithm.SHA256_WITH_ECDSA);
        boolean valid =
                signatureService.verify(data, signature, keyPair.getPublicKey(), SignatureAlgorithm.SHA256_WITH_ECDSA);

        // then
        assertThat(signature).isNotEmpty();
        assertThat(valid).isTrue();
    }

    @Test
    @DisplayName("MD5 digest via service")
    void testMD5DigestWithService() {
        // given
        String data = "Hello, MD5!";

        // when
        String hash = digestService.digest(data, DigestAlgorithm.MD5);

        // then
        assertThat(hash).isNotEmpty();
        assertThat(hash).hasSize(32); // MD5 = 32 hex chars
    }

    @Test
    @DisplayName("MD5 digest via static utils")
    void testMD5DigestWithUtils() {
        // given
        String data = "Hello, MD5!";

        // when
        String hash = DigestUtils.md5(data);

        // then
        assertThat(hash).isNotEmpty();
        assertThat(hash).hasSize(32);
    }

    @Test
    @DisplayName("SHA-256 digest")
    void testSHA256Digest() {
        // given
        String data = "Hello, SHA-256!";

        // when
        String hash1 = digestService.digest(data, DigestAlgorithm.SHA256);
        String hash2 = DigestUtils.sha256(data);

        // then
        assertThat(hash1).isNotEmpty();
        assertThat(hash1).hasSize(64); // SHA-256 = 64 hex chars
        assertThat(hash1).isEqualTo(hash2); // service and utils must agree
    }

    @Test
    @DisplayName("HMAC-SHA256")
    void testHMACSHA256() {
        // given
        String data = "Hello, HMAC!";
        String key = "secret-key";

        // when
        String hmac1 = digestService.hmac(data, key, DigestAlgorithm.HMAC_SHA256);
        String hmac2 = DigestUtils.hmacSha256(data, key);

        // then
        assertThat(hmac1).isNotEmpty();
        assertThat(hmac1).isEqualTo(hmac2);
    }

    @Test
    @DisplayName("key pair generation")
    void testKeyPairGeneration() {
        // when - generate an RSA key pair
        KeyPairInfo rsaKeyPair = keyPairService.generateKeyPair(KeyAlgorithm.RSA, 2048);

        // then
        assertThat(rsaKeyPair).isNotNull();
        assertThat(rsaKeyPair.getPublicKey()).isNotEmpty();
        assertThat(rsaKeyPair.getPrivateKey()).isNotEmpty();
        assertThat(rsaKeyPair.getAlgorithm()).isEqualTo("RSA");
        assertThat(rsaKeyPair.getKeySize()).isEqualTo(2048);
    }

    @Test
    @DisplayName("signature tamper detection")
    void testSignatureTamperDetection() {
        // given
        String originalData = "Original data";
        String tamperedData = "Tampered data";
        KeyPairInfo keyPair = keyPairService.generateKeyPair(KeyAlgorithm.RSA);

        // when - sign the original data
        String signature =
                signatureService.sign(originalData, keyPair.getPrivateKey(), SignatureAlgorithm.SHA256_WITH_RSA);

        // then - verifying with tampered data must fail
        boolean validWithTamperedData = signatureService.verify(
                tamperedData, signature, keyPair.getPublicKey(), SignatureAlgorithm.SHA256_WITH_RSA);

        assertThat(validWithTamperedData).isFalse();
    }

    @Test
    @DisplayName("digest consistency")
    void testDigestConsistency() {
        // given
        String data = "Consistency test";

        // when - compute the digest multiple times
        String hash1 = digestService.digest(data, DigestAlgorithm.SHA256);
        String hash2 = digestService.digest(data, DigestAlgorithm.SHA256);
        String hash3 = DigestUtils.sha256(data);

        // then - all results must match
        assertThat(hash1).isEqualTo(hash2);
        assertThat(hash1).isEqualTo(hash3);
    }
}
