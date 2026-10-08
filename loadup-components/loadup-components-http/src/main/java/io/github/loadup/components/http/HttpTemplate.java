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

import io.micrometer.core.instrument.MeterRegistry;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Supplier;
import org.apache.hc.client5.http.config.ConnectionConfig;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.client5.http.ssl.ClientTlsStrategyBuilder;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.http.io.SocketConfig;
import org.apache.hc.core5.util.Timeout;
import org.springframework.boot.ssl.SslBundles;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;
import tools.jackson.databind.ObjectMapper;

/** Named synchronous operations with bounded bodies, explicit credentials and atomic configuration. */
public class HttpTemplate implements AutoCloseable {
    private final Supplier<RestClient.Builder> builders;
    private final ObjectMapper mapper;
    private final HttpCredentialProvider credentials;
    private final SslBundles sslBundles;
    private final MeterRegistry metrics;
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    private Map<String, RuntimeClient> clients = Map.of();
    private long version;
    private boolean closed;

    public HttpTemplate(
            HttpProperties properties,
            Supplier<RestClient.Builder> builders,
            ObjectMapper mapper,
            HttpCredentialProvider credentials,
            SslBundles sslBundles,
            MeterRegistry metrics) {
        this.builders = builders;
        this.mapper = mapper;
        this.credentials = credentials;
        this.sslBundles = sslBundles;
        this.metrics = metrics;
        refresh(properties);
    }

    /** Builds first, swaps only after active calls finish, then closes the previous pools. */
    public synchronized long refresh(HttpProperties properties) {
        if (properties == null || properties.clients().size() > 256)
            throw new IllegalArgumentException("Invalid HTTP clients");
        Map<String, RuntimeClient> replacement = new HashMap<>();
        try {
            for (var entry : properties.clients().entrySet()) {
                name(entry.getKey());
                HttpProperties.Client config = entry.getValue();
                if (config.operations().size() > 256) throw new IllegalArgumentException("Too many HTTP operations");
                config.operations().forEach((operation, settings) -> {
                    name(operation);
                    headers(settings.headers());
                });
                headers(config.headers());
                if (config.credentialRef() != null && credentials == null)
                    throw new IllegalArgumentException("HTTP credentials require a provider");
                replacement.put(entry.getKey(), create(config));
            }
        } catch (RuntimeException failure) {
            release(replacement);
            throw failure;
        }
        lock.writeLock().lock();
        try {
            if (closed) {
                release(replacement);
                throw new IllegalStateException("HTTP template is closed");
            }
            Map<String, RuntimeClient> previous = clients;
            clients = Map.copyOf(replacement);
            version++;
            release(previous);
            return version;
        } finally {
            lock.writeLock().unlock();
        }
    }

    public ResponseEntity<byte[]> exchange(String client, String operation, HttpCall call) {
        return execute(client, operation, call, (response, limit) -> {
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            copy(response, output, limit);
            return output.toByteArray();
        });
    }

    public <T> ResponseEntity<T> exchange(String client, String operation, HttpCall call, Class<T> responseType) {
        ResponseEntity<byte[]> response = exchange(client, operation, call);
        try {
            byte[] body = response.getBody();
            T result = body == null || body.length == 0 ? null : mapper.readValue(body, responseType);
            return new ResponseEntity<>(result, response.getHeaders(), response.getStatusCode());
        } catch (RuntimeException failure) {
            throw new HttpCallException(HttpCallException.Kind.RESPONSE_DECODING);
        }
    }

    /** Does not close the caller's output; failed transfers may have written partial data. */
    public ResponseEntity<Long> download(String client, String operation, HttpCall call, OutputStream output) {
        if (output == null) throw new IllegalArgumentException("output is required");
        return execute(client, operation, call, (response, limit) -> copy(response, output, limit));
    }

