/*
 * Copyright (C) 2025-2026 LoadUp Cloud
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
package io.github.loadup.components.kms;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.Signature;
import java.security.spec.MGF1ParameterSpec;
import java.security.spec.PSSParameterSpec;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

class OpenBaoKmsTest {
    private final JsonMapper mapper = JsonMapper.builder().build();
    private final List<Request> requests = new CopyOnWriteArrayList<>();
    private final AtomicReference<String> token = new AtomicReference<>("runtime-token");
    private HttpServer server;
    private OpenBaoKmsTemplate kms;
    private OpenBaoKeyManager manager;
    private KeyPair keyPair;
    private KeyPair rotatedPair;
    private KmsProperties config;
    private int minVerificationVersion = 1;
    private int latestVersion = 1;
    private int errorStatus;
    private boolean malformedVerification;
    private String forcedPath;
    private Object forcedResponse;
    private String rawResponse;
    private final Map<String, String> secretValues = new HashMap<>();
    private final Map<String, Integer> secretVersions = new HashMap<>();
    private final AtomicReference<String> concurrentSecretValue = new AtomicReference<>();

    @BeforeEach
    void start() throws Exception {
        var generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(2048);
        keyPair = generator.generateKeyPair();
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/", this::respond);
        server.start();
        config = new KmsProperties(
                URI.create("http://127.0.0.1:" + server.getAddress().getPort() + "/"),
                "payments/transit",
                "tenant",
                null,
                null,
                true,
                null,
                null,
                null,
                1024);
        kms = new OpenBaoKmsTemplate(
                new OpenBaoClient(config, token::get, false, RestClient::builder, mapper, null, null));
        manager = new OpenBaoKeyManager(
                new OpenBaoClient(config, () -> "management-token", true, RestClient::builder, mapper, null, null));
    }

    @AfterEach
    void stop() {
        if (kms != null) {
            kms.close();
        }
        if (manager != null) {
            manager.close();
        }
        if (server != null) {
            server.stop(0);
        }
    }

    @Test
    void separatesIdentitiesAndPinsLatestVersionWhileKeepingOldSignaturesVerifiable() throws Exception {
        manager.create("merchant", KmsKeyType.RSA_2048);
        assertThat(requests.getFirst().token()).isEqualTo("management-token");
        assertThat(requests.getFirst().body().path("exportable").asBoolean()).isFalse();
        assertThat(requests.getFirst().body().path("allow_plaintext_backup").asBoolean())
                .isFalse();
        byte[] message = "order=123&amount=100".getBytes(StandardCharsets.UTF_8);
        KmsSignature signature =
                kms.sign(KmsKeyRef.latest("merchant"), KmsSignatureAlgorithm.RSA_SHA256_PKCS1, message);
        assertThat(signature.version()).isEqualTo(1);
        var local = Signature.getInstance("SHA256withRSA");
        local.initVerify(keyPair.getPublic());
        local.update(message);
        assertThat(local.verify(signature.bytes())).isTrue();
        manager.rotate("merchant");
        assertThat(kms.metadata("merchant").latestVersion()).isEqualTo(2);
        assertThat(kms.verify(KmsKeyRef.latest("merchant"), signature, message)).isTrue();
        assertThat(kms.verify(KmsKeyRef.latest("merchant"), signature, new byte[] {1}))
                .isFalse();
        assertThat(kms.publicKey(new KmsKeyRef("merchant", 1))).contains("BEGIN PUBLIC KEY");
        assertThat(requests).allSatisfy(request -> {
            assertThat(request.path()).startsWith("/v1/payments/transit/");
            assertThat(request.namespace()).isEqualTo("tenant");
        });
        assertThat(requests.stream()
                        .filter(request -> request.path().contains("/sign/"))
                        .findFirst()
                        .orElseThrow()
                        .body()
                        .path("key_version")
                        .asInt())
                .isEqualTo(1);
    }

    @Test
    void freshBinderVerifiesHistoricalFingerprintsWithinTransitsAllowedVersions() {
        byte[] payload = {7, 8, 9};
        var alias = KmsKeyRef.latest("merchant");
        var old = kms.currentSigningKey(alias.name());
        var signature = kms.sign(alias, KmsSignatureAlgorithm.RSA_SHA256_PKCS1, payload);
        manager.rotate(alias.name());
        try (var restarted = new OpenBaoKmsTemplate(
                new OpenBaoClient(config, token::get, false, RestClient::builder, mapper, null, null))) {
            assertThat(restarted.currentSigningKey(alias.name()).fingerprint()).isNotEqualTo(old.fingerprint());
            assertThat(restarted.verify(
                            alias.name(), old.fingerprint(), signature.algorithm(), payload, signature.bytes()))
                    .isTrue();
            minVerificationVersion = 2;
            assertThat(restarted.verify(
                            alias.name(), old.fingerprint(), signature.algorithm(), payload, signature.bytes()))
                    .isFalse();
        }
    }

    @Test
    void usesSha256PssWithHashLengthSaltAndDefensiveSignatureCopies() throws Exception {
        byte[] message = "payment".getBytes(StandardCharsets.UTF_8);
        var signature = kms.sign(KmsKeyRef.latest("merchant"), KmsSignatureAlgorithm.RSA_SHA256_PSS, message);
        var local = Signature.getInstance("RSASSA-PSS");
        local.setParameter(new PSSParameterSpec("SHA-256", "MGF1", MGF1ParameterSpec.SHA256, 32, 1));
        local.initVerify(keyPair.getPublic());
        local.update(message);
        assertThat(local.verify(signature.bytes())).isTrue();
        byte[] copy = signature.bytes();
        copy[0] ^= 1;
        assertThat(kms.verify(KmsKeyRef.latest("merchant"), signature, message)).isTrue();
    }

    @Test
    void preservesCiphertextEnvelopeAndEmptyPlaintext() {
        var ciphertext = kms.encrypt(KmsKeyRef.latest("merchant"), new byte[] {1, 2, 3});
        assertThat(ciphertext.value()).isEqualTo("vault:v1:AQID");
        assertThat(kms.decrypt(KmsKeyRef.latest("merchant"), ciphertext)).containsExactly(1, 2, 3);
        assertThat(kms.decrypt(KmsKeyRef.latest("merchant"), new KmsCiphertext("merchant", 1, "vault:v1:AA==")))
                .isEmpty();
    }

    @Test
    void rejectsMismatchedKeysAndOversizedInputBeforeMakingRequests() {
        var signature = new KmsSignature("other", 1, KmsSignatureAlgorithm.RSA_SHA256_PKCS1, new byte[] {1});
        assertThatThrownBy(() -> kms.verify(KmsKeyRef.latest("merchant"), signature, new byte[] {1}))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> kms.encrypt(KmsKeyRef.latest("merchant"), new byte[1025]))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() ->
                        kms.decrypt(new KmsKeyRef("merchant", 2), new KmsCiphertext("merchant", 1, "vault:v1:AA==")))
                .isInstanceOf(IllegalArgumentException.class);
        assertThat(requests).isEmpty();
    }

    @Test
    void rereadsCredentialsAndSanitizesErrorsWithoutRetry() {
        kms.metadata("merchant");
        token.set("renewed-token");
        kms.metadata("merchant");
        assertThat(requests.getLast().token()).isEqualTo("renewed-token");
        errorStatus = 503;
        int before = requests.size();
        assertThatThrownBy(() -> kms.metadata("merchant"))
                .isInstanceOf(KmsException.class)
                .hasMessage("KMS operation failed: UNAVAILABLE")
                .hasNoCause();
        assertThat(requests).hasSize(before + 1);
        errorStatus = 403;
        assertThatThrownBy(() -> manager.rotate("merchant")).hasMessage("KMS operation failed: ACCESS_DENIED");
    }

    @Test
    void treatsMissingVerificationResultAsProtocolFailure() {
        malformedVerification = true;
        assertThatThrownBy(() -> kms.verify(
                        KmsKeyRef.latest("merchant"),
                        new KmsSignature("merchant", 1, KmsSignatureAlgorithm.RSA_SHA256_PKCS1, new byte[] {1}),
                        new byte[] {1}))
                .hasMessage("KMS operation failed: INVALID_RESPONSE");
    }

    @Test
    void importsOnlyWrappedPrivateMaterialAndDisablesImportedKeyRotation() {
        manager.importWrappedKey("merchant", KmsKeyType.RSA_2048, new byte[] {1, 2, 3});
        var body = requests.getLast().body();
        assertThat(body.path("ciphertext").asString()).isEqualTo("AQID");
        assertThat(body.path("allow_rotation").asBoolean()).isFalse();
        assertThat(body.path("hash_function").asString()).isEqualTo("SHA256");
        manager.setMinimumVersions("merchant", 2, 1);
        assertThat(requests.getLast().body().path("min_encryption_version").asInt())
                .isEqualTo(2);
        assertThat(manager.wrappingPublicKey()).contains("BEGIN PUBLIC KEY");
    }

    @Test
    void classifiesRemoteStatusesWithoutLeakingErrorBodiesOrRetrying() {
        Map<Integer, KmsException.Code> classifications = Map.of(
                401,
                KmsException.Code.AUTHENTICATION,
                403,
                KmsException.Code.ACCESS_DENIED,
                404,
                KmsException.Code.NOT_FOUND,
                429,
                KmsException.Code.UNAVAILABLE,
                500,
                KmsException.Code.UNAVAILABLE,
                502,
                KmsException.Code.UNAVAILABLE,
                503,
                KmsException.Code.UNAVAILABLE,
                504,
                KmsException.Code.UNAVAILABLE,
                400,
                KmsException.Code.INVALID_REQUEST);
        classifications.forEach((status, code) -> {
            errorStatus = status;
            int before = requests.size();
            assertThatThrownBy(() -> kms.metadata("merchant")).isInstanceOfSatisfying(KmsException.class, failure -> {
                assertThat(failure.getCode()).isEqualTo(code);
                assertThat(failure.getMessage()).doesNotContain("secret-plaintext-token");
                assertThat(failure.getCause()).isNull();
            });
            assertThat(requests).hasSize(before + 1);
        });
    }

    @Test
    void malformedJsonOrMissingDataIsAnInvalidResponse() {
        rawResponse = "not-json-secret";
        assertThatThrownBy(() -> kms.metadata("merchant")).hasMessage("KMS operation failed: INVALID_RESPONSE");
        rawResponse = null;
        forcedPath = "/keys/";
        for (Object response : List.of(Map.of(), Map.of("data", "not-an-object"), Map.of("data", List.of()))) {
            forcedResponse = response;
            assertThatThrownBy(() -> kms.metadata("merchant")).hasMessage("KMS operation failed: INVALID_RESPONSE");
        }
    }

    @Test
    void rejectsInvalidMetadataNameVersionsTypesAndPublicKeyIndexes() {
        forcedPath = "/keys/";
        for (Map.Entry<String, Object> invalid : Map.<String, Object>of(
                        "name", "other-key", "latest_version", "1", "keys", "not-an-object", "type", "")
                .entrySet()) {
            Map<String, Object> data = metadataData();
            data.put(invalid.getKey(), invalid.getValue());
            forcedResponse = Map.of("data", data);
            assertThatThrownBy(() -> kms.metadata("merchant")).hasMessage("KMS operation failed: INVALID_RESPONSE");
        }
        for (Object version : List.of(0, -1, 2147483648L)) {
            Map<String, Object> data = metadataData();
            data.put("latest_version", version);
            forcedResponse = Map.of("data", data);
            assertThatThrownBy(() -> kms.metadata("merchant")).hasMessage("KMS operation failed: INVALID_RESPONSE");
        }
        Map<String, Object> data = metadataData();
        data.put("keys", Map.of("not-a-version", Map.of("public_key", "synthetic PEM")));
        forcedResponse = Map.of("data", data);
        assertThatThrownBy(() -> kms.metadata("merchant")).hasMessage("KMS operation failed: INVALID_RESPONSE");
    }

    @Test
    void decryptionRejectsMissingNonTextInvalidBase64AndOversizedPlaintext() {
        forcedPath = "/decrypt/";
        KmsCiphertext ciphertext = new KmsCiphertext("merchant", 1, "vault:v1:AA==");
        for (Object plaintext : List.of(123, "not-base64!", Base64.getEncoder().encodeToString(new byte[1025]))) {
            forcedResponse = Map.of("data", Map.of("plaintext", plaintext));
            assertThatThrownBy(() -> kms.decrypt(KmsKeyRef.latest("merchant"), ciphertext))
                    .hasMessage("KMS operation failed: INVALID_RESPONSE");
        }
        forcedResponse = Map.of("data", Map.of());
        assertThatThrownBy(() -> kms.decrypt(KmsKeyRef.latest("merchant"), ciphertext))
                .hasMessage("KMS operation failed: INVALID_RESPONSE");
    }

    @Test
    void signatureRejectsBadEnvelopeBase64AndUnexpectedVersion() {
        forcedPath = "/sign/";
        for (String signature :
                List.of("", "not-an-envelope", "vault:v2:AA==", "vault:v2147483648:AA==", "vault:v1:AA=")) {
            forcedResponse = Map.of("data", Map.of("signature", signature));
            assertThatThrownBy(() -> kms.sign(
                            KmsKeyRef.latest("merchant"), KmsSignatureAlgorithm.RSA_SHA256_PKCS1, new byte[] {1}))
                    .hasMessage("KMS operation failed: INVALID_RESPONSE");
        }
        forcedResponse = Map.of("data", Map.of("signature", 123));
        assertThatThrownBy(() ->
                        kms.sign(KmsKeyRef.latest("merchant"), KmsSignatureAlgorithm.RSA_SHA256_PKCS1, new byte[] {1}))
                .hasMessage("KMS operation failed: INVALID_RESPONSE");
    }

    @Test
    void encryptionRequiresExpectedEnvelopeAndKeyCapabilities() {
        forcedPath = "/encrypt/";
        forcedResponse = Map.of("data", Map.of("ciphertext", "vault:v2:AA=="));
        assertThatThrownBy(() -> kms.encrypt(KmsKeyRef.latest("merchant"), new byte[] {1}))
                .hasMessage("KMS operation failed: INVALID_RESPONSE");
        forcedPath = "/keys/";
        Map<String, Object> data = metadataData();
        data.put("supports_encryption", false);
        forcedResponse = Map.of("data", data);
        assertThatThrownBy(() -> kms.encrypt(KmsKeyRef.latest("merchant"), new byte[] {1}))
                .isInstanceOf(IllegalArgumentException.class);
        data.put("supports_signing", false);
        assertThatThrownBy(() ->
                        kms.sign(KmsKeyRef.latest("merchant"), KmsSignatureAlgorithm.RSA_SHA256_PKCS1, new byte[] {1}))
                .isInstanceOf(IllegalArgumentException.class);
        data.put("type", "aes256-gcm96");
        data.put("supports_signing", true);
        assertThatThrownBy(() ->
                        kms.sign(KmsKeyRef.latest("merchant"), KmsSignatureAlgorithm.RSA_SHA256_PKCS1, new byte[] {1}))
                .isInstanceOf(IllegalArgumentException.class);
        data.put("keys", Map.of("1", Map.of()));
        assertThatThrownBy(() -> kms.publicKey(KmsKeyRef.latest("merchant")))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rejectsNullInputsMissingAlgorithmAndUnavailableVersions() {
        assertThatThrownBy(() -> kms.encrypt(null, new byte[0])).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> kms.encrypt(KmsKeyRef.latest("merchant"), null))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> kms.decrypt(KmsKeyRef.latest("merchant"), null))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> kms.verify(KmsKeyRef.latest("merchant"), null, new byte[0]))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> kms.sign(KmsKeyRef.latest("merchant"), null, new byte[0]))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> kms.publicKey(new KmsKeyRef("merchant", 2)))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void invalidCredentialsAreRejectedBeforeAnyHttpRequest() {
        for (String invalid : List.of("", " ", "token\r", "token\n", "t".repeat(8193))) {
            token.set(invalid);
            assertThatThrownBy(() -> kms.metadata("merchant")).hasMessage("KMS operation failed: AUTHENTICATION");
        }
        token.set(null);
        assertThatThrownBy(() -> kms.metadata("merchant")).hasMessage("KMS operation failed: AUTHENTICATION");
        assertThat(requests).isEmpty();
    }

    @Test
    void tokenProviderFailureIsSanitizedWithoutOpeningAConnection() {
        KmsProperties configuration = new KmsProperties(
                URI.create("http://127.0.0.1:" + server.getAddress().getPort()),
                "payments/transit",
                null,
                null,
                null,
                true,
                null,
                null,
                null,
                1024);
        try (OpenBaoKmsTemplate failing = new OpenBaoKmsTemplate(new OpenBaoClient(
                configuration,
                () -> {
                    throw new IllegalStateException("synthetic secret token failure");
                },
                false,
                RestClient::builder,
                mapper,
                null,
                null))) {
            assertThatThrownBy(() -> failing.metadata("merchant"))
                    .hasMessage("KMS operation failed: AUTHENTICATION")
                    .hasNoCause();
        }
        assertThat(requests).isEmpty();
    }

    @Test
    void managementValidatesImportsAndMinimumVersionsBeforeNetworkCalls() {
        assertThatThrownBy(() -> manager.create("merchant", null)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> manager.setMinimumVersions("merchant", -1, 1))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> manager.setMinimumVersions("merchant", 0, 0))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> manager.importWrappedKey("merchant", KmsKeyType.RSA_2048, new byte[0]))
                .isInstanceOf(IllegalArgumentException.class);
        for (String pem : List.of("not PEM", "-----BEGIN PUBLIC KEY-----" + "a".repeat(16384))) {
            assertThatThrownBy(() -> manager.importPublicKey("merchant", KmsKeyType.RSA_2048, pem))
                    .isInstanceOf(IllegalArgumentException.class);
        }
        assertThatThrownBy(() -> manager.importPublicKey("merchant", KmsKeyType.RSA_2048, null))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> manager.importPublicKey("merchant", KmsKeyType.AES_256_GCM, publicPem()))
                .isInstanceOf(IllegalArgumentException.class);
        assertThat(requests).isEmpty();
        manager.importPublicKey("merchant", KmsKeyType.RSA_2048, publicPem());
        assertThat(requests.getLast().body().path("public_key").asString()).isEqualTo(publicPem());
        assertThat(requests.getLast().body().path("allow_rotation").asBoolean()).isFalse();
    }

    @Test
    void keepsNamedValuesInKvV2AndReadsTheCurrentVersionFromANewManager() {
        try (var secrets = secretManager()) {
            assertThatThrownBy(() -> secrets.get("payments-token"))
                    .hasMessage("KMS operation failed: NOT_FOUND")
                    .hasNoCause();
            assertThatThrownBy(() -> secrets.update("payments-token", "absent"))
                    .hasMessage("KMS operation failed: NOT_FOUND");
            secrets.create("payments-token", "凭据初始值");
            assertThatThrownBy(() -> secrets.create("payments-token", "duplicate"))
                    .hasMessage("KMS operation failed: INVALID_REQUEST");
            assertThat(secrets.get("payments-token")).isEqualTo("凭据初始值");
            secrets.update("payments-token", "凭据更新值");
        }
        try (var secrets = secretManager()) {
            assertThat(secrets.get("payments-token")).isEqualTo("凭据更新值");
        }
        assertThat(requests)
                .extracting(Request::path)
                .contains("/v1/payments/secrets/data/payments-token", "/v1/payments/secrets/metadata/payments-token");
        assertThat(requests).allSatisfy(request -> {
            assertThat(request.token()).isEqualTo("runtime-token");
            assertThat(request.namespace()).isEqualTo("tenant");
        });
    }

    private OpenBaoKmsSecretManager secretManager() {
        return new OpenBaoKmsSecretManager(
                OpenBaoClient.secrets(config, "payments/secrets", token::get, RestClient::builder, mapper, null, null));
    }

    @Test
    void patchDoesNotOverwriteAConcurrentSecretUpdate() {
        try (var secrets = secretManager()) {
            secrets.create("payments-token", "original");
            concurrentSecretValue.set("concurrent");
            assertThatThrownBy(() -> secrets.update("payments-token", "stale-writer"))
                    .hasMessage("KMS operation failed: INVALID_REQUEST");
            assertThat(secrets.get("payments-token")).isEqualTo("concurrent");
        }
    }

    @Test
    void rejectsMalformedSecretDataAndDoesNotPatchWithoutAValidVersion() {
        try (var secrets = secretManager()) {
            forcedPath = "/data/payments-token";
            forcedResponse = Map.of("data", Map.of("data", Map.of("value", 123)));
            assertThatThrownBy(() -> secrets.get("payments-token"))
                    .hasMessage("KMS operation failed: INVALID_RESPONSE")
                    .hasNoCause();

            forcedPath = "/metadata/payments-token";
            forcedResponse = Map.of("data", Map.of("current_version", "2"));
            assertThatThrownBy(() -> secrets.update("payments-token", "synthetic-new-value"))
                    .hasMessage("KMS operation failed: INVALID_RESPONSE")
                    .hasNoCause();
        }
        assertThat(requests)
                .extracting(Request::path)
                .containsExactly(
                        "/v1/payments/secrets/data/payments-token", "/v1/payments/secrets/metadata/payments-token");
    }

    private Map<String, Object> metadataData() {
        return new HashMap<>(Map.of(
                "name",
                "merchant",
                "type",
                "rsa-2048",
                "latest_version",
                1,
                "supports_encryption",
                true,
                "supports_signing",
                true,
                "keys",
                Map.of("1", Map.of("public_key", publicPem()))));
    }

    private void respond(HttpExchange exchange) {
        try {
            String path = exchange.getRequestURI().getPath();
            byte[] input = exchange.getRequestBody().readAllBytes();
            JsonNode request = input.length == 0 ? mapper.createObjectNode() : mapper.readTree(input);
            requests.add(new Request(
                    path,
                    exchange.getRequestHeaders().getFirst("X-Vault-Token"),
                    exchange.getRequestHeaders().getFirst("X-Vault-Namespace"),
                    request));
            if (errorStatus != 0) {
                send(exchange, errorStatus, Map.of("errors", List.of("secret-plaintext-token")));
                return;
            }
            if (rawResponse != null) {
                byte[] response = rawResponse.getBytes(StandardCharsets.UTF_8);
                exchange.getResponseHeaders().set("Content-Type", "application/json");
                exchange.sendResponseHeaders(200, response.length);
                exchange.getResponseBody().write(response);
                exchange.close();
                return;
            }
            if (forcedPath != null && path.contains(forcedPath)) {
                send(exchange, 200, forcedResponse);
                return;
            }
            if (path.startsWith("/v1/payments/secrets/")) {
                respondToSecret(exchange, path, request);
            } else if (path.contains("/sign/")) {
                var signer = signature(request);
                signer.initSign((request.path("key_version").asInt() == 1 ? keyPair : rotatedPair).getPrivate());
                signer.update(Base64.getDecoder().decode(request.path("input").asString()));
                send(
                        exchange,
                        200,
                        Map.of(
                                "data",
                                Map.of(
                                        "signature",
                                        "vault:v"
                                                + request.path("key_version").asInt()
                                                + ":"
                                                + Base64.getEncoder().encodeToString(signer.sign()))));
            } else if (path.contains("/verify/")) {
                var verifier = signature(request);
                String envelope = request.path("signature").asString();
                verifier.initVerify((envelope.startsWith("vault:v1:") ? keyPair : rotatedPair).getPublic());
                verifier.update(Base64.getDecoder().decode(request.path("input").asString()));
                boolean valid = malformedVerification
                        ? false
                        : verifier.verify(Base64.getDecoder().decode(envelope.substring(envelope.indexOf(':', 7) + 1)));
                send(exchange, 200, Map.of("data", malformedVerification ? Map.of() : Map.of("valid", valid)));
            } else if (path.contains("/encrypt/")) {
                send(
                        exchange,
                        200,
                        Map.of(
                                "data",
                                Map.of(
                                        "ciphertext",
                                        "vault:v"
                                                + request.path("key_version").asInt()
                                                + ":"
                                                + request.path("plaintext").asString())));
            } else if (path.contains("/decrypt/")) {
                String envelope = request.path("ciphertext").asString();
                send(
                        exchange,
                        200,
                        Map.of(
                                "data",
                                Map.of(
                                        "plaintext",
                                        envelope.endsWith("AA==")
                                                ? ""
                                                : envelope.substring(envelope.indexOf(':', 7) + 1))));
            } else if (path.endsWith("/wrapping_key")) {
                send(exchange, 200, Map.of("data", Map.of("public_key", publicPem())));
            } else if ("GET".equals(exchange.getRequestMethod())) {
                send(
                        exchange,
                        200,
                        Map.of(
                                "data",
                                Map.of(
                                        "name",
                                        "merchant",
                                        "type",
                                        "rsa-2048",
                                        "latest_version",
                                        latestVersion,
                                        "min_encryption_version",
                                        0,
                                        "min_decryption_version",
                                        minVerificationVersion,
                                        "supports_encryption",
                                        true,
                                        "supports_decryption",
                                        true,
                                        "supports_signing",
                                        true,
                                        "keys",
                                        Map.of(
                                                "1",
                                                Map.of("public_key", publicPem()),
                                                "2",
                                                Map.of(
                                                        "public_key",
                                                        publicPem(rotatedPair == null ? keyPair : rotatedPair))))));
            } else {
                if (path.endsWith("/rotate")) {
                    rotatedPair = KeyPairGenerator.getInstance("RSA").generateKeyPair();
                    latestVersion++;
                }
                exchange.sendResponseHeaders(204, -1);
                exchange.close();
            }
        } catch (Exception failure) {
            try {
                send(exchange, 500, Map.of("errors", List.of("fixture-failed")));
            } catch (Exception ignored) {
                exchange.close();
            }
        }
    }

    private void respondToSecret(HttpExchange exchange, String path, JsonNode request) throws Exception {
        String name = path.substring(path.lastIndexOf('/') + 1);
        int version = secretVersions.getOrDefault(name, 0);
        if ("GET".equals(exchange.getRequestMethod())) {
            if (version == 0) {
                send(exchange, 404, Map.of("errors", List.of("synthetic internal details")));
            } else if (path.contains("/metadata/")) {
                send(exchange, 200, Map.of("data", Map.of("current_version", version)));
            } else {
                send(exchange, 200, Map.of("data", Map.of("data", Map.of("value", secretValues.get(name)))));
            }
            return;
        }
        String concurrentValue = concurrentSecretValue.getAndSet(null);
        if (concurrentValue != null && "PATCH".equals(exchange.getRequestMethod())) {
            version++;
            secretVersions.put(name, version);
            secretValues.put(name, concurrentValue);
        }
        int cas = request.path("options").path("cas").asInt(-1);
        boolean valid = switch (exchange.getRequestMethod()) {
            case "POST" -> version == 0 && cas == 0;
            case "PATCH" ->
                version > 0
                        && cas == version
                        && "application/merge-patch+json"
                                .equals(exchange.getRequestHeaders().getFirst("Content-Type"));
            default -> false;
        };
        if (!valid) {
            send(exchange, 400, Map.of("errors", List.of("synthetic write failure")));
            return;
        }
        secretValues.put(name, request.path("data").path("value").asString());
        secretVersions.put(name, version + 1);
        send(exchange, 200, Map.of("data", Map.of("version", version + 1)));
    }

    private Signature signature(JsonNode request) throws Exception {
        boolean pss = "pss".equals(request.path("signature_algorithm").asString());
        if (pss) {
            assertThat(request.path("salt_length").asString()).isEqualTo("hash");
        }
        assertThat(request.path("prehashed").asBoolean()).isFalse();
        Signature signature = Signature.getInstance(pss ? "RSASSA-PSS" : "SHA256withRSA");
        if (pss) {
            signature.setParameter(new PSSParameterSpec("SHA-256", "MGF1", MGF1ParameterSpec.SHA256, 32, 1));
        }
        return signature;
    }

    private String publicPem() {
        return publicPem(keyPair);
    }

    private String publicPem(KeyPair pair) {
        return "-----BEGIN PUBLIC KEY-----\n"
                + Base64.getEncoder().encodeToString(pair.getPublic().getEncoded())
                + "\n-----END PUBLIC KEY-----";
    }

    private void send(HttpExchange exchange, int status, Object body) throws Exception {
        byte[] response = mapper.writeValueAsBytes(body);
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(status, response.length);
        exchange.getResponseBody().write(response);
        exchange.close();
    }

    private record Request(String path, String token, String namespace, JsonNode body) {}
}
