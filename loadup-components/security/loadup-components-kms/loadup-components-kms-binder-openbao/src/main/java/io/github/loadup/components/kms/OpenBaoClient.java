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

import io.github.loadup.components.http.HttpCall;
import io.github.loadup.components.http.HttpCallException;
import io.github.loadup.components.http.HttpProperties;
import io.github.loadup.components.http.HttpTemplate;
import io.micrometer.core.instrument.MeterRegistry;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;
import org.springframework.boot.ssl.SslBundles;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

final class OpenBaoClient implements AutoCloseable {
    private final HttpTemplate http;
    private final ObjectMapper mapper;
    private final int maxInputBytes;
    private final String clientName;

    OpenBaoClient(
            KmsProperties properties,
            KmsTokenProvider tokens,
            boolean management,
            Supplier<RestClient.Builder> builders,
            ObjectMapper mapper,
            SslBundles sslBundles,
            MeterRegistry metrics) {
        this(
                properties,
                tokens,
                builders,
                mapper,
                sslBundles,
                metrics,
                management ? "kms-admin" : "kms",
                management ? "openbao-admin" : "openbao-transit",
                properties.maxInputBytes() * 2 + 4096,
                transitOperations(properties.mount(), management));
    }

    static OpenBaoClient secrets(
            KmsProperties properties,
            String mount,
            KmsTokenProvider tokens,
            Supplier<RestClient.Builder> builders,
            ObjectMapper mapper,
            SslBundles sslBundles,
            MeterRegistry metrics) {
        if (mount == null || mount.length() > 128 || !mount.matches("[A-Za-z0-9_-]+(?:/[A-Za-z0-9_-]+)*")) {
            throw new IllegalArgumentException("Invalid OpenBao secret mount");
        }
        String prefix = "/v1/" + mount;
        String path = prefix + "/data/{key}";
        return new OpenBaoClient(
                properties,
                tokens,
                builders,
                mapper,
                sslBundles,
                metrics,
                "kms-secrets",
                "openbao-secrets",
                KmsSecretManager.MAX_VALUE_BYTES * 6 + 4096,
                Map.of(
                        "get", new HttpProperties.Operation(HttpMethod.GET, path, null),
                        "secret-metadata",
                                new HttpProperties.Operation(HttpMethod.GET, prefix + "/metadata/{key}", null),
                        "create", new HttpProperties.Operation(HttpMethod.POST, path, null),
                        "update",
                                new HttpProperties.Operation(
                                        HttpMethod.PATCH,
                                        path,
                                        Map.of(HttpHeaders.CONTENT_TYPE, "application/merge-patch+json"))));
    }

    private OpenBaoClient(
            KmsProperties properties,
            KmsTokenProvider tokens,
            Supplier<RestClient.Builder> builders,
            ObjectMapper mapper,
            SslBundles sslBundles,
            MeterRegistry metrics,
            String clientName,
            String observerName,
            int requestLimit,
            Map<String, HttpProperties.Operation> operations) {
        this.mapper = mapper;
        maxInputBytes = properties.maxInputBytes();
        this.clientName = clientName;
        Map<String, String> headers =
                properties.namespace() == null ? Map.of() : Map.of("X-Vault-Namespace", properties.namespace());
        var config = new HttpProperties.Client(
                properties.endpoint(),
                false,
                properties.connectTimeout(),
                properties.readTimeout(),
                null,
                null,
                null,
                requestLimit,
                (long) requestLimit + 61440,
                properties.sslBundle(),
                null,
                true,
                "openbao",
                headers,
                operations);
        http = new HttpTemplate(
                new HttpProperties(Map.of(clientName, config)),
                builders,
                mapper,
                reference -> {
                    String token;
                    try {
                        token = tokens.token();
                    } catch (RuntimeException failure) {
                        throw new KmsException(KmsException.Code.AUTHENTICATION);
                    }
                    if (token == null
                            || token.isBlank()
                            || token.length() > 8192
                            || token.indexOf('\r') >= 0
                            || token.indexOf('\n') >= 0) {
                        throw new KmsException(KmsException.Code.AUTHENTICATION);
                    }
                    return Map.of("X-Vault-Token", token);
                },
                sslBundles,
                metrics,
                observerName);
    }

