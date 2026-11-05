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
package io.github.loadup.components.http;

import java.net.URI;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.ConstructorBinding;
import org.springframework.http.HttpMethod;

@ConfigurationProperties("loadup.http")
public record HttpProperties(Integer defaultPort, Map<String, Client> clients) {
    public HttpProperties(Map<String, Client> clients) {
        this(null, clients);
    }

    @ConstructorBinding
    public HttpProperties {
        defaultPort = defaultPort == null ? Integer.valueOf(8080) : defaultPort;
        if (defaultPort < 1 || defaultPort > 65535) {
            throw new IllegalArgumentException("Invalid HTTP default port");
        }
        if (clients == null) {
            clients = Map.of();
        } else {
            var normalized = new HashMap<String, Client>();
            clients.forEach((name, client) -> normalized.put(name, client == null ? Client.conventional() : client));
            clients = Map.copyOf(normalized);
        }
    }

    public record Client(
            URI baseUrl,
            Duration connectTimeout,
            Duration readTimeout,
            Duration acquireTimeout,
            Integer maxConnections,
            Integer maxConnectionsPerRoute,
            Integer maxRequestBytes,
            Long maxResponseBytes,
            String sslBundle,
            URI proxy,
            boolean allowPrivateAddresses,
            String credentialRef,
            Map<String, String> headers,
            Map<String, Operation> operations) {
        public Client {
            boolean conventional = baseUrl == null;
            connectTimeout = connectTimeout == null ? Duration.ofSeconds(3) : connectTimeout;
            readTimeout = readTimeout == null ? Duration.ofSeconds(10) : readTimeout;
            acquireTimeout = acquireTimeout == null ? Duration.ofSeconds(3) : acquireTimeout;
            maxConnections = maxConnections == null ? Integer.valueOf(50) : maxConnections;
            maxConnectionsPerRoute = maxConnectionsPerRoute == null ? Integer.valueOf(20) : maxConnectionsPerRoute;
            maxRequestBytes = maxRequestBytes == null ? Integer.valueOf(1024 * 1024) : maxRequestBytes;
            maxResponseBytes = maxResponseBytes == null ? Long.valueOf(10L * 1024 * 1024) : maxResponseBytes;
            headers = headers == null ? Map.of() : Map.copyOf(headers);
            operations = operations == null ? Map.of() : Map.copyOf(operations);
            if (!conventional
                    && (!("https".equals(baseUrl.getScheme()) || "http".equals(baseUrl.getScheme()))
                            || baseUrl.getHost() == null
                            || baseUrl.getUserInfo() != null
                            || baseUrl.getQuery() != null
                            || baseUrl.getFragment() != null
                            || baseUrl.getRawPath().contains("{"))) {
                throw new IllegalArgumentException(
                        "base-url must be an HTTP(S) URL without credentials, query or fragment");
            }
            if (conventional) {
                // A client named after an application is an internal service target; resolve it later.
                allowPrivateAddresses = true;
            }
            for (Duration timeout : new Duration[] {connectTimeout, readTimeout, acquireTimeout}) {
                if (timeout.toMillis() < 1 || timeout.toMillis() > 86400000) {
                    throw new IllegalArgumentException("Invalid HTTP timeout");
                }
            }
            if (maxConnections < 1
                    || maxConnectionsPerRoute < 1
                    || maxConnectionsPerRoute > maxConnections
                    || maxRequestBytes < 1
                    || maxResponseBytes < 1
                    || maxResponseBytes > Integer.MAX_VALUE) {
                throw new IllegalArgumentException("Invalid HTTP connection or size limits");
            }
            if (proxy != null
                    && (!"http".equals(proxy.getScheme())
                            || proxy.getHost() == null
                            || proxy.getPort() < 1
                            || proxy.getUserInfo() != null
                            || proxy.getQuery() != null
                            || proxy.getFragment() != null)) {
                throw new IllegalArgumentException("proxy must be an HTTP proxy URL with an explicit port");
            }
        }

        /** An internal service target addressed by application name and the framework default port. */
        public static Client conventional() {
            return new Client(null, null, null, null, null, null, null, null, null, null, true, null, null, null);
        }

        public URI resolveBaseUrl(String name, int defaultPort) {
            return baseUrl == null ? URI.create("http://" + name + ":" + defaultPort) : baseUrl;
        }
    }

    public record Operation(HttpMethod method, String path, Map<String, String> headers) {
        public Operation {
            if (method == null
                    || path == null
                    || !path.startsWith("/")
                    || path.startsWith("//")
                    || path.contains("?")
                    || path.contains("#")
                    || path.contains("\\")
                    || path.contains("..")) {
                throw new IllegalArgumentException("operation requires a method and an absolute path template");
            }
            headers = headers == null ? Map.of() : Map.copyOf(headers);
        }
    }
}
