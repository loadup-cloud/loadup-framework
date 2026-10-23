/*
 * Copyright 2026 LoadUp Cloud
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.github.loadup.components.rpc.grpc;

import io.github.loadup.components.resourceserver.LoadUpJwtAuthenticationConverter;
import io.github.loadup.components.resourceserver.ResourceServerProperties;
import io.grpc.Metadata;
import io.grpc.ServerCall;
import io.grpc.ServerCallHandler;
import io.grpc.Status;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import java.time.Instant;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class RpcJwtInterceptorTest {
    private final JwtDecoder decoder = mock(JwtDecoder.class);
    private final ResourceServerProperties properties = new ResourceServerProperties();
    private final ServerCall<String, String> call = mock(ServerCall.class);

    RpcJwtInterceptorTest() {
        properties.setIssuerUri("https://issuer.example");
        properties.setAudience("payment");
    }

    private Jwt jwt(String audience) {
        return Jwt.withTokenValue("header.payload.signature").header("alg", "RS256")
                .issuer("https://issuer.example").subject("acquire")
                .issuedAt(Instant.now().minusSeconds(10)).expiresAt(Instant.now().plusSeconds(300))
                .audience(List.of(audience)).claim("token_use", "access")
                .claim("tenant_id", "tenant").claim("service_id", "acquire").claim("principal_type", "SERVICE")
                .claim("permissions", List.of("pay:payment:accept")).build();
    }

    @Test
    void verifiedJwtIsPresentOnlyInsideItsOwnCallbacksEvenWhenCallbackThrows() {
        var verified = jwt("payment");
        when(decoder.decode(anyString())).thenReturn(verified);
        var before = SecurityContextHolder.getContext();
        var headers = new Metadata();
        headers.put(RpcServerSupport.AUTHORIZATION, "Bearer header.payload.signature");
        ServerCallHandler<String, String> handler = (serverCall, metadata) -> {
            assertSame(verified, SecurityContextHolder.getContext().getAuthentication().getCredentials());
            return new ServerCall.Listener<>() {
                @Override public void onHalfClose() {
                    assertSame(verified, SecurityContextHolder.getContext().getAuthentication().getCredentials());
                    assertTrue(SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream()
                            .anyMatch(a -> a.getAuthority().equals("pay:payment:accept")));
                    throw new IllegalStateException("synthetic callback failure");
                }
            };
        };
        try {
            var listener = new RpcJwtInterceptor(decoder, new LoadUpJwtAuthenticationConverter(), properties)
                    .interceptCall(call, headers, handler);
            assertSame(before, SecurityContextHolder.getContext());
            assertThrows(IllegalStateException.class, listener::onHalfClose);
            assertSame(before, SecurityContextHolder.getContext());
        } finally {
            SecurityContextHolder.clearContext();
        }
    }

    @Test
    void wrongAudienceAndMissingCredentialCannotReachAnApplicationHandler() {
        when(decoder.decode(anyString())).thenReturn(jwt("other"));
        var interceptor = new RpcJwtInterceptor(decoder, new LoadUpJwtAuthenticationConverter(), properties);
        var headers = new Metadata();
        headers.put(RpcServerSupport.AUTHORIZATION, "Bearer header.payload.signature");
        ServerCallHandler<String, String> handler = mock(ServerCallHandler.class);
        interceptor.interceptCall(call, headers, handler);
        interceptor.interceptCall(call, new Metadata(), handler);
        verifyNoInteractions(handler);
        verify(call, times(2)).close(argThat(status -> status.getCode() == Status.Code.UNAUTHENTICATED), any());
    }
}