    private static Map<String, HttpProperties.Operation> transitOperations(String mount, boolean management) {
        String prefix = "/v1/" + mount;
        Map<String, HttpProperties.Operation> operations = new HashMap<>();
        operations.put("metadata", new HttpProperties.Operation(HttpMethod.GET, prefix + "/keys/{key}", null));
        if (management) {
            operations.put("create", new HttpProperties.Operation(HttpMethod.POST, prefix + "/keys/{key}", null));
            operations.put(
                    "rotate", new HttpProperties.Operation(HttpMethod.POST, prefix + "/keys/{key}/rotate", null));
            operations.put(
                    "configure", new HttpProperties.Operation(HttpMethod.POST, prefix + "/keys/{key}/config", null));
            operations.put("wrapping", new HttpProperties.Operation(HttpMethod.GET, prefix + "/wrapping_key", null));
            operations.put(
                    "import", new HttpProperties.Operation(HttpMethod.POST, prefix + "/keys/{key}/import", null));
        } else {
            operations.put("encrypt", new HttpProperties.Operation(HttpMethod.POST, prefix + "/encrypt/{key}", null));
            operations.put("decrypt", new HttpProperties.Operation(HttpMethod.POST, prefix + "/decrypt/{key}", null));
            operations.put(
                    "sign", new HttpProperties.Operation(HttpMethod.POST, prefix + "/sign/{key}/sha2-256", null));
            operations.put(
                    "verify", new HttpProperties.Operation(HttpMethod.POST, prefix + "/verify/{key}/sha2-256", null));
        }
        return operations;
    }

    static String text(JsonNode node, String field) {
        JsonNode value = node.path(field);
        if (!value.isString() || value.asString().isEmpty()) {
            throw new KmsException(KmsException.Code.INVALID_RESPONSE);
        }
        return value.asString();
    }

    static int version(String envelope) {
        if (envelope == null || !envelope.matches("vault:v[1-9][0-9]*:[A-Za-z0-9+/=]+")) {
            throw new KmsException(KmsException.Code.INVALID_RESPONSE);
        }
        try {
            return Integer.parseInt(envelope.substring(7, envelope.indexOf(':', 7)));
        } catch (NumberFormatException invalid) {
            throw new KmsException(KmsException.Code.INVALID_RESPONSE);
        }
    }

    JsonNode call(String operation, String key, Object body, boolean requireData) {
        if (!"wrapping".equals(operation)) {
            KmsKeyRef.validateName(key);
        }
        try {
            var response = http.exchange(
                    clientName, operation, new HttpCall(key == null ? Map.of() : Map.of("key", key), null, null, body));
            int status = response.getStatusCode().value();
            if (!response.getStatusCode().is2xxSuccessful()) {
                throw new KmsException(
                        switch (status) {
                            case 401 -> KmsException.Code.AUTHENTICATION;
                            case 403 -> KmsException.Code.ACCESS_DENIED;
                            case 404 -> KmsException.Code.NOT_FOUND;
                            case 429, 500, 502, 503, 504 -> KmsException.Code.UNAVAILABLE;
                            default -> KmsException.Code.INVALID_REQUEST;
                        });
            }
            if (!requireData) {
                return null;
            }
            JsonNode data;
            try {
                data = mapper.readTree(response.getBody()).path("data");
            } catch (RuntimeException invalid) {
                throw new KmsException(KmsException.Code.INVALID_RESPONSE);
            }
            if (!data.isObject()) {
                throw new KmsException(KmsException.Code.INVALID_RESPONSE);
            }
            return data;
        } catch (HttpCallException failure) {
            throw new KmsException(
                    switch (failure.getKind()) {
                        case TIMEOUT -> KmsException.Code.UNAVAILABLE;
                        case SERIALIZATION -> KmsException.Code.INVALID_REQUEST;
                        case SIZE_LIMIT, RESPONSE_DECODING -> KmsException.Code.INVALID_RESPONSE;
                        default -> KmsException.Code.TRANSPORT;
                    });
        }
    }

    void validateInput(byte[] input) {
        if (input == null || input.length > maxInputBytes) {
            throw new IllegalArgumentException("KMS input exceeds configured limit or is null");
        }
    }

    void validateEncoded(String input) {
        if (input == null || input.length() > maxInputBytes * 2 + 4096) {
            throw new IllegalArgumentException("Invalid encoded KMS input");
        }
    }

    @Override
    public void close() {
        http.close();
    }
}
