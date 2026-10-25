/*
 * #%L
 * LoadUp Http
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
package io.github.loadup.components.http;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.sun.net.httpserver.HttpServer;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import java.io.ByteArrayOutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.json.JsonMapper;

class HttpTemplateTest {
    private HttpServer server;
    private HttpTemplate http;
    private SimpleMeterRegistry metrics;
    private final AtomicInteger calls = new AtomicInteger();

    @BeforeEach
    void start() throws Exception {
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/echo", exchange -> {
            calls.incrementAndGet();
            byte[] body = exchange.getRequestBody().readAllBytes();
            exchange.sendResponseHeaders(200, body.length);
            exchange.getResponseBody().write(body);
            exchange.close();
        });
        server.createContext("/fail", exchange -> {
            calls.incrementAndGet();
            byte[] body = "failure".getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(503, body.length);
            exchange.getResponseBody().write(body);
            exchange.close();
        });
        server.createContext("/redirect", exchange -> {
            calls.incrementAndGet();
            exchange.getResponseHeaders().add("Location", "/fail");
            exchange.sendResponseHeaders(302, -1);
            exchange.close();
        });
        server.start();
        metrics = new SimpleMeterRegistry();
        http = new HttpTemplate(
                config(1024L, true, Map.of()),
                RestClient::builder,
                JsonMapper.builder().build(),
                null,
                null,
                metrics);
    }

    @AfterEach
    void stop() {
        if (http != null) http.close();
        if (server != null) server.stop(0);
        if (metrics != null) metrics.close();
    }

    @Test
    void sendsJsonAndPreservesProtocolStatusWithoutRetryOrRedirect() {
        var response = http.exchange("test", "echo", new HttpCall(null, null, null, Map.of("amount", 100)), Map.class);
        assertThat(response.getBody()).containsEntry("amount", 100);
        var failed = http.exchange("test", "fail", HttpCall.empty());
        assertThat(failed.getStatusCode().value()).isEqualTo(503);
        assertThat(new String(failed.getBody(), StandardCharsets.UTF_8)).isEqualTo("failure");
        assertThat(http.exchange("test", "redirect", HttpCall.empty())
                        .getStatusCode()
                        .value())
                .isEqualTo(302);
        assertThat(calls).hasValue(3);
        assertThat(metrics.get("loadup.http.calls")
                        .tags("client", "test", "operation", "echo", "outcome", "success")
                        .timer()
                        .count())
                .isEqualTo(1L);
        assertThat(metrics.get("loadup.http.calls")
                        .tags("client", "test", "operation", "fail", "outcome", "failure")
                        .timer()
                        .count())
                .isEqualTo(1L);
        assertThat(metrics.get("loadup.http.calls")
                        .tags("client", "test", "operation", "redirect", "outcome", "failure")
                        .timer()
                        .count())
                .isEqualTo(1L);
    }

    @Test
    void rawBodiesAndDownloadsAreNotWrapped() {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        var response = http.download("test", "echo", new HttpCall(null, null, null, "raw"), output);
        assertThat(response.getBody()).isEqualTo(3L);
        assertThat(output.toString(StandardCharsets.UTF_8)).isEqualTo("raw");
    }

    @Test
    void limitsBodiesAndBlocksPrivateTargetsByDefault() {
        http.refresh(config(2L, true, Map.of()));
        assertThatThrownBy(() -> http.exchange("test", "fail", HttpCall.empty()))
                .isInstanceOf(HttpCallException.class)
                .hasMessageContaining("SIZE_LIMIT");
        http.refresh(config(1024L, false, Map.of()));
        assertThatThrownBy(() -> http.exchange("test", "fail", HttpCall.empty()))
                .isInstanceOf(HttpCallException.class)
                .hasMessageContaining("CONNECTION");
        assertThat(calls).hasValue(1);
    }

    @Test
    void invalidRefreshKeepsPreviousConfiguration() {
        assertThatThrownBy(() -> http.refresh(config(1024L, true, Map.of("X-Test", "bad\r\nheader"))))
                .isInstanceOf(IllegalArgumentException.class);
        assertThat(http.exchange("test", "fail", HttpCall.empty())
                        .getStatusCode()
                        .value())
                .isEqualTo(503);
    }

    @Test
    void encodesPathAndQueryValuesWithoutChangingOrigin() {
        URI uri = HttpTemplate.uri(
                URI.create("https://example.test/api"),
                "/orders/{id}",
                new HttpCall(Map.of("id", "a/b ?"), Map.of("q", List.of("a+b&c")), null, null));
        assertThat(uri.toASCIIString()).isEqualTo("https://example.test/api/orders/a%2Fb%20%3F?q=a%2Bb%26c");
    }

    @Test
    void addressPolicyRejectsLoopbackAndUniqueLocalAddresses() throws Exception {
        assertThat(HttpTargetPolicy.blocked(InetAddress.getByName("127.0.0.1"))).isTrue();
        assertThat(HttpTargetPolicy.blocked(InetAddress.getByName("fc00::1"))).isTrue();
        assertThat(HttpTargetPolicy.blocked(InetAddress.getByName("100.64.0.1")))
                .isTrue();
    }

    @Test
    void readTimeoutIsClassifiedAndTransportFailureDoesNotRetry() {
        server.createContext("/slow", exchange -> {
            calls.incrementAndGet();
            try {
                new java.util.concurrent.CountDownLatch(1).await(2, java.util.concurrent.TimeUnit.SECONDS);
            } catch (InterruptedException interrupted) {
                Thread.currentThread().interrupt();
            }
            exchange.close();
        });
        var operations = Map.of("slow", new HttpProperties.Operation(HttpMethod.GET, "/slow", null));
        var client = new HttpProperties.Client(
                URI.create("http://127.0.0.1:" + server.getAddress().getPort()),
                null,
                Duration.ofMillis(100),
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                true,
                null,
                null,
                operations);
        http.refresh(new HttpProperties(Map.of("test", client)));
        assertThatThrownBy(() -> http.exchange("test", "slow", HttpCall.empty()))
                .isInstanceOf(HttpCallException.class)
                .hasMessageContaining("TIMEOUT");
        assertThat(calls).hasValue(1);
    }

    @Test
    void requestLimitIsCheckedBeforeSending() {
        assertThatThrownBy(() ->
                        http.exchange("test", "echo", new HttpCall(null, null, null, new byte[1024 * 1024 + 1])))
                .isInstanceOf(HttpCallException.class)
                .hasMessageContaining("SIZE_LIMIT");
        assertThat(calls).hasValue(0);
    }

    @Test
    void binaryAndUtf8BodiesAreSentWithoutJsonQuoting() {
        byte[] binary = {0, 1, 2, (byte) 255};
        assertThat(http.exchange("test", "echo", new HttpCall(null, null, null, binary))
                        .getBody())
                .isEqualTo(binary);
        String text = "payment-€";
        assertThat(http.exchange("test", "echo", new HttpCall(null, null, null, text))
                        .getBody())
                .isEqualTo(text.getBytes(StandardCharsets.UTF_8));
        assertThat(calls).hasValue(2);
    }

    @Test
    void emptyTypedResponseIsNullAndMalformedJsonHasSafeClassification() {
        assertThat(http.exchange("test", "redirect", HttpCall.empty(), Map.class)
                        .getBody())
                .isNull();
        assertThatThrownBy(() -> http.exchange("test", "fail", HttpCall.empty(), Map.class))
                .isInstanceOfSatisfying(
                        HttpCallException.class,
                        failure -> assertThat(failure.getKind()).isEqualTo(HttpCallException.Kind.RESPONSE_DECODING))
                .hasMessageNotContaining("failure");
    }

    @Test
    void malformedAndTransportOwnedHeadersAreRejectedBeforeSending() {
        List<Map<String, String>> invalid = List.of(
                Map.of("X-Test", "bad\rheader"), Map.of("X-Test", "bad\nheader"),
                Map.of("Bad Header", "value"), Map.of("Host", "forged.example"),
                Map.of("cOnTeNt-LeNgTh", "123"), Map.of("Transfer-Encoding", "chunked"),
                Map.of("Connection", "close"), Map.of("Proxy-Authorization", "secret"));
        for (Map<String, String> headers : invalid) {
            assertThatThrownBy(() -> http.exchange("test", "echo", new HttpCall(null, null, headers, "body")))
                    .isInstanceOf(IllegalArgumentException.class);
        }
        assertThat(calls).hasValue(0);
    }

    @Test
    void unknownOperationNullCallAndNullDownloadFailBeforeSending() {
        assertThatThrownBy(() -> http.exchange("missing", "echo", HttpCall.empty()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("client");
        assertThatThrownBy(() -> http.exchange("test", "missing", HttpCall.empty()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("operation");
        assertThatThrownBy(() -> http.exchange("test", "echo", null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("call");
        assertThatThrownBy(() -> http.download("test", "echo", HttpCall.empty(), null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("output");
        assertThat(calls).hasValue(0);
    }

    @Test
    void closedTemplateRejectsCallsAndRefreshWithoutSending() {
        http.close();
        http.close();
        assertThatThrownBy(() -> http.exchange("test", "echo", HttpCall.empty()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("closed");
        assertThatThrownBy(() -> http.refresh(config(1024L, true, Map.of())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("closed");
        assertThat(calls).hasValue(0);
    }

    @Test
    void invalidRefreshNamesAndNullPropertiesLeaveCurrentVersionUsable() {
        assertThatThrownBy(() -> http.refresh(null)).isInstanceOf(IllegalArgumentException.class);
        HttpProperties.Client valid = config(1024L, true, Map.of()).clients().get("test");
        assertThatThrownBy(() -> http.refresh(new HttpProperties(Map.of("bad name", valid))))
                .isInstanceOf(IllegalArgumentException.class);
        var operations = new java.util.HashMap<String, HttpProperties.Operation>();
        for (int i = 0; i < 257; i++)
            operations.put("operation" + i, new HttpProperties.Operation(HttpMethod.POST, "/echo", null));
        assertThatThrownBy(() -> http.refresh(
                        new HttpProperties(Map.of("test", copyClient(valid, null, null, null, operations)))))
                .isInstanceOf(IllegalArgumentException.class);
        var clients = new java.util.HashMap<String, HttpProperties.Client>();
        for (int i = 0; i < 257; i++) clients.put("client" + i, valid);
        assertThatThrownBy(() -> http.refresh(new HttpProperties(clients)))
                .isInstanceOf(IllegalArgumentException.class);
        assertThat(http.refresh(config(1024L, true, Map.of()))).isEqualTo(2L);
        assertThat(http.exchange("test", "echo", new HttpCall(null, null, null, "usable"))
                        .getBody())
                .isEqualTo("usable".getBytes(StandardCharsets.UTF_8));
    }

    @Test
    void chunkedDownloadsEnforceLimitAndNeverCloseCallerOutput() {
        server.createContext("/chunked", exchange -> {
            exchange.sendResponseHeaders(200, 0);
            exchange.getResponseBody().write("large-body".getBytes(StandardCharsets.UTF_8));
            exchange.close();
        });
        var client = config(4L, true, Map.of()).clients().get("test");
        http.refresh(new HttpProperties(Map.of(
                "test",
                copyClient(
                        client,
                        null,
                        null,
                        null,
                        Map.of("chunked", new HttpProperties.Operation(HttpMethod.GET, "/chunked", null))))));
        class OwnedOutput extends ByteArrayOutputStream {
            boolean closed;

            @Override
            public void close() {
                closed = true;
            }
        }
        var output = new OwnedOutput();
        assertThatThrownBy(() -> http.download("test", "chunked", HttpCall.empty(), output))
                .isInstanceOfSatisfying(
                        HttpCallException.class,
                        failure -> assertThat(failure.getKind()).isEqualTo(HttpCallException.Kind.SIZE_LIMIT));
        assertThat(output.closed).isFalse();
        output.write(1);
    }

    @Test
    void requestSerializationFailureIsSafeAndDoesNotSend() {
        class BrokenBody {
            public String getSecret() {
                throw new IllegalStateException("sensitive payload");
            }
        }
        assertThatThrownBy(() -> http.exchange("test", "echo", new HttpCall(null, null, null, new BrokenBody())))
                .isInstanceOfSatisfying(
                        HttpCallException.class,
                        failure -> assertThat(failure.getKind()).isEqualTo(HttpCallException.Kind.SERIALIZATION))
                .hasMessageNotContaining("sensitive payload");
        assertThat(calls).hasValue(0);
    }

    @Test
    void builderFailureDuringRefreshKeepsPreviousPoolUsable() {
        http.close();
        var failBuild = new java.util.concurrent.atomic.AtomicBoolean();
        http = new HttpTemplate(
                config(1024L, true, Map.of()),
                () -> {
                    if (failBuild.get()) throw new IllegalStateException("builder failure");
                    return RestClient.builder();
                },
                JsonMapper.builder().build(),
                null,
                null,
                null);
        failBuild.set(true);
        assertThatThrownBy(() -> http.refresh(config(1024L, true, Map.of())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("builder failure");
        assertThat(http.exchange("test", "echo", new HttpCall(null, null, null, "still usable"))
                        .getBody())
                .isEqualTo("still usable".getBytes(StandardCharsets.UTF_8));
    }

    @Test
    void configuredCredentialsOverrideUntrustedCallerAuthorization() {
        server.createContext("/credentials", exchange -> {
            byte[] authorization =
                    exchange.getRequestHeaders().getFirst("Authorization").getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(200, authorization.length);
            exchange.getResponseBody().write(authorization);
            exchange.close();
        });
        var client = config(1024L, true, Map.of()).clients().get("test");
        var settings = new HttpProperties(Map.of(
                "test",
                copyClient(
                        client,
                        "trusted-key",
                        null,
                        null,
                        Map.of("credentials", new HttpProperties.Operation(HttpMethod.GET, "/credentials", null)))));
        http.close();
        http = new HttpTemplate(
                settings,
                RestClient::builder,
                JsonMapper.builder().build(),
                reference -> {
                    assertThat(reference).isEqualTo("trusted-key");
                    return Map.of("Authorization", "Bearer managed");
                },
                null,
                null);
        assertThat(http.exchange(
                                "test",
                                "credentials",
                                new HttpCall(null, null, Map.of("Authorization", "Bearer untrusted"), null))
                        .getBody())
                .isEqualTo("Bearer managed".getBytes(StandardCharsets.UTF_8));
    }

    @Test
    void missingCredentialProviderAndSslRegistryRejectRefreshWithoutReplacingCurrentPool() {
        var client = config(1024L, true, Map.of()).clients().get("test");
        assertThatThrownBy(() -> http.refresh(new HttpProperties(
                        Map.of("test", copyClient(client, "secret", null, null, client.operations())))))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("provider");
        assertThatThrownBy(() -> http.refresh(
                        new HttpProperties(Map.of("test", copyClient(client, null, "tls", null, client.operations())))))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("SSL");
        assertThat(http.exchange("test", "echo", new HttpCall(null, null, null, "usable"))
                        .getBody())
                .isEqualTo("usable".getBytes(StandardCharsets.UTF_8));
    }

    @Test
    void callerContentTypeAndOperationHeadersRemainAvailable() {
        server.createContext("/headers", exchange -> {
            String values = exchange.getRequestHeaders().getFirst("Content-Type") + ":"
                    + exchange.getRequestHeaders().getFirst("X-Operation");
            byte[] bytes = values.getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(200, bytes.length);
            exchange.getResponseBody().write(bytes);
            exchange.close();
        });
        var client = config(1024L, true, Map.of()).clients().get("test");
        http.refresh(new HttpProperties(Map.of(
                "test",
                copyClient(
                        client,
                        null,
                        null,
                        null,
                        Map.of(
                                "headers",
                                new HttpProperties.Operation(
                                        HttpMethod.POST, "/headers", Map.of("X-Operation", "configured")))))));
        assertThat(http.exchange(
                                "test",
                                "headers",
                                new HttpCall(null, null, Map.of("Content-Type", "text/plain"), "body"))
                        .getBody())
                .isEqualTo("text/plain:configured".getBytes(StandardCharsets.UTF_8));
    }

    @Test
    void signerSeesFinalWireRequestAndCanAddAuthorizationWithoutMutatingBody() {
        AtomicReference<String> method = new AtomicReference<>();
        AtomicReference<String> rawPath = new AtomicReference<>();
        AtomicReference<String> rawQuery = new AtomicReference<>();
        AtomicReference<String> authorization = new AtomicReference<>();
        AtomicReference<String> receivedBody = new AtomicReference<>();
        server.createContext("/signed", exchange -> {
            calls.incrementAndGet();
            method.set(exchange.getRequestMethod());
            rawPath.set(exchange.getRequestURI().getRawPath());
            rawQuery.set(exchange.getRequestURI().getRawQuery());
            authorization.set(exchange.getRequestHeaders().getFirst("Authorization"));
            receivedBody.set(new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
            byte[] response = receivedBody.get().getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(200, response.length);
            exchange.getResponseBody().write(response);
            exchange.close();
        });
        var client = config(1024L, true, Map.of()).clients().get("test");
        http.refresh(new HttpProperties(Map.of(
                "test",
                copyClient(
                        client,
                        null,
                        null,
                        null,
                        Map.of("signed", new HttpProperties.Operation(HttpMethod.POST, "/signed/{id}", null))))));
        String body = "退款-€";

        var response = http.exchange(
                "test",
                "signed",
                new HttpCall(Map.of("id", "a/b"), Map.of("q", List.of("x+y&z")), null, body),
                (requestMethod, uri, signedBody) -> {
                    assertThat(requestMethod).isEqualTo(HttpMethod.POST);
                    assertThat(uri.getRawPath()).isEqualTo("/signed/a%2Fb");
                    assertThat(uri.getRawQuery()).isEqualTo("q=x%2By%26z");
                    assertThat(new String(signedBody, StandardCharsets.UTF_8)).isEqualTo(body);
                    java.util.Arrays.fill(signedBody, (byte) 'x');
                    return Map.of("Authorization", "Bearer signed");
                });

        assertThat(response.getBody()).isEqualTo(body.getBytes(StandardCharsets.UTF_8));
        assertThat(method).hasValue("POST");
        assertThat(rawPath).hasValue("/signed/a%2Fb");
        assertThat(rawQuery).hasValue("q=x%2By%26z");
        assertThat(authorization).hasValue("Bearer signed");
        assertThat(receivedBody).hasValue(body);
        assertThat(calls).hasValue(1);
    }

    @Test
    void signerFailureAndMalformedSignerHeadersFailBeforeSending() {
        assertThatThrownBy(() ->
                        http.exchange("test", "echo", new HttpCall(null, null, null, "body"), (method, uri, body) -> {
                            throw new IllegalStateException("signing failed");
                        }))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("signing failed");
        assertThatThrownBy(() -> http.exchange(
                        "test",
                        "echo",
                        new HttpCall(null, null, null, "body"),
                        (method, uri, body) -> Map.of("X-Test", "invalid\rheader")))
                .isInstanceOf(IllegalArgumentException.class);
        assertThat(calls).hasValue(0);
    }

    private static HttpProperties.Client copyClient(
            HttpProperties.Client source,
            String credentialRef,
            String sslBundle,
            URI proxy,
            Map<String, HttpProperties.Operation> operations) {
        return new HttpProperties.Client(
                source.baseUrl(),
                source.connectTimeout(),
                source.readTimeout(),
                source.acquireTimeout(),
                source.maxConnections(),
                source.maxConnectionsPerRoute(),
                source.maxRequestBytes(),
                source.maxResponseBytes(),
                sslBundle,
                proxy,
                source.allowPrivateAddresses(),
                credentialRef,
                source.headers(),
                operations);
    }

    private HttpProperties config(long limit, boolean allowPrivate, Map<String, String> headers) {
        var client = new HttpProperties.Client(
                URI.create("http://127.0.0.1:" + server.getAddress().getPort()),
                Duration.ofSeconds(1),
                Duration.ofSeconds(1),
                null,
                null,
                null,
                null,
                limit,
                null,
                null,
                allowPrivate,
                null,
                headers,
                Map.of(
                        "echo",
                        new HttpProperties.Operation(HttpMethod.POST, "/echo", null),
                        "fail",
                        new HttpProperties.Operation(HttpMethod.GET, "/fail", null),
                        "redirect",
                        new HttpProperties.Operation(HttpMethod.GET, "/redirect", null)));
        return new HttpProperties(Map.of("test", client));
    }
}
