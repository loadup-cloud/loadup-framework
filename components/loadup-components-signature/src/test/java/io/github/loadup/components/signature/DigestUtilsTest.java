package io.github.loadup.components.signature;

/*-
 * #%L
 * LoadUp Components :: Signature
 * %%
 * Copyright (C) 2025 - 2026 LoadUp Cloud
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

import static org.assertj.core.api.Assertions.assertThat;

import io.github.loadup.components.signature.enums.DigestAlgorithm;
import io.github.loadup.components.signature.util.DigestUtils;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * DigestUtils 工具类测试
 *
 * @author loadup
 */
@Slf4j
@DisplayName("DigestUtils 工具类测试")
class DigestUtilsTest {

    @Test
    @DisplayName("MD5 快速方法")
    void testMD5() {
        // given
        String data = "Hello, MD5!";

        // when
        String hash = DigestUtils.md5(data);

        // then
        assertThat(hash).isNotEmpty();
        assertThat(hash).hasSize(32);

        // 与通用方法结果一致
        assertThat(hash).isEqualTo(DigestUtils.digest(data, DigestAlgorithm.MD5));
    }

    @Test
    @DisplayName("SHA-256 快速方法")
    void testSHA256() {
        // given
        String data = "Hello, SHA-256!";

        // when
        String hash = DigestUtils.sha256(data);

        // then
        assertThat(hash).isNotEmpty();
        assertThat(hash).hasSize(64);

        // 与通用方法结果一致
        assertThat(hash).isEqualTo(DigestUtils.digest(data, DigestAlgorithm.SHA256));
    }

    @Test
    @DisplayName("SHA-512 快速方法")
    void testSHA512() {
        // given
        String data = "Hello, SHA-512!";

        // when
        String hash = DigestUtils.sha512(data);

        // then
        assertThat(hash).isNotEmpty();
        assertThat(hash).hasSize(128);

        // 与通用方法结果一致
        assertThat(hash).isEqualTo(DigestUtils.digest(data, DigestAlgorithm.SHA512));
    }

    @Test
    @DisplayName("HMAC-SHA256 快速方法")
    void testHmacSHA256() {
        // given
        String data = "Test HMAC";
        String key = "secret-key";

        // when
        String hmac = DigestUtils.hmacSha256(data, key);

        // then
        assertThat(hmac).isNotEmpty();
        assertThat(hmac).hasSize(64);

        // 与通用方法结果一致
        assertThat(hmac).isEqualTo(DigestUtils.hmac(data, key, DigestAlgorithm.HMAC_SHA256));
    }

    @Test
    @DisplayName("HMAC-SHA512 快速方法")
    void testHmacSHA512() {
        // given
        String data = "Test HMAC 512";
        String key = "secret-key";

        // when
        String hmac = DigestUtils.hmacSha512(data, key);

        // then
        assertThat(hmac).isNotEmpty();
        assertThat(hmac).hasSize(128);

        // 与通用方法结果一致
        assertThat(hmac).isEqualTo(DigestUtils.hmac(data, key, DigestAlgorithm.HMAC_SHA512));
    }

    @Test
    @DisplayName("MD5 已知值测试")
    void testMD5KnownValue() {
        // MD5("hello") = 5d41402abc4b2a76b9719d911017c592
        String hash = DigestUtils.md5("hello");
        assertThat(hash).isEqualTo("5d41402abc4b2a76b9719d911017c592");
    }

    @Test
    @DisplayName("SHA-256 已知值测试")
    void testSHA256KnownValue() {
        // SHA-256("hello") = 2cf24dba5fb0a30e26e83b2ac5b9e29e1b161e5c1fa7425e73043362938b9824
        String hash = DigestUtils.sha256("hello");
        assertThat(hash).isEqualTo("2cf24dba5fb0a30e26e83b2ac5b9e29e1b161e5c1fa7425e73043362938b9824");
    }

    @Test
    @DisplayName("空字符串 MD5")
    void testEmptyStringMD5() {
        // MD5("") = d41d8cd98f00b204e9800998ecf8427e
        String hash = DigestUtils.md5("");
        assertThat(hash).isEqualTo("d41d8cd98f00b204e9800998ecf8427e");
    }
}
