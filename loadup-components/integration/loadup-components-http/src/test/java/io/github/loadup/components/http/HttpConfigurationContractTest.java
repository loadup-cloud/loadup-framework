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

import java.net.URI;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.http.HttpMethod;

class HttpConfigurationContractTest {
    @ParameterizedTest
    @NullSource
    @ValueSource(
            strings = {
                "ftp://example.com",
                "http:/relative",
                "https://user:password@example.com",
                "https://example.com?q=1",
                "https://example.com#fragment"
            })
    void rejectsUnsafeBaseUrls(String url) {
        assertThatThrownBy(() ->
                        client(url == null ? null : URI.create(url), null, null, null, null, null, null, null, null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @ParameterizedTest
    @ValueSource(
            strings = {
                "https://proxy.example:8080",
                "http:/proxy",
                "http://proxy.example",
                "http://proxy.example:0",
                "http://user:password@proxy.example:8080",
                "http://proxy.example:8080?q=1",
                "http://proxy.example:8080#fragment"
            })
    void rejectsUnsafeOrAmbiguousProxyUrls(String proxy) {
        assertThatThrownBy(() -> client(
                        URI.create("https://example.com"), null, null, null, null, null, null, null, URI.create(proxy)))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @ParameterizedTest
    @ValueSource(
            strings = {
                "",
                "relative",
                "//other-host/path",
                "/path?query",
                "/path#fragment",
                "/path\\segment",
                "/parent/../secret"
            })
    @NullSource
    void rejectsPathsThatCouldChangeTargetOrEscapeTheOperation(String path) {
        assertThatThrownBy(() -> new HttpProperties.Operation(HttpMethod.POST, path, null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void methodIsRequiredAndOperationHeadersAreImmutable() {
        assertThatThrownBy(() -> new HttpProperties.Operation(null, "/orders", null))
                .isInstanceOf(IllegalArgumentException.class);
        Map<String, String> headers = new HashMap<>(Map.of("Accept", "application/json"));
        HttpProperties.Operation operation = new HttpProperties.Operation(HttpMethod.POST, "/orders/{id}", headers);
        headers.put("Accept", "text/plain");
        assertThat(operation.headers()).containsEntry("Accept", "application/json");
        assertThatThrownBy(() -> operation.headers().put("Extra", "value"))
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @ParameterizedTest
    @ValueSource(longs = {-1, 0, 86_400_001})
    void allTimeoutTypesEnforceTheirMillisecondBounds(long millis) {
        Duration invalid = Duration.ofMillis(millis);
        URI url = URI.create("https://example.com");
        assertThatThrownBy(() -> client(url, invalid, null, null, null, null, null, null, null))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> client(url, null, invalid, null, null, null, null, null, null))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> client(url, null, null, invalid, null, null, null, null, null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @ParameterizedTest
    @CsvSource({"0,1,1,1", "1,0,1,1", "1,2,1,1", "1,1,0,1", "1,1,1,0", "1,1,1,2147483648"})
    void rejectsInvalidConnectionAndPayloadLimits(int total, int perRoute, int requestBytes, long responseBytes) {
        assertThatThrownBy(() -> client(
                        URI.create("https://example.com"),
                        null,
                        null,
                        null,
                        total,
                        perRoute,
                        requestBytes,
                        responseBytes,
                        null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void legalBoundaryValuesAndDefaultsSupportMinimalClientDefinitions() {
        URI url = URI.create("https://example.com/api");
        HttpProperties.Client defaults = client(url, null, null, null, null, null, null, null, null);
        assertThat(defaults.connectTimeout()).isEqualTo(Duration.ofSeconds(3));
        assertThat(defaults.readTimeout()).isEqualTo(Duration.ofSeconds(10));
        assertThat(defaults.acquireTimeout()).isEqualTo(Duration.ofSeconds(3));
        assertThat(defaults.headers()).isEmpty();
        assertThat(defaults.operations()).isEmpty();
        HttpProperties.Client bounded = client(
                url,
                Duration.ofMillis(1),
                Duration.ofDays(1),
                Duration.ofMillis(1),
                1,
                1,
                1,
                (long) Integer.MAX_VALUE,
                URI.create("http://proxy.example:8080"));
        assertThat(bounded.maxResponseBytes()).isEqualTo(Integer.MAX_VALUE);
        assertThat(bounded.proxy()).isEqualTo(URI.create("http://proxy.example:8080"));
        assertThatThrownBy(() -> client(url, Duration.ofNanos(999_999), null, null, null, null, null, null, null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void callSnapshotsNestedQueryCollectionsAndSupportsAbsentOptionalInputs() {
        Map<String, Object> path = new HashMap<>(Map.of("id", "first"));
        List<String> tags = new ArrayList<>(List.of("a", "b"));
        Map<String, List<String>> query = new HashMap<>(Map.of("tag", tags));
        Map<String, String> headers = new HashMap<>(Map.of("Accept", "application/json"));
        HttpCall call = new HttpCall(path, query, headers, "body");
        path.put("id", "changed");
        tags.add("changed");
        query.clear();
        headers.clear();
        assertThat(call.pathVariables().get("id")).isEqualTo("first");
        assertThatThrownBy(() -> call.pathVariables().clear()).isInstanceOf(UnsupportedOperationException.class);
        assertThat(call.query()).containsEntry("tag", List.of("a", "b"));
        assertThat(call.headers()).containsEntry("Accept", "application/json");
        assertThat(call.body()).isEqualTo("body");
        assertThatThrownBy(() -> call.query().get("tag").add("extra"))
                .isInstanceOf(UnsupportedOperationException.class);
        assertThat(HttpCall.empty().pathVariables()).isEmpty();
        assertThat(HttpCall.empty().query()).isEmpty();
        assertThat(HttpCall.empty().headers()).isEmpty();
        assertThat(HttpCall.empty().body()).isNull();
    }

    @Test
    void configurationSnapshotsTheClientMap() {
        Map<String, HttpProperties.Client> clients = new HashMap<>();
        clients.put(
                "payments", client(URI.create("https://example.com"), null, null, null, null, null, null, null, null));
        HttpProperties properties = new HttpProperties(clients);
        clients.clear();
        assertThat(properties.clients()).containsOnlyKeys("payments");
        assertThat(new HttpProperties(null).clients()).isEmpty();
        assertThatThrownBy(() -> properties.clients().clear()).isInstanceOf(UnsupportedOperationException.class);
    }

    private static HttpProperties.Client client(
            URI url,
            Duration connect,
            Duration read,
            Duration acquire,
            Integer total,
            Integer perRoute,
            Integer request,
            Long response,
            URI proxy) {
        return new HttpProperties.Client(
                url, connect, read, acquire, total, perRoute, request, response, null, proxy, false, null, null, null);
    }
}
