/*
 * #%L
 * LoadUp Kms
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

/** Protocol fixtures verify mapping and JCA interoperability, not a live OpenBao deployment. */
class OpenBaoKmsTest {
    private final JsonMapper mapper = JsonMapper.builder().build();
    private final List<Request> requests = new CopyOnWriteArrayList<>();
    private final AtomicReference<String> token = new AtomicReference<>("runtime-token");
    private HttpServer server;
    private OpenBaoKmsTemplate kms;
    private OpenBaoKeyManager manager;
    private KeyPair keyPair;
    private int latestVersion = 1;
    private int errorStatus;
    private boolean malformedVerification;

    @BeforeEach
    void start() throws Exception {
        var generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(2048);
        keyPair = generator.generateKeyPair();
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/", this::respond);
        server.start();
        var config = new KmsProperties(
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
                new OpenBaoTransitClient(config, token::get, false, RestClient::builder, mapper, null, null));
        manager = new OpenBaoKeyManager(new OpenBaoTransitClient(
                config, () -> "management-token", true, RestClient::builder, mapper, null, null));
    }

    @AfterEach
    void stop() {
        if (kms != null) kms.close();
        if (manager != null) manager.close();
        if (server != null) server.stop(0);
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
            if (path.contains("/sign/")) {
                var signer = signature(request);
                signer.initSign(keyPair.getPrivate());
                signer.update(Base64.getDecoder().decode(request.path("input").asString()));
                send(
                        exchange,
                        200,
                        Map.of(
                                "data",
                                Map.of(
                                        "signature",
                                        "vault:v" + request.path("key_version").asInt() + ":"
                                                + Base64.getEncoder().encodeToString(signer.sign()))));
            } else if (path.contains("/verify/")) {
                var verifier = signature(request);
                verifier.initVerify(keyPair.getPublic());
                verifier.update(Base64.getDecoder().decode(request.path("input").asString()));
                String envelope = request.path("signature").asString();
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
                                        "vault:v" + request.path("key_version").asInt() + ":"
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
                                        1,
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
                                                Map.of("public_key", publicPem())))));
            } else {
                if (path.endsWith("/rotate")) latestVersion++;
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

    private Signature signature(JsonNode request) throws Exception {
        boolean pss = "pss".equals(request.path("signature_algorithm").asString());
        if (pss) {
            assertThat(request.path("salt_length").asString()).isEqualTo("hash");
        }
        assertThat(request.path("prehashed").asBoolean()).isFalse();
        Signature signature = Signature.getInstance(pss ? "RSASSA-PSS" : "SHA256withRSA");
        if (pss) signature.setParameter(new PSSParameterSpec("SHA-256", "MGF1", MGF1ParameterSpec.SHA256, 32, 1));
        return signature;
    }

    private String publicPem() {
        return "-----BEGIN PUBLIC KEY-----\n"
                + Base64.getEncoder().encodeToString(keyPair.getPublic().getEncoded()) + "\n-----END PUBLIC KEY-----";
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
