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
package io.github.loadup.components.signature.request;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import io.github.loadup.components.kms.KmsKeyRef;
import io.github.loadup.components.kms.KmsSignature;
import io.github.loadup.components.kms.KmsSignatureAlgorithm;
import io.github.loadup.components.kms.KmsTemplate;
import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.Signature;
import java.security.spec.MGF1ParameterSpec;
import java.security.spec.PSSParameterSpec;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class RequestSignatureTest {
    private final KmsTemplate kms = mock(KmsTemplate.class);
    private final MutableClock clock = new MutableClock(Instant.ofEpochSecond(1700000000));
    private final RequestSignatureProperties properties = new RequestSignatureProperties(null, null, null);
    private final Map<String, Duration> claims = new ConcurrentHashMap<>();
    private final RequestNonceStore store = (key, ttl) -> claims.putIfAbsent(key, ttl) == null;
    private final RequestSigningIdentity identity = new RequestSigningIdentity(
            "merchant-app", "acquiring-api", new KmsKeyRef("merchant-key", 1), KmsSignatureAlgorithm.RSA_SHA256_PKCS1);
    private final RequestContent content = new RequestContent(
            "POST",
            "/api/pay?item=%2F&item=2",
            "application/json",
            "{\"amount\":100}".getBytes(StandardCharsets.UTF_8));
    private RequestSigner signer;
    private RequestVerifier verifier;
    private KeyPair pair;

    @BeforeEach
    void start() throws Exception {
        var generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(2048);
        pair = generator.generateKeyPair();
        when(kms.sign(any(), any(), any())).thenAnswer(invocation -> {
            KmsKeyRef key = invocation.getArgument(0);
            KmsSignatureAlgorithm algorithm = invocation.getArgument(1);
            var jca = signature(algorithm);
            jca.initSign(pair.getPrivate());
            jca.update((byte[]) invocation.getArgument(2));
            return new KmsSignature(key.name(), key.version(), algorithm, jca.sign());
        });
        when(kms.verify(any(), any(), any())).thenAnswer(invocation -> {
            KmsSignature signed = invocation.getArgument(1);
            var jca = signature(signed.algorithm());
            jca.initVerify(pair.getPublic());
            jca.update((byte[]) invocation.getArgument(2));
            return jca.verify(signed.bytes());
        });
        signer = new RequestSigner(kms, clock, properties);
        verifier = new RequestVerifier(kms, store, clock, properties);
    }

    @Test
    void publishesStableCanonicalVectorIncludingRawQueryAndTrailingLf() {
        var parameters = new RequestSignatureParameters(
                "merchant-app", "acquiring-api", 1, identity.algorithm(), 1700000000, "abcdefghijklmnopqrstuv");
        assertThat(new String(RequestSignatureCanonicalizer.canonicalize(content, parameters), StandardCharsets.UTF_8))
                .isEqualTo("""
                loadup-request-v1
                app-id:merchant-app
                audience:acquiring-api
                key-version:1
                algorithm:RSA_SHA256_PKCS1
                method:POST
                path:/api/pay
                query:?item=%2F&item=2
                content-type:application/json
                timestamp:1700000000
                nonce:abcdefghijklmnopqrstuv
                payload-sha256:4d4bbe59c6aad22442cde199a6a8a5f034405fcd78fb5a81c24ef249de1c45f1
                """);
    }

    @Test
    void signsRoundTripsHeadersAndConsumesNonceExactlyOnce() {
        var signed = signer.sign(identity, content);
        var parsed = RequestSignature.fromHeaders(headers(signed));
        assertThat(parsed).isEqualTo(signed);
        assertThat(signed.parameters().nonce()).hasSize(32);
        verifier.verify(identity, content, parsed);
        assertThat(claims).hasSize(1);
        assertThat(claims.values()).containsExactly(Duration.ofSeconds(330));
        assertThatThrownBy(() -> verifier.verify(identity, content, parsed))
                .hasMessage("Request signature rejected: REPLAY");
    }

    @Test
    void rejectsChangesToMethodPathQueryContentTypeAndPayloadWithoutBurningNonce() {
        var signed = signer.sign(identity, content);
        for (var changed : List.of(
                new RequestContent("GET", content.target(), content.contentType(), content.payload()),
                new RequestContent("POST", "/api/refund?item=%2F&item=2", content.contentType(), content.payload()),
                new RequestContent("POST", "/api/pay?item=2&item=%2F", content.contentType(), content.payload()),
                new RequestContent("POST", "/api/pay?item=%2f&item=2", content.contentType(), content.payload()),
                new RequestContent("POST", content.target(), "text/plain", content.payload()),
                new RequestContent(
                        "POST",
                        content.target(),
                        content.contentType(),
                        "{\"amount\": 100}".getBytes(StandardCharsets.UTF_8)))) {
            assertThatThrownBy(() -> verifier.verify(identity, changed, signed))
                    .hasMessage("Request signature rejected: INVALID_SIGNATURE");
        }
        assertThat(claims).isEmpty();
        verifier.verify(identity, content, signed);
    }

    @Test
    void rejectsChangedTimestampAndNonceCryptographically() {
        var signed = signer.sign(identity, content);
        var p = signed.parameters();
        for (var altered : List.of(
                new RequestSignatureParameters(
                        p.appId(), p.audience(), p.keyVersion(), p.algorithm(), p.timestamp() + 1, p.nonce()),
                new RequestSignatureParameters(
                        p.appId(),
                        p.audience(),
                        p.keyVersion(),
                        p.algorithm(),
                        p.timestamp(),
                        "zyxwvutsrqponmlkjihgfe"))) {
            assertThatThrownBy(
                            () -> verifier.verify(identity, content, new RequestSignature(altered, signed.signature())))
                    .hasMessage("Request signature rejected: INVALID_SIGNATURE");
        }
        assertThat(claims).isEmpty();
    }

    @Test
    void bindsAudienceAppKeyVersionAndAlgorithmToTrustedPolicy() {
        var signed = signer.sign(identity, content);
        for (var wrong : List.of(
                new RequestSigningIdentity("another", identity.audience(), identity.key(), identity.algorithm()),
                new RequestSigningIdentity(identity.appId(), "different-api", identity.key(), identity.algorithm()),
                new RequestSigningIdentity(
                        identity.appId(), identity.audience(), new KmsKeyRef("merchant-key", 2), identity.algorithm()),
                new RequestSigningIdentity(
                        identity.appId(), identity.audience(), identity.key(), KmsSignatureAlgorithm.RSA_SHA256_PSS))) {
            assertThatThrownBy(() -> verifier.verify(wrong, content, signed))
                    .hasMessage("Request signature rejected: IDENTITY_MISMATCH");
        }
        assertThat(claims).isEmpty();
    }

    @Test
    void enforcesExpiryFutureSkewAndFullFutureDatedReplayLifetime() {
        var signed = signer.sign(identity, content);
        clock.now = clock.now.minusSeconds(30);
        verifier.verify(identity, content, signed);
        assertThat(claims.values()).containsExactly(Duration.ofSeconds(360));
        clock.now = clock.now.minusSeconds(1);
        assertThatThrownBy(() -> verifier.verify(identity, content, signed))
                .hasMessage("Request signature rejected: FUTURE_TIMESTAMP");
        clock.now = Instant.ofEpochSecond(signed.parameters().timestamp() + 300);
        assertThatThrownBy(() -> verifier.verify(identity, content, signed))
                .hasMessage("Request signature rejected: EXPIRED");
    }

    @Test
    void retainsClaimsForALaggingNodeAndRechecksTimeAfterSlowKmsVerification() {
        var signed = signer.sign(identity, content);
        clock.now = Instant.ofEpochSecond(signed.parameters().timestamp() + 299);
        verifier.verify(identity, content, signed);
        assertThat(claims.values()).containsExactly(Duration.ofSeconds(31));
        clock.now = clock.now.minusSeconds(30);
        assertThatThrownBy(() -> verifier.verify(identity, content, signed))
                .hasMessage("Request signature rejected: REPLAY");
        claims.clear();
        when(kms.verify(any(), any(), any())).thenAnswer(invocation -> {
            clock.now = Instant.ofEpochSecond(signed.parameters().timestamp() + 300);
            return true;
        });
        assertThatThrownBy(() -> verifier.verify(identity, content, signed))
                .hasMessage("Request signature rejected: EXPIRED");
        assertThat(claims).isEmpty();
    }

    @Test
    void failsClosedWhenStoreOrKmsIsUnavailable() {
        var signed = signer.sign(identity, content);
        var failing = new RequestVerifier(
                kms,
                (key, ttl) -> {
                    throw new IllegalStateException("secret-backend-detail");
                },
                clock,
                properties);
        assertThatThrownBy(() -> failing.verify(identity, content, signed))
                .hasMessage("Request signature rejected: REPLAY_STORE_UNAVAILABLE")
                .hasNoCause();
        when(kms.verify(any(), any(), any()))
                .thenThrow(new io.github.loadup.components.kms.KmsException(
                        io.github.loadup.components.kms.KmsException.Code.UNAVAILABLE));
        assertThatThrownBy(() -> verifier.verify(identity, content, signed))
                .hasMessage("KMS operation failed: UNAVAILABLE");
        assertThat(claims).isEmpty();
    }

    @Test
    void permitsOnlyOneConcurrentVerificationOfTheSameNonce() throws Exception {
        var signed = signer.sign(identity, content);
        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            List<Callable<Boolean>> tasks = new ArrayList<>();
            for (int i = 0; i < 12; i++)
                tasks.add(() -> {
                    try {
                        verifier.verify(identity, content, signed);
                        return true;
                    } catch (RequestSignatureException rejected) {
                        assertThat(rejected.getCode()).isEqualTo(RequestSignatureException.Code.REPLAY);
                        return false;
                    }
                });
            int accepted = 0;
            for (var result : executor.invokeAll(tasks)) if (result.get()) accepted++;
            assertThat(accepted).isEqualTo(1);
        }
    }

    @Test
    void supportsPssAndRejectsOversizeBodiesBeforeRemoteCalls() {
        var pss = new RequestSigningIdentity(
                identity.appId(), identity.audience(), identity.key(), KmsSignatureAlgorithm.RSA_SHA256_PSS);
        var signed = signer.sign(pss, content);
        verifier.verify(pss, content, signed);
        var unused = mock(KmsTemplate.class);
        var small = new RequestSigner(unused, clock, new RequestSignatureProperties(null, null, 1));
        assertThatThrownBy(() -> small.sign(identity, content))
                .hasMessage("Request signature rejected: PAYLOAD_TOO_LARGE");
        verifyNoInteractions(unused);
    }

    @Test
    void rejectsDuplicateMalformedAndAmbiguousHeadersAndTargets() {
        var signed = signer.sign(identity, content);
        var duplicate = headers(signed);
        duplicate.put("x-nonce", List.of(signed.parameters().nonce()));
        assertThatThrownBy(() -> RequestSignature.fromHeaders(duplicate))
                .hasMessage("Request signature rejected: MALFORMED");
        var multiple = headers(signed);
        multiple.put("X-Timestamp", List.of("1700000000", "1700000000"));
        assertThatThrownBy(() -> RequestSignature.fromHeaders(multiple))
                .hasMessage("Request signature rejected: MALFORMED");
        for (String target : List.of(
                "https://evil.test/api/pay",
                "//evil.test/path",
                "/api/pay#fragment",
                "/api/pay\nnonce:forged",
                "/api/pay?x=%xx")) {
            assertThatThrownBy(() -> new RequestContent("POST", target, "application/json", new byte[0]))
                    .hasMessage("Request signature rejected: MALFORMED");
        }
        var huge = headers(signed);
        huge.put("X-Key-Version", List.of("9999999999"));
        assertThatThrownBy(() -> RequestSignature.fromHeaders(huge))
                .hasMessage("Request signature rejected: MALFORMED");
    }

    private static Map<String, List<String>> headers(RequestSignature signature) {
        Map<String, List<String>> headers = new HashMap<>();
        signature.toHeaders().forEach((name, value) -> headers.put(name, List.of(value)));
        return headers;
    }

    private static Signature signature(KmsSignatureAlgorithm algorithm) throws Exception {
        var signature = Signature.getInstance(
                algorithm == KmsSignatureAlgorithm.RSA_SHA256_PSS ? "RSASSA-PSS" : "SHA256withRSA");
        if (algorithm == KmsSignatureAlgorithm.RSA_SHA256_PSS)
            signature.setParameter(new PSSParameterSpec("SHA-256", "MGF1", MGF1ParameterSpec.SHA256, 32, 1));
        return signature;
    }

    private static final class MutableClock extends Clock {
        private Instant now;

        MutableClock(Instant now) {
            this.now = now;
        }

        @Override
        public ZoneId getZone() {
            return ZoneOffset.UTC;
        }

        @Override
        public Clock withZone(ZoneId zone) {
            return this;
        }

        @Override
        public Instant instant() {
            return now;
        }
    }
}
