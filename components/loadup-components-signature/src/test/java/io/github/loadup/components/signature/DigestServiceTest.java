package io.github.loadup.components.signature;

import io.github.loadup.components.signature.enums.DigestAlgorithm;
import io.github.loadup.components.signature.exception.SignatureException;
import io.github.loadup.components.signature.service.DigestService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * DigestService 测试
 *
 * @author loadup
 */
@Slf4j
@SpringBootTest
@DisplayName("DigestService 测试")
class DigestServiceTest {

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
        assertThatThrownBy(() ->
            digestService.digest("data", DigestAlgorithm.HMAC_SHA256)
        )
        .isInstanceOf(SignatureException.class)
        .hasMessageContaining("HMAC 算法需要使用 hmac() 方法");
    }

    @Test
    @DisplayName("使用非 HMAC 算法调用 hmac() 应抛出异常")
    void testHmacWithNonHmacAlgorithmShouldThrowException() {
        // when & then
        assertThatThrownBy(() ->
            digestService.hmac("data".getBytes(), "key".getBytes(), DigestAlgorithm.SHA256)
        )
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
}

