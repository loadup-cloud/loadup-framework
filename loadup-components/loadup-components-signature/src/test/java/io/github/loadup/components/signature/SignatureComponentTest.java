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
import io.github.loadup.components.signature.enums.KeyAlgorithm;
import io.github.loadup.components.signature.enums.SignatureAlgorithm;
import io.github.loadup.components.signature.model.KeyPairInfo;
import io.github.loadup.components.signature.service.DigestService;
import io.github.loadup.components.signature.service.KeyPairService;
import io.github.loadup.components.signature.service.SignatureService;
import io.github.loadup.components.signature.util.DigestUtils;
import io.github.loadup.components.signature.util.SignatureUtils;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Signature Component 综合测试
 *
 * @author loadup
 */
@Slf4j
@SpringBootTest
@DisplayName("Signature 组件综合测试")
class SignatureComponentTest {

    @Autowired
    private SignatureService signatureService;

    @Autowired
    private DigestService digestService;

    @Autowired
    private KeyPairService keyPairService;

    @Test
    @DisplayName("RSA 签名验签 - Service 方式")
    void testRSASignatureWithService() {
        // given
        String data = "Hello, LoadUp Signature!";
        KeyPairInfo keyPair = keyPairService.generateKeyPair(KeyAlgorithm.RSA, 2048);

        // when - 签名
        String signature = signatureService.sign(data, keyPair.getPrivateKey(), SignatureAlgorithm.SHA256_WITH_RSA);

        // then - 验签
        boolean valid =
                signatureService.verify(data, signature, keyPair.getPublicKey(), SignatureAlgorithm.SHA256_WITH_RSA);

        assertThat(signature).isNotEmpty();
        assertThat(valid).isTrue();

        log.info("RSA 签名: {}", signature);
    }

    @Test
    @DisplayName("RSA 签名验签 - Utils 静态方法")
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
    @DisplayName("DSA 签名验签")
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
    @DisplayName("ECDSA 签名验签")
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
    @DisplayName("MD5 摘要 - Service 方式")
    void testMD5DigestWithService() {
        // given
        String data = "Hello, MD5!";

        // when
        String hash = digestService.digest(data, DigestAlgorithm.MD5);

        // then
        assertThat(hash).isNotEmpty();
        assertThat(hash).hasSize(32); // MD5 长度为 32 个 Hex 字符

        log.info("MD5: {}", hash);
    }

    @Test
    @DisplayName("MD5 摘要 - Utils 静态方法")
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
    @DisplayName("SHA-256 摘要")
    void testSHA256Digest() {
        // given
        String data = "Hello, SHA-256!";

        // when
        String hash1 = digestService.digest(data, DigestAlgorithm.SHA256);
        String hash2 = DigestUtils.sha256(data);

        // then
        assertThat(hash1).isNotEmpty();
        assertThat(hash1).hasSize(64); // SHA-256 长度为 64 个 Hex 字符
        assertThat(hash1).isEqualTo(hash2); // Service 和 Utils 结果应一致
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

        log.info("HMAC-SHA256: {}", hmac1);
    }

    @Test
    @DisplayName("密钥对生成")
    void testKeyPairGeneration() {
        // when - 生成 RSA 密钥对
        KeyPairInfo rsaKeyPair = keyPairService.generateKeyPair(KeyAlgorithm.RSA, 2048);

        // then
        assertThat(rsaKeyPair).isNotNull();
        assertThat(rsaKeyPair.getPublicKey()).isNotEmpty();
        assertThat(rsaKeyPair.getPrivateKey()).isNotEmpty();
        assertThat(rsaKeyPair.getAlgorithm()).isEqualTo("RSA");
        assertThat(rsaKeyPair.getKeySize()).isEqualTo(2048);

        log.info("RSA Public Key: {}", rsaKeyPair.getPublicKey());
    }

    @Test
    @DisplayName("签名篡改检测")
    void testSignatureTamperDetection() {
        // given
        String originalData = "Original data";
        String tamperedData = "Tampered data";
        KeyPairInfo keyPair = keyPairService.generateKeyPair(KeyAlgorithm.RSA);

        // when - 对原始数据签名
        String signature =
                signatureService.sign(originalData, keyPair.getPrivateKey(), SignatureAlgorithm.SHA256_WITH_RSA);

        // then - 使用篡改后的数据验签应失败
        boolean validWithTamperedData = signatureService.verify(
                tamperedData, signature, keyPair.getPublicKey(), SignatureAlgorithm.SHA256_WITH_RSA);

        assertThat(validWithTamperedData).isFalse();
    }

    @Test
    @DisplayName("摘要一致性测试")
    void testDigestConsistency() {
        // given
        String data = "Consistency test";

        // when - 多次计算同一数据的摘要
        String hash1 = digestService.digest(data, DigestAlgorithm.SHA256);
        String hash2 = digestService.digest(data, DigestAlgorithm.SHA256);
        String hash3 = DigestUtils.sha256(data);

        // then - 结果应完全一致
        assertThat(hash1).isEqualTo(hash2);
        assertThat(hash1).isEqualTo(hash3);
    }
}
