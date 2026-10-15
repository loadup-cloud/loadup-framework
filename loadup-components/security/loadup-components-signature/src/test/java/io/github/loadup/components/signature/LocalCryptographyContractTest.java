/*
 * #%L
 * LoadUp Components Signature
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
package io.github.loadup.components.signature;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.github.loadup.components.signature.enums.DigestAlgorithm;
import io.github.loadup.components.signature.enums.KeyAlgorithm;
import io.github.loadup.components.signature.enums.SignatureAlgorithm;
import io.github.loadup.components.signature.exception.SignatureException;
import io.github.loadup.components.signature.properties.SignatureProperties;
import io.github.loadup.components.signature.service.impl.DigestServiceImpl;
import io.github.loadup.components.signature.service.impl.KeyPairServiceImpl;
import io.github.loadup.components.signature.service.impl.SignatureServiceImpl;
import io.github.loadup.components.signature.util.DigestUtils;
import io.github.loadup.components.signature.util.SignatureUtils;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

class LocalCryptographyContractTest {
    private final KeyPairServiceImpl keys = new KeyPairServiceImpl(new SignatureProperties());
    private final SignatureServiceImpl signatures = new SignatureServiceImpl(keys);
    private final DigestServiceImpl digests = new DigestServiceImpl();

    @ParameterizedTest
    @EnumSource(SignatureAlgorithm.class)
    void encodedAndJcaKeysInteroperateAndRejectTamperedPayloads(SignatureAlgorithm algorithm) {
        var keyType = KeyAlgorithm.valueOf(algorithm.getKeyAlgorithm());
        var pair = keys.generateKeyPair(keyType);
        var privateKey = keys.loadPrivateKey(pair.getPrivateKey(), keyType);
        var publicKey = keys.loadPublicKey(pair.getPublicKey(), keyType);
        assertThat(privateKey.getAlgorithm()).isEqualTo(algorithm.getKeyAlgorithm());
        assertThat(publicKey.getAlgorithm()).isEqualTo(algorithm.getKeyAlgorithm());
        assertThat(pair.getAlgorithm()).isEqualTo(algorithm.getKeyAlgorithm());
        assertThat(pair.getKeySize()).isPositive();
        String payload = "payment-123:100.00:CNY";
        String signature = signatures.sign(payload, pair.getPrivateKey(), algorithm);
        assertThat(SignatureUtils.verify(payload, signature, pair.getPublicKey(), algorithm))
                .isTrue();
        assertThat(signatures.verify("tampered", signature, pair.getPublicKey(), algorithm))
                .isFalse();
        String utilitySignature = SignatureUtils.sign(payload, pair.getPrivateKey(), algorithm);
        assertThat(signatures.verify(payload, utilitySignature, pair.getPublicKey(), algorithm))
                .isTrue();
        String binarySignature = signatures.sign(payload.getBytes(StandardCharsets.UTF_8), privateKey, algorithm);
        assertThat(signatures.verify(payload.getBytes(StandardCharsets.UTF_8), binarySignature, publicKey, algorithm))
                .isTrue();
    }

    @Test
    void rsaShortcutsProduceVerifiableSignatures() {
        var pair = keys.generateKeyPair(KeyAlgorithm.RSA, 2048);
        String signature = SignatureUtils.signRSA("invoice", pair.getPrivateKey());
        assertThat(SignatureUtils.verifyRSA("invoice", signature, pair.getPublicKey()))
                .isTrue();
        assertThat(SignatureUtils.verifyRSA("changed", signature, pair.getPublicKey()))
                .isFalse();
    }

    @ParameterizedTest
    @EnumSource(DigestAlgorithm.class)
    void digestAndMacResultsMatchIndependentJcaImplementations(DigestAlgorithm algorithm) throws Exception {
        byte[] data = "payment-123:100.00:CNY".getBytes(StandardCharsets.UTF_8);
        byte[] secret = "synthetic-test-key".getBytes(StandardCharsets.UTF_8);
        if (algorithm.isHmac()) {
            Mac jca = Mac.getInstance(algorithm.getJcaName());
            jca.init(new SecretKeySpec(secret, algorithm.getJcaName()));
            String expected = HexFormat.of().formatHex(jca.doFinal(data));
            assertThat(digests.hmac(data, secret, algorithm)).isEqualTo(expected);
            assertThat(digests.hmac(new String(data, StandardCharsets.UTF_8), "synthetic-test-key", algorithm))
                    .isEqualTo(expected);
            assertThat(DigestUtils.hmac(new String(data, StandardCharsets.UTF_8), "synthetic-test-key", algorithm))
                    .isEqualTo(expected);
            assertThatThrownBy(() -> digests.digest(data, algorithm)).isInstanceOf(SignatureException.class);
        } else {
            String expected = HexFormat.of()
                    .formatHex(MessageDigest.getInstance(algorithm.getJcaName()).digest(data));
            assertThat(digests.digest(data, algorithm)).isEqualTo(expected);
            assertThat(digests.digest(new String(data, StandardCharsets.UTF_8), algorithm))
                    .isEqualTo(expected);
            assertThat(DigestUtils.digest(new String(data, StandardCharsets.UTF_8), algorithm))
                    .isEqualTo(expected);
            assertThatThrownBy(() -> digests.hmac(data, secret, algorithm)).isInstanceOf(SignatureException.class);
        }
    }

    @Test
    void malformedKeysAndPayloadsPreserveCategorizedFailures() {
        var algorithm = SignatureAlgorithm.SHA256_WITH_RSA;
        assertCode(
                () -> keys.generateKeyPair(KeyAlgorithm.RSA, 1),
                SignatureException.SignatureErrorCode.KEY_GENERATION_FAILED);
        assertCode(
                () -> keys.loadPrivateKey("not-base64", KeyAlgorithm.RSA),
                SignatureException.SignatureErrorCode.INVALID_KEY);
        assertCode(
                () -> keys.loadPublicKey("not-base64", KeyAlgorithm.RSA),
                SignatureException.SignatureErrorCode.INVALID_KEY);
        assertCode(
                () -> signatures.sign("payload", "not-base64", algorithm),
                SignatureException.SignatureErrorCode.INVALID_KEY);
        assertCode(
                () -> signatures.verify("payload", "bad", "not-base64", algorithm),
                SignatureException.SignatureErrorCode.INVALID_KEY);
        assertCode(
                () -> signatures.sign((String) null, "bad", algorithm),
                SignatureException.SignatureErrorCode.SIGN_FAILED);
        assertCode(
                () -> signatures.verify((String) null, "bad", "bad", algorithm),
                SignatureException.SignatureErrorCode.VERIFY_FAILED);
        assertCode(
                () -> signatures.sign(new byte[0], null, algorithm), SignatureException.SignatureErrorCode.SIGN_FAILED);
        assertCode(
                () -> signatures.verify(new byte[0], "bad", null, algorithm),
                SignatureException.SignatureErrorCode.VERIFY_FAILED);
        assertCode(() -> SignatureUtils.signRSA("payload", "bad"), SignatureException.SignatureErrorCode.SIGN_FAILED);
        assertCode(
                () -> SignatureUtils.verifyRSA("payload", "bad", "bad"),
                SignatureException.SignatureErrorCode.VERIFY_FAILED);
        assertCode(
                () -> digests.digest((byte[]) null, DigestAlgorithm.SHA256),
                SignatureException.SignatureErrorCode.DIGEST_FAILED);
        assertCode(
                () -> digests.hmac(new byte[0], new byte[0], DigestAlgorithm.HMAC_SHA256),
                SignatureException.SignatureErrorCode.DIGEST_FAILED);
        assertCode(
                () -> DigestUtils.digest(null, DigestAlgorithm.SHA256),
                SignatureException.SignatureErrorCode.DIGEST_FAILED);
        assertCode(
                () -> DigestUtils.hmac("payload", "", DigestAlgorithm.HMAC_SHA256),
                SignatureException.SignatureErrorCode.DIGEST_FAILED);
    }

    private static void assertCode(Runnable operation, SignatureException.SignatureErrorCode expected) {
        assertThatThrownBy(operation::run).isInstanceOfSatisfying(SignatureException.class, error -> {
            assertThat(error.getErrorCode()).isEqualTo(expected);
            assertThat(error.getCause()).isNotNull();
            assertThat(error.getMessage()).isEqualTo(expected.getMessage());
        });
    }
}
