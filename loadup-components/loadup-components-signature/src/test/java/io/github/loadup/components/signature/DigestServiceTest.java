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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * DigestService 测试
 *
 * @author loadup
 */
@SpringBootTest
@DisplayName("DigestService 测试")
class DigestServiceTest {
    private static final Logger log = LoggerFactory.getLogger(DigestServiceTest.class);

    @Autowired
    private DigestService digestService;

    @Test
    @DisplayName("MD5 摘要计算")
    void testMD5() {
        // given
        String data = "Hello, LoadUp!";

        // when
        String hash = digestService.digest(data, DigestAlgorithm.MD5);

        // then
        assertThat(hash).isNotEmpty();
        assertThat(hash).hasSize(32); // MD5 = 128 bit = 32 Hex

        // MD5 应该是确定性的
        String hash2 = digestService.digest(data, DigestAlgorithm.MD5);
        assertThat(hash).isEqualTo(hash2);

        log.info("MD5: {}", hash);
    }

    @Test
    @DisplayName("SHA-1 摘要计算")
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
    @DisplayName("SHA-256 摘要计算")
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
    @DisplayName("SHA-512 摘要计算")
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
    @DisplayName("HMAC-SHA256 计算")
    void testHmacSHA256() {
        // given
        String data = "Test HMAC";
        String key = "secret-key-123";

        // when
        String hmac = digestService.hmac(data, key, DigestAlgorithm.HMAC_SHA256);

        // then
        assertThat(hmac).isNotEmpty();
        assertThat(hmac).hasSize(64);

        // 同样的数据和密钥应该产生相同的 HMAC
        String hmac2 = digestService.hmac(data, key, DigestAlgorithm.HMAC_SHA256);
        assertThat(hmac).isEqualTo(hmac2);
    }

    @Test
    @DisplayName("HMAC-SHA512 计算")
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
    @DisplayName("不同密钥产生不同的 HMAC")
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
    @DisplayName("使用 HMAC 算法调用 digest() 应抛出异常")
    void testDigestWithHmacAlgorithmShouldThrowException() {
        // when & then
        assertThatThrownBy(() -> digestService.digest("data", DigestAlgorithm.HMAC_SHA256))
                .isInstanceOf(SignatureException.class)
                .hasMessageContaining("HMAC 算法需要使用 hmac() 方法");
    }

    @Test
    @DisplayName("使用非 HMAC 算法调用 hmac() 应抛出异常")
    void testHmacWithNonHmacAlgorithmShouldThrowException() {
        // when & then
        assertThatThrownBy(() -> digestService.hmac("data".getBytes(), "key".getBytes(), DigestAlgorithm.SHA256))
                .isInstanceOf(SignatureException.class)
                .hasMessageContaining("非 HMAC 算法需要使用 digest() 方法");
    }

    @Test
    @DisplayName("空字符串摘要计算")
    void testEmptyStringDigest() {
        // given
        String emptyData = "";

        // when
        String hash = digestService.digest(emptyData, DigestAlgorithm.SHA256);

        // then
        assertThat(hash).isNotEmpty();
        // 空字符串的 SHA-256 是固定值
        assertThat(hash).isEqualTo("e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855");
    }

    public Logger getLog() {
        return this.log;
    }

    public DigestService getDigestService() {
        return this.digestService;
    }
}
