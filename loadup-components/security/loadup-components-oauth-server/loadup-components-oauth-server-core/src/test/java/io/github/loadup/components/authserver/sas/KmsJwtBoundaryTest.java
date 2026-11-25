/*
 * Copyright (C) 2026 LoadUp Cloud
 *
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
 */
package io.github.loadup.components.authserver.sas;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jwt.SignedJWT;
import io.github.loadup.components.authserver.config.TestKeyStores;
import io.github.loadup.components.authserver.properties.LoadUpAuthServerProperties.Signing;
import io.github.loadup.components.authserver.properties.LoadUpAuthServerProperties.SigningKey;
import io.github.loadup.components.kms.KmsKeyMetadata;
import io.github.loadup.components.kms.KmsKeyRef;
import io.github.loadup.components.kms.KmsSignature;
import io.github.loadup.components.kms.KmsSignatureAlgorithm;
import io.github.loadup.components.kms.KmsTemplate;
import io.github.loadup.components.kms.LocalKmsTemplate;
import java.math.BigInteger;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyFactory;
import java.security.KeyStore;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.RSAPublicKeySpec;
import java.time.Instant;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwtEncodingException;

class KmsJwtBoundaryTest {
    private static final KmsKeyRef ACTIVE = new KmsKeyRef("auth-current", 1);
    private static final String KID = "auth-current-v1";
    private static final Instant ISSUED_AT = Instant.parse("2026-01-01T00:00:00Z");

    @TempDir
    static Path directory;

    private static LocalKmsTemplate local;
    private static String publicKey;
    private static RSAPublicKey rsa;

    @BeforeAll
    static void setUp() throws Exception {
        var path = TestKeyStores.create(directory, ACTIVE.name());
        var store = KeyStore.getInstance("PKCS12");
        try (var input = Files.newInputStream(path)) {
            store.load(input, TestKeyStores.PASSWORD.toCharArray());
        }
        local = new LocalKmsTemplate(store, TestKeyStores.PASSWORD.toCharArray());
        publicKey = local.publicKey(ACTIVE);
        rsa = (RSAPublicKey) store.getCertificate(ACTIVE.name()).getPublicKey();
    }

    @Test
    void missingSigningConfigurationAndUnpinnedRetainedReferencesAreRejected() {
        var kms = kms();
        assertThatThrownBy(() -> new KmsSigningKeys(kms, null)).isInstanceOf(IllegalArgumentException.class);
        var noRetainedKeys = signing();
        noRetainedKeys.setVerificationKeys(null);
        assertThatThrownBy(() -> new KmsSigningKeys(kms, noRetainedKeys)).isInstanceOf(IllegalArgumentException.class);
        var noActiveKey = signing();
        noActiveKey.setActiveKey(null);
        assertThatThrownBy(() -> new KmsSigningKeys(kms, noActiveKey)).isInstanceOf(IllegalArgumentException.class);

        var retained = signing();
        var invalidKey = new SigningKey();
        invalidKey.setName("old");
        invalidKey.setVersion(-1);
        retained.setVerificationKeys(List.of(invalidKey));
        assertThatThrownBy(() -> new KmsSigningKeys(kms, retained))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Authorization keys require a name and a non-negative version");
    }

    @Test
    void defaultActiveVersionFollowsTheAliasLatestVersion() {
        var kms = local;
        var signing = signing();
        signing.getActiveKey().setVersion(0);

        var keys = new KmsSigningKeys(kms, signing);

        assertThat(keys.active().actual()).isEqualTo(ACTIVE);
        assertThat(keys.publicKeys().getKeys())
                .extracting("keyID")
                .anyMatch(id -> id.toString().startsWith("auth-current."));
    }

    @Test
    void metadataMustMatchTheActiveNameCapabilityAndPinnedVersion() {
        for (var metadata : List.of(
                new KmsKeyMetadata("other", "rsa", 1, 1, 1, false, false, true, Map.of()),
                new KmsKeyMetadata(ACTIVE.name(), "rsa", 1, 1, 1, false, false, false, Map.of()),
                new KmsKeyMetadata(ACTIVE.name(), "rsa", 0, 1, 1, false, false, true, Map.of()))) {
            var kms = kms();
            when(kms.metadata(ACTIVE.name())).thenReturn(metadata);
            assertThatThrownBy(() -> new KmsSigningKeys(kms, signing()))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Authorization active key must support signing at the pinned version");
            verify(kms, never()).publicKey(any(KmsKeyRef.class));
        }
    }

