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
import io.github.loadup.components.signature.util.DigestUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** Tests for the {@code DigestUtils} static helpers. */
class DigestUtilsTest {

    @Test
    @DisplayName("MD5 quick method")
    void testMD5() {
        // given
        String data = "Hello, MD5!";

        // when
        String hash = DigestUtils.md5(data);

        // then
        assertThat(hash).isNotEmpty();
        assertThat(hash).hasSize(32);

        // must match the generic digest method
        assertThat(hash).isEqualTo(DigestUtils.digest(data, DigestAlgorithm.MD5));
    }

    @Test
    @DisplayName("SHA-256 quick method")
    void testSHA256() {
        // given
        String data = "Hello, SHA-256!";

        // when
        String hash = DigestUtils.sha256(data);

        // then
        assertThat(hash).isNotEmpty();
        assertThat(hash).hasSize(64);

        // must match the generic digest method
        assertThat(hash).isEqualTo(DigestUtils.digest(data, DigestAlgorithm.SHA256));
    }

    @Test
    @DisplayName("SHA-512 quick method")
    void testSHA512() {
        // given
        String data = "Hello, SHA-512!";

        // when
        String hash = DigestUtils.sha512(data);

        // then
        assertThat(hash).isNotEmpty();
        assertThat(hash).hasSize(128);

        // must match the generic digest method
        assertThat(hash).isEqualTo(DigestUtils.digest(data, DigestAlgorithm.SHA512));
    }

    @Test
    @DisplayName("HMAC-SHA256 quick method")
    void testHmacSHA256() {
        // given
        String data = "Test HMAC";
        String key = "secret-key";

        // when
        String hmac = DigestUtils.hmacSha256(data, key);

        // then
        assertThat(hmac).isNotEmpty();
        assertThat(hmac).hasSize(64);

        // must match the generic hmac method
        assertThat(hmac).isEqualTo(DigestUtils.hmac(data, key, DigestAlgorithm.HMAC_SHA256));
    }

    @Test
    @DisplayName("HMAC-SHA512 quick method")
    void testHmacSHA512() {
        // given
        String data = "Test HMAC 512";
        String key = "secret-key";

        // when
        String hmac = DigestUtils.hmacSha512(data, key);

        // then
        assertThat(hmac).isNotEmpty();
        assertThat(hmac).hasSize(128);

        // must match the generic hmac method
        assertThat(hmac).isEqualTo(DigestUtils.hmac(data, key, DigestAlgorithm.HMAC_SHA512));
    }

    @Test
    @DisplayName("MD5 known value")
    void testMD5KnownValue() {
        // MD5("hello") = 5d41402abc4b2a76b9719d911017c592
        String hash = DigestUtils.md5("hello");
        assertThat(hash).isEqualTo("5d41402abc4b2a76b9719d911017c592");
    }

    @Test
    @DisplayName("SHA-256 known value")
    void testSHA256KnownValue() {
        // SHA-256("hello") = 2cf24dba5fb0a30e26e83b2ac5b9e29e1b161e5c1fa7425e73043362938b9824
        String hash = DigestUtils.sha256("hello");
        assertThat(hash).isEqualTo("2cf24dba5fb0a30e26e83b2ac5b9e29e1b161e5c1fa7425e73043362938b9824");
    }

    @Test
    @DisplayName("empty string MD5")
    void testEmptyStringMD5() {
        // MD5("") = d41d8cd98f00b204e9800998ecf8427e
        String hash = DigestUtils.md5("");
        assertThat(hash).isEqualTo("d41d8cd98f00b204e9800998ecf8427e");
    }
}
