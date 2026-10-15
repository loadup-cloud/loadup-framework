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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import io.github.loadup.components.kms.KmsKeyRef;
import io.github.loadup.components.kms.KmsSignature;
import io.github.loadup.components.kms.KmsSignatureAlgorithm;
import io.github.loadup.components.kms.KmsTemplate;
import io.github.loadup.components.signature.config.SignatureAutoConfiguration;
import io.github.loadup.components.signature.request.RequestContent;
import io.github.loadup.components.signature.request.RequestNonceStore;
import io.github.loadup.components.signature.request.RequestSignatureAutoConfiguration;
import io.github.loadup.components.signature.request.RequestSignatureProperties;
import io.github.loadup.components.signature.request.RequestSigner;
import io.github.loadup.components.signature.request.RequestSigningIdentity;
import io.github.loadup.components.signature.request.RequestVerifier;
import io.github.loadup.components.signature.service.DigestService;
import io.github.loadup.components.signature.service.KeyPairService;
import io.github.loadup.components.signature.service.SignatureService;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

class SignatureAssemblyContractTest {
    private final ApplicationContextRunner contexts = new ApplicationContextRunner()
            .withConfiguration(
                    AutoConfigurations.of(SignatureAutoConfiguration.class, RequestSignatureAutoConfiguration.class));

    @Test
    void disabledComponentRemovesLocalAndRequestServices() {
        contexts.withPropertyValues(
                        "loadup.components.signature.enabled=false", "loadup.components.signature.request.enabled=true")
                .run(context -> assertThat(context)
                        .doesNotHaveBean(KeyPairService.class)
                        .doesNotHaveBean(SignatureService.class)
                        .doesNotHaveBean(DigestService.class)
                        .doesNotHaveBean(RequestSigner.class)
                        .doesNotHaveBean(RequestVerifier.class));
    }

    @Test
    void consumerLocalServicesReplaceAllDefaultServices() {
        var keys = mock(KeyPairService.class);
        var signatures = mock(SignatureService.class);
        var digests = mock(DigestService.class);
        contexts.withBean(KeyPairService.class, () -> keys)
                .withBean(SignatureService.class, () -> signatures)
                .withBean(DigestService.class, () -> digests)
                .run(context -> {
                    assertThat(context.getBean(KeyPairService.class)).isSameAs(keys);
                    assertThat(context.getBean(SignatureService.class)).isSameAs(signatures);
                    assertThat(context.getBean(DigestService.class)).isSameAs(digests);
                });
    }

    @Test
    void explicitClockAndBoundRequestLimitsReachSigner() {
        var kms = mock(KmsTemplate.class);
        when(kms.sign(any(), any(), any()))
                .thenReturn(new KmsSignature("key", 3, KmsSignatureAlgorithm.RSA_SHA256_PKCS1, new byte[256]));
        var clock = Clock.fixed(Instant.ofEpochSecond(1800000000), ZoneId.systemDefault());
        contexts.withPropertyValues(
                        "loadup.components.signature.request.enabled=true",
                        "loadup.components.signature.request.verification-enabled=false",
                        "loadup.components.signature.request.max-age=10s",
                        "loadup.components.signature.request.future-skew=1s",
                        "loadup.components.signature.request.max-payload-bytes=16")
                .withBean(KmsTemplate.class, () -> kms)
                .withBean(Clock.class, () -> clock)
                .run(context -> {
                    assertThat(context).hasSingleBean(RequestSigner.class).doesNotHaveBean(RequestVerifier.class);
                    var policy = context.getBean(RequestSignatureProperties.class);
                    assertThat(policy.maxAge()).isEqualTo(Duration.ofSeconds(10));
                    assertThat(policy.futureSkew()).isEqualTo(Duration.ofSeconds(1));
                    assertThat(policy.maxPayloadBytes()).isEqualTo(16);
                    var signed = context.getBean(RequestSigner.class)
                            .sign(
                                    new RequestSigningIdentity(
                                            "app",
                                            "api",
                                            new KmsKeyRef("key", 3),
                                            KmsSignatureAlgorithm.RSA_SHA256_PKCS1),
                                    new RequestContent("GET", "/api/balance", null, new byte[0]));
                    assertThat(signed.parameters().timestamp()).isEqualTo(1800000000);
                });
    }

    @Test
    void consumerRequestServicesAreRetainedWithoutTheirDependencies() {
        var signer = mock(RequestSigner.class);
        var verifier = mock(RequestVerifier.class);
        contexts.withPropertyValues("loadup.components.signature.request.enabled=true")
                .withBean(RequestSigner.class, () -> signer)
                .withBean(RequestVerifier.class, () -> verifier)
                .run(context -> {
                    assertThat(context.getBean(RequestSigner.class)).isSameAs(signer);
                    assertThat(context.getBean(RequestVerifier.class)).isSameAs(verifier);
                    assertThat(context).doesNotHaveBean(RequestNonceStore.class);
                });
    }

    @Test
    void invalidConfiguredResourceLimitsFailStartup() {
        contexts.withPropertyValues(
                        "loadup.components.signature.request.enabled=true",
                        "loadup.components.signature.request.verification-enabled=false",
                        "loadup.components.signature.request.max-payload-bytes=0")
                .withBean(KmsTemplate.class, () -> mock(KmsTemplate.class))
                .run(context -> assertThat(context).hasFailed());
    }
}
