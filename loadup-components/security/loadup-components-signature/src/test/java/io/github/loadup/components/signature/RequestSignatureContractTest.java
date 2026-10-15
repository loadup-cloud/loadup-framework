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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import io.github.loadup.components.kms.KmsException;
import io.github.loadup.components.kms.KmsKeyRef;
import io.github.loadup.components.kms.KmsSignature;
import io.github.loadup.components.kms.KmsSignatureAlgorithm;
import io.github.loadup.components.kms.KmsTemplate;
import io.github.loadup.components.signature.request.RedisRequestNonceStore;
import io.github.loadup.components.signature.request.RequestContent;
import io.github.loadup.components.signature.request.RequestSignature;
import io.github.loadup.components.signature.request.RequestSignatureCanonicalizer;
import io.github.loadup.components.signature.request.RequestSignatureException;
import io.github.loadup.components.signature.request.RequestSignatureParameters;
import io.github.loadup.components.signature.request.RequestSignatureProperties;
import io.github.loadup.components.signature.request.RequestSigner;
import io.github.loadup.components.signature.request.RequestSigningIdentity;
import io.github.loadup.components.signature.request.RequestVerifier;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

class RequestSignatureContractTest {
    private static final KmsSignatureAlgorithm ALGORITHM = KmsSignatureAlgorithm.RSA_SHA256_PKCS1;
    private static final long TIMESTAMP = 1700000000L;
    private static final String NONCE = "abcdefghijklmnopqrstuv";
    private static final RequestSignatureParameters PARAMETERS =
            new RequestSignatureParameters("app", "api", 1, ALGORITHM, TIMESTAMP, NONCE);
    private static final RequestSigningIdentity IDENTITY =
            new RequestSigningIdentity("app", "api", new KmsKeyRef("merchant-key", 1), ALGORITHM);
    private static final RequestContent CONTENT = new RequestContent("GET", "/api/balance", null, new byte[0]);
    private static final Clock CLOCK = Clock.fixed(Instant.ofEpochSecond(TIMESTAMP), ZoneId.systemDefault());

    @Test
    void canonicalEmptyBodyHasStandardSha256AndPreservesNoQueryTarget() {
        String canonical =
                new String(RequestSignatureCanonicalizer.canonicalize(CONTENT, PARAMETERS), StandardCharsets.UTF_8);
        assertThat(canonical)
                .contains("method:GET\npath:/api/balance\nquery:\ncontent-type:\n")
                .endsWith("payload-sha256:e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855\n");
        assertMalformed(() -> RequestSignatureCanonicalizer.canonicalize(null, PARAMETERS));
        assertMalformed(() -> RequestSignatureCanonicalizer.canonicalize(CONTENT, null));
    }

    @Test
    void bodySnapshotAndSafeDescriptionsDoNotLeakPayloadOrSignature() {
        byte[] input = "sensitive-content".getBytes(StandardCharsets.UTF_8);
        var content = new RequestContent("POST", "/api/pay", " application/json ", input);
        input[0] = 'X';
        content.payload()[0] = 'Y';
        assertThat(new String(content.payload(), StandardCharsets.UTF_8)).isEqualTo("sensitive-content");
        assertThat(content.contentType()).isEqualTo("application/json");
        assertThat(content.toString()).contains("POST", "payloadBytes").doesNotContain("sensitive-content");
        var signature = signature(PARAMETERS, 256);
        assertThat(signature.toString()).contains("app", "keyVersion").doesNotContain(signature.signature(), NONCE);
    }

    @Test
    void rejectsInvalidHttpFieldsBeforeCanonicalization() {
        for (String method : Arrays.asList(null, "get", "A".repeat(17), "POST\r\n")) {
            assertMalformed(() -> new RequestContent(method, "/api/pay", null, new byte[0]));
        }
        for (String target : Arrays.asList(null, "api/pay", "/" + "a".repeat(8192), "/api/space here")) {
            assertMalformed(() -> new RequestContent("GET", target, null, new byte[0]));
        }
        for (String media : List.of("application/json\r\nX-Injected: true", "x".repeat(257), "é")) {
            assertMalformed(() -> new RequestContent("GET", "/api/pay", media, new byte[0]));
        }
        assertMalformed(() -> new RequestContent("GET", "/api/pay", null, null));
    }