    private <T> ResponseEntity<T> execute(String name, String operation, HttpCall call, BodyReader<T> reader) {
        lock.readLock().lock();
        long started = System.nanoTime();
        String outcome = "failure";
        boolean configured = false;
        try {
            if (closed) throw new IllegalStateException("HTTP template is closed");
            RuntimeClient runtime = clients.get(name);
            if (runtime == null) throw new IllegalArgumentException("Unknown HTTP client");
            HttpProperties.Operation definition = runtime.config.operations().get(operation);
            if (definition == null) throw new IllegalArgumentException("Unknown HTTP operation");
            configured = true;
            if (call == null) throw new IllegalArgumentException("call is required");
            URI uri = uri(runtime.config.baseUrl(), definition.path(), call);
            try {
                runtime.policy.resolve(uri.getHost());
            } catch (java.net.UnknownHostException blocked) {
                throw new HttpCallException(HttpCallException.Kind.CONNECTION);
            }
            HttpHeaders requestHeaders = new HttpHeaders();
            requestHeaders.setAll(headers(runtime.config.headers()));
            requestHeaders.setAll(headers(definition.headers()));
            requestHeaders.setAll(headers(call.headers()));
            if (runtime.config.credentialRef() != null) {
                requestHeaders.setAll(headers(credentials.headers(runtime.config.credentialRef())));
            }
            byte[] body = encode(call.body(), runtime.config.maxRequestBytes());
            if (body != null && !requestHeaders.containsHeader(HttpHeaders.CONTENT_TYPE)) {
                requestHeaders.setContentType(
                        call.body() instanceof byte[] || call.body() instanceof String
                                ? MediaType.APPLICATION_OCTET_STREAM
                                : MediaType.APPLICATION_JSON);
            }
            RestClient.RequestBodySpec request =
                    runtime.client.method(definition.method()).uri(uri).headers(h -> h.addAll(requestHeaders));
            if (body != null) request.body(body);
            ResponseEntity<T> result = request.exchange((sent, response) -> {
                if (response.getHeaders().getContentLength() > runtime.config.maxResponseBytes()) {
                    throw new HttpCallException(HttpCallException.Kind.SIZE_LIMIT);
                }
                return new ResponseEntity<>(
                        reader.read(response.getBody(), runtime.config.maxResponseBytes()),
                        HttpHeaders.readOnlyHttpHeaders(response.getHeaders()),
                        response.getStatusCode());
            });
            outcome = result.getStatusCode().is2xxSuccessful() ? "success" : "http_error";
            return result;
        } catch (ResourceAccessException failure) {
            throw classify(failure);
        } finally {
            try {
                if (configured && metrics != null)
                    metrics.timer("loadup.http.calls", "client", name, "operation", operation, "outcome", outcome)
                            .record(System.nanoTime() - started, TimeUnit.NANOSECONDS);
            } finally {
                lock.readLock().unlock();
            }
        }
    }

    static URI uri(URI base, String path, HttpCall call) {
        var builder = UriComponentsBuilder.fromUriString(base.toString()).path(path);
        Map<String, Object> values = new HashMap<>(call.pathVariables());
        int index = 0;
        for (var entry : call.query().entrySet()) {
            for (String value : entry.getValue()) {
                String key;
                do {
                    key = "loadupQuery" + index++;
                } while (values.containsKey(key));
                builder.queryParam(entry.getKey(), "{" + key + "}");
                values.put(key, value);
            }
        }
        URI uri = builder.encode().buildAndExpand(values).toUri();
        if (!base.getHost().equals(uri.getHost())
                || !base.getScheme().equals(uri.getScheme())
                || base.getPort() != uri.getPort()) {
            throw new IllegalArgumentException("HTTP operation cannot change origin");
        }
        return uri;
    }

    private RuntimeClient create(HttpProperties.Client config) {
        var policy = new HttpTargetPolicy(config.allowPrivateAddresses());
        var poolBuilder = PoolingHttpClientConnectionManagerBuilder.create()
                .setDnsResolver(policy)
                .setMaxConnTotal(config.maxConnections())
                .setMaxConnPerRoute(config.maxConnectionsPerRoute())
                .setDefaultConnectionConfig(ConnectionConfig.custom()
                        .setConnectTimeout(
                                Timeout.ofMilliseconds(config.connectTimeout().toMillis()))
                        .build())
                .setDefaultSocketConfig(SocketConfig.custom()
                        .setSoTimeout(
                                Timeout.ofMilliseconds(config.readTimeout().toMillis()))
                        .build());
        if (config.sslBundle() != null) {
            if (sslBundles == null) throw new IllegalArgumentException("HTTP SSL bundle registry is unavailable");
            poolBuilder.setTlsSocketStrategy(ClientTlsStrategyBuilder.create()
                    .setSslContext(sslBundles.getBundle(config.sslBundle()).createSslContext())
                    .buildClassic());
        }
        var pool = poolBuilder.build();
        var transportBuilder = HttpClients.custom()
                .setConnectionManager(pool)
                .disableAutomaticRetries()
                .disableRedirectHandling()
                .disableCookieManagement()
                .disableAuthCaching()
                .setDefaultRequestConfig(RequestConfig.custom()
                        .setConnectionRequestTimeout(
                                Timeout.ofMilliseconds(config.acquireTimeout().toMillis()))
                        .setResponseTimeout(
                                Timeout.ofMilliseconds(config.readTimeout().toMillis()))
                        .build());
        if (config.proxy() != null)
            transportBuilder.setProxy(new HttpHost(
                    config.proxy().getScheme(),
                    config.proxy().getHost(),
                    config.proxy().getPort()));
        CloseableHttpClient transport;
        try {
            transport = transportBuilder.build();
        } catch (RuntimeException failure) {
            pool.close();
            throw failure;
        }
        try {
            RestClient client = builders.get()
                    .clone()
                    .requestFactory(new HttpComponentsClientHttpRequestFactory(transport))
                    .build();
            return new RuntimeClient(config, client, transport, policy);
        } catch (RuntimeException failure) {
            try {
                transport.close();
            } catch (IOException ignored) {
            }
            throw failure;
        }
    }

