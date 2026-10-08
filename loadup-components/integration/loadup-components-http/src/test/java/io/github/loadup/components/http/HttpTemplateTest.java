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