    @Test
    void rejectsUnboundedOrAmbiguousSigningParameters() {
        for (String identifier : Arrays.asList(null, "", "a".repeat(129), "forged\nfield")) {
            assertMalformed(() -> new RequestSignatureParameters(identifier, "api", 1, ALGORITHM, TIMESTAMP, NONCE));
            assertMalformed(() -> new RequestSignatureParameters("app", identifier, 1, ALGORITHM, TIMESTAMP, NONCE));
        }
        assertMalformed(() -> new RequestSignatureParameters("app", "api", 0, ALGORITHM, TIMESTAMP, NONCE));
        assertMalformed(() -> new RequestSignatureParameters("app", "api", 1, null, TIMESTAMP, NONCE));
        assertMalformed(() -> new RequestSignatureParameters("app", "api", 1, ALGORITHM, -1, NONCE));
        assertMalformed(() -> new RequestSignatureParameters("app", "api", 1, ALGORITHM, Long.MAX_VALUE, NONCE));
        for (String nonce : Arrays.asList(null, "short", "x".repeat(129), "invalid+nonce/value-with-padding=")) {
            assertMalformed(() -> new RequestSignatureParameters("app", "api", 1, ALGORITHM, TIMESTAMP, nonce));
        }
        assertThatThrownBy(() -> new RequestSigningIdentity("app", "api", null, ALGORITHM))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new RequestSigningIdentity("app", "api", KmsKeyRef.latest("merchant-key"), ALGORITHM))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new RequestSigningIdentity("app", "api", IDENTITY.key(), null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void signatureEncodingAcceptsRsaSizesAndRejectsNonCanonicalOrInvalidEncodings() {
        for (int bytes : new int[] {256, 384, 512}) {
            assertThat(RequestSignature.fromHeaders(headers(signature(PARAMETERS, bytes))))
                    .isEqualTo(signature(PARAMETERS, bytes));
        }
        String padded = signature(PARAMETERS, 256).signature();
        for (String encoded : Arrays.asList(
                null,
                "!invalid!",
                "",
                padded.substring(0, padded.length() - 1),
                Base64.getEncoder().encodeToString(new byte[255]),
                "A".repeat(685))) {
            assertMalformed(() -> new RequestSignature(PARAMETERS, encoded));
        }
        assertMalformed(() -> new RequestSignature(null, padded));
    }

    @Test
    void headersRejectMissingValuesWrongProtocolAndNonCanonicalNumbers() {
        assertMalformed(() -> RequestSignature.fromHeaders(null));
        assertMalformed(() -> RequestSignature.fromHeaders(Map.of()));
        var valid = signature(PARAMETERS, 256);
        for (Map.Entry<String, String> replacement : Map.of(
                        "X-Signature-Protocol",
                        "loadup-request-v2",
                        "X-Key-Version",
                        "01",
                        "X-Timestamp",
                        "01700000000",
                        "X-Signature-Algorithm",
                        "SHA256_WITH_RSA")
                .entrySet()) {
            var headers = headers(valid);
            headers.put(replacement.getKey(), List.of(replacement.getValue()));
            assertMalformed(() -> RequestSignature.fromHeaders(headers));
        }
        List<List<String>> invalidValues = new ArrayList<>();
        invalidValues.add(null);
        invalidValues.add(List.of());
        invalidValues.add(Arrays.asList((String) null));
        invalidValues.add(List.of("x".repeat(1025)));
        for (List<String> values : invalidValues) {
            var headers = headers(valid);
            headers.put("X-App-Id", values);
            assertMalformed(() -> RequestSignature.fromHeaders(headers));
        }
        var lowercase = new HashMap<String, List<String>>();
        valid.toHeaders()
                .forEach((key, value) -> lowercase.put(key.toLowerCase(java.util.Locale.ROOT), List.of(value)));
        lowercase.put(null, List.of("ignored"));
        assertThat(RequestSignature.fromHeaders(lowercase)).isEqualTo(valid);
    }

    @Test
    void rejectsMismatchedKmsResponsesInsteadOfPublishingUntrustedMetadata() {
        var kms = mock(KmsTemplate.class);
        var signer = new RequestSigner(kms, CLOCK, new RequestSignatureProperties(null, null, null));
        for (KmsSignature response : Arrays.asList(
                null,
                new KmsSignature("other-key", 1, ALGORITHM, new byte[256]),
                new KmsSignature("merchant-key", 2, ALGORITHM, new byte[256]),
                new KmsSignature("merchant-key", 1, KmsSignatureAlgorithm.RSA_SHA256_PSS, new byte[256]),
                new KmsSignature("merchant-key", 1, ALGORITHM, new byte[1]))) {
            when(kms.sign(any(), any(), any())).thenReturn(response);
            assertThatThrownBy(() -> signer.sign(IDENTITY, CONTENT))
                    .isInstanceOf(KmsException.class)
                    .satisfies(error -> assertThat(((KmsException) error).getResultCode())
                            .isEqualTo(KmsException.Code.INVALID_RESPONSE));
        }
    }

    @Test
    void missingAndOversizedContentFailBeforeEitherRemoteBoundary() {
        var kms = mock(KmsTemplate.class);
        var nonce = mock(io.github.loadup.components.signature.request.RequestNonceStore.class);
        var policy = new RequestSignatureProperties(Duration.ofSeconds(2), Duration.ZERO, 1);
        var signer = new RequestSigner(kms, CLOCK, policy);
        var verifier = new RequestVerifier(kms, nonce, CLOCK, policy);
        assertMalformed(() -> signer.sign(IDENTITY, null));
        assertMalformed(() -> verifier.verify(IDENTITY, CONTENT, null));
        var oversized = new RequestContent("POST", "/api/pay", null, new byte[2]);
        assertThatThrownBy(() -> verifier.verify(IDENTITY, oversized, signature(PARAMETERS, 256)))
                .isInstanceOf(RequestSignatureException.class)
                .satisfies(error -> assertThat(((RequestSignatureException) error).getCode())
                        .isEqualTo(RequestSignatureException.Code.PAYLOAD_TOO_LARGE));
        verifyNoInteractions(kms, nonce);
    }

    @Test
    void requestSettingsRejectUnsafeResourceAndTimeBounds() {
        for (Duration age : List.of(Duration.ZERO, Duration.ofDays(1).plusNanos(1))) {
            assertThatThrownBy(() -> new RequestSignatureProperties(age, Duration.ZERO, 1))
                    .isInstanceOf(IllegalArgumentException.class);
        }
        for (Duration skew : List.of(Duration.ofNanos(-1), Duration.ofMinutes(5).plusNanos(1))) {
            assertThatThrownBy(() -> new RequestSignatureProperties(Duration.ofSeconds(1), skew, 1))
                    .isInstanceOf(IllegalArgumentException.class);
        }
        for (int size : new int[] {0, 16 * 1024 * 1024 + 1}) {
            assertThatThrownBy(() -> new RequestSignatureProperties(Duration.ofSeconds(1), Duration.ZERO, size))
                    .isInstanceOf(IllegalArgumentException.class);
        }
        assertThat(new RequestSignatureProperties(Duration.ofDays(1), Duration.ofMinutes(5), 16 * 1024 * 1024)
                        .maxPayloadBytes())
                .isEqualTo(16 * 1024 * 1024);
    }

    @Test
    @SuppressWarnings("unchecked")
    void redisNonceValidationPreventsInvalidClaimsAndUsesAnExactAtomicExpiry() {
        var redis = mock(StringRedisTemplate.class);
        ValueOperations<String, String> values = mock(ValueOperations.class);
        var store = new RedisRequestNonceStore(redis);
        for (String replayKey : Arrays.asList(null, "A".repeat(64), "a".repeat(63))) {
            assertThatThrownBy(() -> store.claim(replayKey, Duration.ofSeconds(1)))
                    .isInstanceOf(IllegalArgumentException.class);
        }
        for (Duration ttl : Arrays.asList(null, Duration.ZERO, Duration.ofNanos(-1))) {
            assertThatThrownBy(() -> store.claim("a".repeat(64), ttl)).isInstanceOf(IllegalArgumentException.class);
        }
        verifyNoInteractions(redis);
        when(redis.opsForValue()).thenReturn(values);
        when(values.setIfAbsent("loadup:signature:nonce:" + "a".repeat(64), "1", Duration.ofMillis(1)))
                .thenReturn(true);
        assertThat(store.claim("a".repeat(64), Duration.ofMillis(1))).isTrue();
        verify(values).setIfAbsent("loadup:signature:nonce:" + "a".repeat(64), "1", Duration.ofMillis(1));
    }

    private static RequestSignature signature(RequestSignatureParameters parameters, int size) {
        return new RequestSignature(parameters, Base64.getEncoder().encodeToString(new byte[size]));
    }

    private static Map<String, List<String>> headers(RequestSignature signature) {
        var headers = new HashMap<String, List<String>>();
        signature.toHeaders().forEach((key, value) -> headers.put(key, List.of(value)));
        return headers;
    }

    private static void assertMalformed(Runnable operation) {
        assertThatThrownBy(operation::run)
                .isInstanceOf(RequestSignatureException.class)
                .satisfies(error -> assertThat(((RequestSignatureException) error).getCode())
                        .isEqualTo(RequestSignatureException.Code.MALFORMED));
    }
}
