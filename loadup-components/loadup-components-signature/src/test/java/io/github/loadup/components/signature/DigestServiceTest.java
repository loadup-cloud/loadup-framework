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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.github.loadup.components.signature.enums.DigestAlgorithm;
import io.github.loadup.components.signature.exception.SignatureException;
import io.github.loadup.components.signature.service.DigestService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/** Tests for the {@code DigestService} facade. */
@SpringBootTest
class DigestServiceTest {

    @Autowired
    private DigestService digestService;

    @Test
    @DisplayName("MD5 digest")
    void testMD5() {
        // given
        String data = "Hello, LoadUp!";

        // when
        String hash = digestService.digest(data, DigestAlgorithm.MD5);

        // then
        assertThat(hash).isNotEmpty();
        assertThat(hash).hasSize(32); // MD5 = 128 bit = 32 Hex

        // digest must be deterministic
        String hash2 = digestService.digest(data, DigestAlgorithm.MD5);
        assertThat(hash).isEqualTo(hash2);
    }

    @Test
    @DisplayName("SHA-1 digest")
    void testSHA1() {
        // given
        String data = "Test SHA-1";

        // when
        String hash = digestService.digest(data, DigestAlgorithm.SHA1);

        // then
        assertThat(hash).isNotEmpty();
        assertThat(hash).hasSize(40); // SHA-1 = 160 bit = 40 Hex
    }

    @Test
    @DisplayName("SHA-256 digest")
    void testSHA256() {
        // given
        String data = "Test SHA-256";

        // when
        String hash = digestService.digest(data, DigestAlgorithm.SHA256);

        // then
        assertThat(hash).isNotEmpty();
        assertThat(hash).hasSize(64); // SHA-256 = 256 bit = 64 Hex
    }

    @Test
    @DisplayName("SHA-512 digest")
    void testSHA512() {
        // given
        String data = "Test SHA-512";

        // when
        String hash = digestService.digest(data, DigestAlgorithm.SHA512);

        // then
        assertThat(hash).isNotEmpty();
        assertThat(hash).hasSize(128); // SHA-512 = 512 bit = 128 Hex
    }

    @Test
    @DisplayName("HMAC-SHA256")
    void testHmacSHA256() {
        // given
        String data = "Test HMAC";
        String key = "secret-key-123";

        // when
        String hmac = digestService.hmac(data, key, DigestAlgorithm.HMAC_SHA256);

        // then
        assertThat(hmac).isNotEmpty();
        assertThat(hmac).hasSize(64);

        // same data and key must produce the same MAC
        String hmac2 = digestService.hmac(data, key, DigestAlgorithm.HMAC_SHA256);
        assertThat(hmac).isEqualTo(hmac2);
    }

    @Test
    @DisplayName("HMAC-SHA512")
    void testHmacSHA512() {
        // given
        String data = "Test HMAC 512";
        String key = "secret-key-456";

        // when
        String hmac = digestService.hmac(data, key, DigestAlgorithm.HMAC_SHA512);

        // then
        assertThat(hmac).isNotEmpty();
        assertThat(hmac).hasSize(128);
    }

    @Test
    @DisplayName("different keys produce different MACs")
    void testHmacWithDifferentKeys() {
        // given
        String data = "Same data";
        String key1 = "key1";
        String key2 = "key2";

        // when
        String hmac1 = digestService.hmac(data, key1, DigestAlgorithm.HMAC_SHA256);
        String hmac2 = digestService.hmac(data, key2, DigestAlgorithm.HMAC_SHA256);

        // then
        assertThat(hmac1).isNotEqualTo(hmac2);
    }

    @Test
    @DisplayName("digest() rejects HMAC algorithms")
    void testDigestWithHmacAlgorithmShouldThrowException() {
        // when & then
        assertThatThrownBy(() -> digestService.digest("data", DigestAlgorithm.HMAC_SHA256))
                .isInstanceOf(SignatureException.class)
                .hasMessageContaining("must use the hmac() method");
    }

    @Test
    @DisplayName("hmac() rejects non-HMAC algorithms")
    void testHmacWithNonHmacAlgorithmShouldThrowException() {
        // when & then
        assertThatThrownBy(() -> digestService.hmac("data".getBytes(), "key".getBytes(), DigestAlgorithm.SHA256))
                .isInstanceOf(SignatureException.class)
                .hasMessageContaining("must use the digest() method");
    }

    @Test
    @DisplayName("empty string digest")
    void testEmptyStringDigest() {
        // given
        String emptyData = "";

        // when
        String hash = digestService.digest(emptyData, DigestAlgorithm.SHA256);

        // then
        assertThat(hash).isNotEmpty();
        // SHA-256 of the empty string is a fixed value
        assertThat(hash).isEqualTo("e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855");
    }
}
