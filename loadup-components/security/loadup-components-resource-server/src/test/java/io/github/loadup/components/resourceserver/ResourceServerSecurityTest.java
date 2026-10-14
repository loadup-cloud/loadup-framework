/*
 * #%L
 * LoadUp JWT Resource Server
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
package io.github.loadup.components.resourceserver;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.github.loadup.commons.enums.CommonResultCodeEnum;
import io.github.loadup.components.observability.ApiResultMetrics;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import jakarta.servlet.Filter;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.WebApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.BadJwtException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.web.SecurityFilterChain;
import tools.jackson.databind.json.JsonMapper;

class ResourceServerSecurityTest {
    private final WebApplicationContextRunner runner = new WebApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(ResourceServerAutoConfiguration.class))
            .withUserConfiguration(SecuritySupport.class)
            .withPropertyValues(
                    "loadup.security.resource-server.enabled=true",
                    "loadup.security.resource-server.issuer-uri=https://issuer.test",
                    "loadup.security.resource-server.audience=merchant-api");

    @Test
    void unauthenticatedApiRequestReturnsJsonBusinessFailureAndRecordsOutcome() {
        runner.run(application -> {
            var response = request(
                    application.getBean("springSecurityFilterChain", Filter.class), "/api/private", null, () -> {});
            assertFailure(response, CommonResultCodeEnum.UNAUTHENTICATED);
            assertThat(application
                            .getBean(SimpleMeterRegistry.class)
                            .get("loadup.api.responses")
                            .tag("outcome", "failure")
                            .counter()
                            .count())
                    .isEqualTo(1);
            assertThat(response.getHeader("WWW-Authenticate")).isNull();
        });
    }

    @Test
    void bearerAuthenticationUsesVerifiedPrincipalAndDoesNotCreateSession() {
        runner.run(application -> {
            var called = new java.util.concurrent.atomic.AtomicBoolean();
            Filter filter = application.getBean("springSecurityFilterChain", Filter.class);
            var response = request(filter, "/api/private", "valid", () -> {
                var authentication = SecurityContextHolder.getContext().getAuthentication();
                assertThat(authentication.getName()).isEqualTo("merchant-user");
                assertThat(authentication.isAuthenticated()).isTrue();
                called.set(true);
            });
            assertThat(called).isTrue();
            assertThat(response.getStatus()).isEqualTo(200);
            assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        });
    }

    @Test
    void invalidBearerAndWrongAudienceReturnSafeUnauthenticatedEnvelopes() {
        runner.run(application -> {
            Filter filter = application.getBean("springSecurityFilterChain", Filter.class);
            assertFailure(request(filter, "/api/private", "invalid", () -> {}), CommonResultCodeEnum.UNAUTHENTICATED);
            assertFailure(
                    request(filter, "/api/private", "wrong-audience", () -> {}), CommonResultCodeEnum.UNAUTHENTICATED);
        });
    }

    @Test
    void permissionFailureReturnsAccessDeniedEnvelopeAfterAuthentication() {
        runner.run(application -> {
            var protectedService = application.getBean(ProtectedService.class);
            var response = request(
                    application.getBean("springSecurityFilterChain", Filter.class),
                    "/api/private",
                    "valid",
                    protectedService::adminOnly);
            assertFailure(response, CommonResultCodeEnum.ACCESS_DENIED);
        });
    }

    @Test
    void configuredPublicPathsAndNonApiProtocolsPassThrough() {
        runner.withPropertyValues("loadup.security.resource-server.permit-all[0]=/api/public")
                .run(application -> {
                    Filter filter = application.getBean("springSecurityFilterChain", Filter.class);
                    var calls = new java.util.concurrent.atomic.AtomicInteger();
                    assertThat(request(filter, "/api/public", null, calls::incrementAndGet)
                                    .getStatus())
                            .isEqualTo(200);
                    assertThat(request(filter, "/actuator/health", null, calls::incrementAndGet)
                                    .getStatus())
                            .isEqualTo(200);
                    assertThat(calls).hasValue(2);
                    assertFailure(
                            request(filter, "/api/private", null, calls::incrementAndGet),
                            CommonResultCodeEnum.UNAUTHENTICATED);
                    assertThat(calls).hasValue(2);
                });
    }

    @Test
    void defaultChainCanBeDisabledWhileConverterRemainsAvailable() {
        runner.withPropertyValues("loadup.security.resource-server.default-security-filter-chain=false")
                .run(application -> {
                    assertThat(application).hasNotFailed().doesNotHaveBean(SecurityFilterChain.class);
                    assertThat(application).hasSingleBean(LoadUpJwtAuthenticationConverter.class);
                    assertThat(application
                                    .getBean(ResourceServerProperties.class)
                                    .isDefaultSecurityFilterChain())
                            .isFalse();
                });
    }

    @Test
    void disabledResourceServerDoesNotContributeItsChain() {
        runner.withPropertyValues("loadup.security.resource-server.enabled=false")
                .run(application ->
                        assertThat(application).hasNotFailed().doesNotHaveBean(ResourceServerAutoConfiguration.class));
    }

    @Test
    void missingIssuerOrAudienceFailsStartupEvenWithCustomDecoder() {
        runner.withPropertyValues("loadup.security.resource-server.audience=")
                .run(application -> assertThat(application).hasFailed());
        runner.withPropertyValues("loadup.security.resource-server.issuer-uri=")
                .run(application -> assertThat(application).hasFailed());
    }

    @Test
    void decoderFactoryValidatesSettingsAndBuildsLazyJwkDecoder() {
        var configuration = new ResourceServerAutoConfiguration();
        var properties = new ResourceServerProperties();
        assertThatThrownBy(() -> configuration.resourceServerJwtDecoder(properties))
                .isInstanceOf(IllegalArgumentException.class);
        properties.setIssuerUri("https://issuer.test");
        assertThatThrownBy(() -> configuration.resourceServerJwtDecoder(properties))
                .isInstanceOf(IllegalArgumentException.class);
        properties.setAudience("merchant-api");
        properties.setJwkSetUri("https://issuer.test/jwks");
        assertThat(configuration.resourceServerJwtDecoder(properties)).isNotNull();
    }

    private static MockHttpServletResponse request(Filter filter, String path, String token, Runnable endpoint)
            throws Exception {
        var request = new MockHttpServletRequest("POST", path);
        request.setServletPath(path);
        if (token != null) request.addHeader("Authorization", "Bearer " + token);
        var response = new MockHttpServletResponse();
        filter.doFilter(request, response, (incoming, outgoing) -> {
            endpoint.run();
            ((jakarta.servlet.http.HttpServletResponse) outgoing).setStatus(200);
        });
        assertThat(request.getSession(false)).isNull();
        return response;
    }

    private static void assertFailure(MockHttpServletResponse response, CommonResultCodeEnum code) throws Exception {
        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(response.getContentType()).startsWith("application/json");
        var json = JsonMapper.builder().build().readTree(response.getContentAsString());
        assertThat(json.path("result").path("code").asString()).isEqualTo(code.getCode());
        assertThat(json.has("data")).isTrue();
        assertThat(response.getContentAsString()).doesNotContain("sensitive decoder detail");
    }

    @Configuration(proxyBeanMethods = false)
    @EnableWebSecurity
    @EnableMethodSecurity
    static class SecuritySupport {
        @Bean
        JwtDecoder jwtDecoder() {
            return token -> {
                if (token.equals("invalid")) throw new BadJwtException("sensitive decoder detail");
                return Jwt.withTokenValue(token)
                        .header("alg", "RS256")
                        .subject("merchant-user")
                        .issuer("https://issuer.test")
                        .audience(List.of(token.equals("wrong-audience") ? "other" : "merchant-api"))
                        .issuedAt(Instant.now().minusSeconds(10))
                        .expiresAt(Instant.now().plusSeconds(300))
                        .claim("token_use", "access")
                        .claim("roles", List.of("USER"))
                        .build();
            };
        }

        @Bean
        SimpleMeterRegistry meterRegistry() {
            return new SimpleMeterRegistry();
        }

        @Bean
        ApiResultMetrics apiResultMetrics(SimpleMeterRegistry registry) {
            return new ApiResultMetrics(registry);
        }

        @Bean
        ProtectedService protectedService() {
            return new ProtectedService();
        }
    }

    static class ProtectedService {
        @PreAuthorize("hasRole('ADMIN')")
        public void adminOnly() {}
    }
}
