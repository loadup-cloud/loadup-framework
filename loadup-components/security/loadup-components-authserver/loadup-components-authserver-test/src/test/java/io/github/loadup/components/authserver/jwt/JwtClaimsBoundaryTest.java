/*
 * #%L
 * LoadUp Components AuthServer Test
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
package io.github.loadup.components.authserver.jwt;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.server.authorization.OAuth2Authorization;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.token.JwtEncodingContext;

class JwtClaimsBoundaryTest {
    private static JwtClaimsSet customize(Authentication authentication, String audience, boolean withAuthorization) {
        RegisteredClient client = RegisteredClient.withId("client-id")
                .clientId("client")
                .authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
                .build();
        JwtClaimsSet.Builder claims = JwtClaimsSet.builder();
        var builder = JwtEncodingContext.with(JwsHeader.with(SignatureAlgorithm.RS256), claims)
                .registeredClient(client)
                .principal(authentication)
                .authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
                .tokenType(OAuth2TokenType.ACCESS_TOKEN);
        if (withAuthorization) {
            builder.authorization(OAuth2Authorization.withRegisteredClient(client)
                    .principalName("merchant")
                    .authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
                    .build());
        }
        new LoadUpJwtTokenCustomizer(audience).customize(builder.build());
        return claims.build();
    }

    @Test
    void refreshTokensAreNotCustomized() {
        JwtEncodingContext context = mock(JwtEncodingContext.class);
        when(context.getTokenType()).thenReturn(OAuth2TokenType.REFRESH_TOKEN);
        new LoadUpJwtTokenCustomizer().customize(context);
        verify(context, never()).getClaims();
        verify(context, never()).getPrincipal();
    }

    @Test
    void missingPrincipalStillReceivesTokenUseWithoutAudience() {
        JwtClaimsSet.Builder claims = JwtClaimsSet.builder();
        JwtEncodingContext context = mock(JwtEncodingContext.class);
        when(context.getTokenType()).thenReturn(OAuth2TokenType.ACCESS_TOKEN);
        when(context.getClaims()).thenReturn(claims);
        new LoadUpJwtTokenCustomizer().customize(context);
        assertThat(claims.build().getClaims()).containsOnlyKeys("token_use");
    }

    @Test
    void userDetailsAndInvalidAuthoritiesAreHandledWithoutEmptyClaims() {
        var user = User.withUsername("ada")
                .password("unused")
                .authorities("ROLE_USER")
                .build();
        List<GrantedAuthority> authorities = List.of(() -> null, () -> "  ", () -> "ROLE_USER", () -> "read");
        Authentication authentication = new UsernamePasswordAuthenticationToken(user, "unused", authorities);
        JwtClaimsSet claims = customize(authentication, "  ", false);
        assertThat(claims.getClaimAsString("username")).isEqualTo("ada");
        assertThat(claims.getClaimAsStringList("roles")).containsExactly("USER");
        assertThat(claims.getClaimAsStringList("permissions")).containsExactly("read");
        assertThat(claims.getAudience()).isNull();
    }

    @Test
    void authorizationWithoutUserFallsBackToCurrentSubject() {
        LoadUpSubject subject = () -> "merchant-123";
        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(subject);
        when(authentication.getName()).thenReturn("merchant");
        when(authentication.getAuthorities()).thenReturn(List.of());
        JwtClaimsSet claims = customize(authentication, null, true);
        assertThat(claims.getSubject()).isEqualTo("merchant-123");
        assertThat(claims.getClaimAsString("username")).isEqualTo("merchant");
    }

    @Test
    void unknownPrincipalWithoutNameDoesNotWriteUsername() {
        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(new Object());
        when(authentication.getAuthorities()).thenReturn(List.of());
        assertThat(customize(authentication, null, false).getClaims()).containsOnlyKeys("token_use");
    }
}
