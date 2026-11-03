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
package io.github.loadup.components.serviceauth;

import com.sun.net.httpserver.HttpServer;
import io.github.loadup.commons.util.TenantUtil;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ServiceAccessTokensTest {
    HttpServer server;
    final AtomicInteger requests = new AtomicInteger();
    final AtomicInteger status = new AtomicInteger(200);
    final List<String> basic = new ArrayList<>();
    final List<String> bodies = new ArrayList<>();
    final MutableClock clock = new MutableClock();
    ServiceAuthProperties settings;

    @BeforeEach
    void start() throws Exception {
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/oauth2/token", exchange -> {
            int request = requests.incrementAndGet();
            basic.add(exchange.getRequestHeaders().getFirst("Authorization"));
            bodies.add(new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
            byte[] response = (status.get() == 200
                            ? "{\"access_token\":\"token-" + request
                                    + "\",\"token_type\":\"Bearer\",\"expires_in\":300}"
                            : "{\"error\":\"invalid_client\",\"error_description\":\"synthetic-secret\"}")
                    .getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(status.get(), response.length);
            exchange.getResponseBody().write(response);
            exchange.close();
        });
        server.start();
        settings = new ServiceAuthProperties();
        settings.setTokenUri("http://127.0.0.1:" + server.getAddress().getPort() + "/oauth2/token");
        settings.setAllowInsecureHttp(true);
        settings.setClientSecret("synthetic-secret");
        settings.setRegistrations(Map.of(
                "payment",
                        new ServiceAuthProperties.Registration(
                                "acquire.tenant.payment", "tenant", "acquire", "payment", List.of("pay:payment:read")),
                "realtime",
                        new ServiceAuthProperties.Registration(
                                "acquire.tenant.realtime",
                                "tenant",
                                "acquire",
                                "realtime",
                                List.of("pay:realtime:projection:order"))));
    }

    @AfterEach
    void stop() {
        if (server != null) {
            server.stop(0);
        }
    }

    @Test
    void cachesPerRouteAndReacquiresBeforeExpiryOnBackgroundThreads() {
        var tokens = ServiceAuthAutoConfiguration.create(settings, clock);
        Assertions.assertThat(token(tokens, "acquire", "payment")).isEqualTo("token-1");
        Assertions.assertThat(token(tokens, "acquire", "payment")).isEqualTo("token-1");
        Assertions.assertThat(token(tokens, "acquire", "realtime")).isEqualTo("token-2");
        Assertions.assertThat(requests).hasValue(2);
        clock.now = clock.now.plusSeconds(280);
        Assertions.assertThat(token(tokens, "acquire", "payment")).isEqualTo("token-3");
        String expected = "Basic "
                + Base64.getEncoder()
                        .encodeToString("acquire.tenant.payment:synthetic-secret".getBytes(StandardCharsets.UTF_8));
        Assertions.assertThat(basic.getFirst()).isEqualTo(expected);
        Assertions.assertThat(bodies)
                .allSatisfy(body -> Assertions.assertThat(body).contains("grant_type=client_credentials", "scope="));
        Assertions.assertThat(bodies.getFirst()).contains("scope=pay%3Apayment%3Aread");
    }

    @Test
    void unknownTenantContextCallerOrAudienceCannotAcquireCredentials() {
        var tokens = ServiceAuthAutoConfiguration.create(settings, clock);
        Assertions.assertThatThrownBy(
                        () -> TenantUtil.callWithTenant("other", () -> tokens.token("acquire", "payment")))
                .isInstanceOf(IllegalStateException.class);
        Assertions.assertThatThrownBy(() -> token(tokens, "payment", "payment"))
                .isInstanceOf(IllegalStateException.class);
        Assertions.assertThatThrownBy(() -> token(tokens, "acquire", "customer"))
                .isInstanceOf(IllegalStateException.class);
        Assertions.assertThat(requests).hasValue(0);
    }

    @Test
    void missingTenantContextCannotAcquireCredentials() {
        var tokens = ServiceAuthAutoConfiguration.create(settings, clock);
        Assertions.assertThatThrownBy(() -> tokens.token("acquire", "payment"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Service authorization requires a trusted tenant context");
    }

    @Test
    void issuerFailuresAreSanitizedAndDoNotReturnAnExpiredToken() {
        var tokens = ServiceAuthAutoConfiguration.create(settings, clock);
        token(tokens, "acquire", "payment");
        clock.now = clock.now.plusSeconds(400);
        status.set(401);
        Assertions.assertThatThrownBy(() -> token(tokens, "acquire", "payment"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Service authorization unavailable")
                .hasNoCause();
        status.set(200);
        Assertions.assertThat(token(tokens, "acquire", "payment")).isEqualTo("token-3");
    }

    @Test
    void missingScopesCannotSilentlyAcquireAnUnprivilegedToken() {
        Assertions.assertThatThrownBy(() ->
                        new ServiceAuthProperties.Registration("client", "tenant", "acquire", "payment", List.of()))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void insecureHttpRequiresExplicitOptIn() {
        settings.setAllowInsecureHttp(false);
        Assertions.assertThatThrownBy(() -> ServiceAuthAutoConfiguration.create(settings, clock))
                .isInstanceOf(IllegalArgumentException.class);
    }

    private static String token(ServiceAccessTokens tokens, String service, String audience) {
        return TenantUtil.callWithTenant("tenant", () -> tokens.token(service, audience));
    }

    static class MutableClock extends Clock {
        Instant now = Instant.now();

        @Override
        public ZoneId getZone() {
            return ZoneOffset.UTC;
        }

        @Override
        public Clock withZone(ZoneId zone) {
            return this;
        }

        @Override
        public Instant instant() {
            return now;
        }
    }
}