    @Test
    void publicKeysBelowAndAboveTheSupportedRsaSizeAreRejected() throws Exception {
        for (int bits : new int[] {1024, 4097}) {
            var modulus = BigInteger.ONE.shiftLeft(bits - 1).add(BigInteger.valueOf(17));
            var key = KeyFactory.getInstance("RSA")
                    .generatePublic(new RSAPublicKeySpec(modulus, BigInteger.valueOf(65537)));
            var pem = "-----BEGIN PUBLIC KEY-----\n"
                    + Base64.getMimeEncoder(64, new byte[] {'\n'}).encodeToString(key.getEncoded())
                    + "\n-----END PUBLIC KEY-----\n";
            var kms = kms();
            when(kms.publicKey(ACTIVE)).thenReturn(pem);
            assertThatThrownBy(() -> new KmsSigningKeys(kms, signing()))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Authorization signing requires RSA-2048 through RSA-4096");
        }
    }

    @Test
    void matchingKeyIdAndUriHeadersProduceVerifiableTokensWithNumericDates() throws Exception {
        var kms = signingKms();
        var encoder = new KmsJwtEncoder(kms, new KmsSigningKeys(kms, signing()));
        var header = JwsHeader.with(SignatureAlgorithm.RS256)
                .keyId(KID)
                .header("jku", URI.create("https://issuer.example.test/jwks"))
                .header("x5u", URI.create("https://issuer.example.test/cert"))
                .build();
        var jwt = encoder.encode(JwtEncoderParameters.from(header, claims()));
        assertThat(jwt.getHeaders())
                .containsEntry("kid", KID)
                .containsEntry("jku", "https://issuer.example.test/jwks")
                .containsEntry("x5u", "https://issuer.example.test/cert");
        var parsed = SignedJWT.parse(jwt.getTokenValue());
        assertThat(parsed.verify(new RSASSAVerifier(rsa))).isTrue();
        assertThat(parsed.getJWTClaimsSet().getIssueTime().toInstant()).isEqualTo(ISSUED_AT);
        assertThat(parsed.getJWTClaimsSet().getNotBeforeTime().toInstant()).isEqualTo(ISSUED_AT);
        assertThat(parsed.getJWTClaimsSet().getExpirationTime().toInstant()).isEqualTo(ISSUED_AT.plusSeconds(60));
    }

    @Test
    void rotatedSigningKeyKeepsHistoricalTokensVerifiableAcrossRestart() throws Exception {
        var oldDirectory = Files.createDirectories(directory.resolve("old"));
        var newDirectory = Files.createDirectories(directory.resolve("new"));
        var oldStore = TestKeyStores.load(TestKeyStores.create(oldDirectory, "auth-current"));
        var newStorePath = TestKeyStores.create(newDirectory, "auth-current");
        var newStore = TestKeyStores.load(newStorePath);
        var oldKms = new LocalKmsTemplate(oldStore, TestKeyStores.PASSWORD.toCharArray());
        var oldKeys = new KmsSigningKeys(oldKms, signing("auth-current", 0));
        var oldEncoder = new KmsJwtEncoder(oldKms, oldKeys);
        var oldToken = SignedJWT.parse(
                oldEncoder.encode(JwtEncoderParameters.from(claims())).getTokenValue());
        var oldPublicKey =
                (RSAPublicKey) oldStore.getCertificate("auth-current").getPublicKey();
        assertThat(oldToken.verify(new RSASSAVerifier(oldPublicKey))).isTrue();

        var replacement = new LocalKmsTemplate(newStore, TestKeyStores.PASSWORD.toCharArray());
        var replacementEncoder =
                new KmsJwtEncoder(replacement, new KmsSigningKeys(replacement, signing("auth-current", 0)));
        var newToken = SignedJWT.parse(
                replacementEncoder.encode(JwtEncoderParameters.from(claims())).getTokenValue());
        var newPublicKey =
                (RSAPublicKey) newStore.getCertificate("auth-current").getPublicKey();

        assertThat(newToken.verify(new RSASSAVerifier(newPublicKey))).isTrue();
        newStore.setCertificateEntry("auth-current.history.old", oldStore.getCertificate("auth-current"));
        try (var output = Files.newOutputStream(newStorePath)) {
            newStore.store(output, TestKeyStores.PASSWORD.toCharArray());
        }
        var reloadedStore = TestKeyStores.load(newStorePath);
        var restartedKms = new LocalKmsTemplate(reloadedStore, TestKeyStores.PASSWORD.toCharArray());
        var restartedKeys = new KmsSigningKeys(restartedKms, signing("auth-current", 0));
        assertThat(restartedKeys.publicKeys().getKeys()).hasSize(2);
        var retainedOldKey = (RSAKey)
                restartedKeys.publicKeys().getKeyByKeyId(oldToken.getHeader().getKeyID());
        assertThat(oldToken.verify(new RSASSAVerifier(retainedOldKey.toRSAPublicKey())))
                .isTrue();
        var retainedNewKey = (RSAKey)
                restartedKeys.publicKeys().getKeyByKeyId(newToken.getHeader().getKeyID());
        assertThat(newToken.verify(new RSASSAVerifier(retainedNewKey.toRSAPublicKey())))
                .isTrue();
        var segments = newToken.serialize().split("\\.");
        segments[2] = (segments[2].charAt(0) == 'A' ? "B" : "A") + segments[2].substring(1);
        var tampered = SignedJWT.parse(String.join(".", segments));
        assertThat(tampered.verify(new RSASSAVerifier(retainedNewKey.toRSAPublicKey())))
                .isFalse();
    }