    private byte[] encode(Object body, int limit) {
        if (body == null) return null;
        byte[] bytes;
        try {
            bytes = body instanceof byte[] raw
                    ? raw.clone()
                    : body instanceof String text
                            ? text.getBytes(StandardCharsets.UTF_8)
                            : mapper.writeValueAsBytes(body);
        } catch (RuntimeException failure) {
            throw new HttpCallException(HttpCallException.Kind.SERIALIZATION);
        }
        if (bytes.length > limit) throw new HttpCallException(HttpCallException.Kind.SIZE_LIMIT);
        return bytes;
    }

    private static long copy(InputStream input, OutputStream output, long limit) throws IOException {
        byte[] buffer = new byte[8192];
        long total = 0;
        int read;
        while ((read = input.read(buffer)) != -1) {
            total += read;
            if (total > limit) throw new HttpCallException(HttpCallException.Kind.SIZE_LIMIT);
            output.write(buffer, 0, read);
        }
        return total;
    }

    private static HttpCallException classify(Throwable failure) {
        for (Throwable cause = failure; cause != null; cause = cause.getCause()) {
            if (cause instanceof java.net.SocketTimeoutException
                    || cause instanceof java.net.http.HttpTimeoutException
                    || cause.getClass().getSimpleName().contains("Timeout"))
                return new HttpCallException(HttpCallException.Kind.TIMEOUT);
            if (cause instanceof java.net.ConnectException || cause instanceof java.net.UnknownHostException) {
                return new HttpCallException(HttpCallException.Kind.CONNECTION);
            }
        }
        return new HttpCallException(HttpCallException.Kind.TRANSPORT);
    }

    private static Map<String, String> headers(Map<String, String> headers) {
        if (headers == null) throw new IllegalArgumentException("HTTP headers must not be null");
        for (var entry : headers.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            if (!key.matches("[!#$%&'*+.^_`|~0-9A-Za-z-]+")
                    || value.indexOf('\r') >= 0
                    || value.indexOf('\n') >= 0
                    || List.of(
                                    "host",
                                    "content-length",
                                    "transfer-encoding",
                                    "connection",
                                    "upgrade",
                                    "te",
                                    "trailer",
                                    "proxy-authorization",
                                    "keep-alive")
                            .contains(key.toLowerCase(java.util.Locale.ROOT))) {
                throw new IllegalArgumentException("Invalid or transport-owned HTTP header");
            }
        }
        return headers;
    }

    private static void name(String name) {
        if (name == null || !name.matches("[A-Za-z0-9_-]{1,64}"))
            throw new IllegalArgumentException("Invalid HTTP configuration name");
    }

    private static void release(Map<String, RuntimeClient> clients) {
        for (RuntimeClient client : clients.values()) {
            try {
                client.transport.close();
            } catch (IOException ignored) {
            }
        }
    }

    @Override
    public synchronized void close() {
        lock.writeLock().lock();
        try {
            closed = true;
            release(clients);
            clients = Map.of();
        } finally {
            lock.writeLock().unlock();
        }
    }

    private record RuntimeClient(
            HttpProperties.Client config, RestClient client, CloseableHttpClient transport, HttpTargetPolicy policy) {}

    @FunctionalInterface
    private interface BodyReader<T> {
        T read(InputStream body, long limit) throws IOException;
    }
}