    @Test
    void criticalAndUnencodedPayloadHeadersFailBeforeKmsSigning() {
        var kms = signingKms();
        var encoder = new KmsJwtEncoder(kms, new KmsSigningKeys(kms, signing()));
        for (var header : List.of(
                JwsHeader.with(SignatureAlgorithm.RS256)
                        .header("crit", List.of("custom"))
                        .header("custom", true)
                        .build(),
                JwsHeader.with(SignatureAlgorithm.RS256)
                        .header("b64", false)
                        .header("crit", List.of("b64"))
                        .build())) {
            assertThatThrownBy(() -> encoder.encode(JwtEncoderParameters.from(header, claims())))
                    .isInstanceOf(JwtEncodingException.class)
                    .hasMessage("KMS authorization token signing failed")
                    .hasNoCause();
        }
        verify(kms, never()).sign(eq(ACTIVE), eq(KmsSignatureAlgorithm.RSA_SHA256_PKCS1), any(byte[].class));
    }

    @Test
    void mismatchedSignatureVersionAndAlgorithmCannotBeUsedForTokens() {
        for (var signature : List.of(
                new KmsSignature(ACTIVE.name(), 2, KmsSignatureAlgorithm.RSA_SHA256_PKCS1, new byte[] {1}),
                new KmsSignature(ACTIVE.name(), 1, KmsSignatureAlgorithm.RSA_SHA256_PSS, new byte[] {1}))) {
            var kms = kms();
            when(kms.sign(eq(ACTIVE), eq(KmsSignatureAlgorithm.RSA_SHA256_PKCS1), any(byte[].class)))
                    .thenReturn(signature);
            var encoder = new KmsJwtEncoder(kms, new KmsSigningKeys(kms, signing()));
            assertThatThrownBy(() -> encoder.encode(JwtEncoderParameters.from(claims())))
                    .isInstanceOf(JwtEncodingException.class)
                    .hasMessage("KMS authorization token signing failed")
                    .hasNoCause();
            verify(kms).sign(eq(ACTIVE), eq(KmsSignatureAlgorithm.RSA_SHA256_PKCS1), any(byte[].class));
        }
    }

    private static Signing signing() {
        return signing(ACTIVE.name(), ACTIVE.version());
    }

    private static Signing signing(String alias, int version) {
        var active = new SigningKey();
        active.setName(alias);
        active.setVersion(version);
        var signing = new Signing();
        signing.setActiveKey(active);
        return signing;
    }

    private static JwtClaimsSet claims() {
        return JwtClaimsSet.builder()
                .subject("client")
                .issuedAt(ISSUED_AT)
                .notBefore(ISSUED_AT)
                .expiresAt(ISSUED_AT.plusSeconds(60))
                .build();
    }

    private static KmsTemplate kms() {
        var kms = mock(KmsTemplate.class);
        when(kms.metadata(ACTIVE.name())).thenReturn(local.metadata(ACTIVE.name()));
        when(kms.publicKey(ACTIVE)).thenReturn(publicKey);
        return kms;
    }

    private static KmsTemplate signingKms() {
        var kms = kms();
        when(kms.sign(eq(ACTIVE), eq(KmsSignatureAlgorithm.RSA_SHA256_PKCS1), any(byte[].class)))
                .thenAnswer(invocation ->
                        local.sign(ACTIVE, KmsSignatureAlgorithm.RSA_SHA256_PKCS1, invocation.getArgument(2)));
        return kms;
    }
}
