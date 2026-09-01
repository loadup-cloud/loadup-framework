package io.github.loadup.components.authserver.jwt;

/*-
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

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.token.JwtEncodingContext;

@DisplayName("LoadUpJwtTokenCustomizer")
class LoadUpJwtTokenCustomizerTest {

    private final LoadUpJwtTokenCustomizer customizer = new LoadUpJwtTokenCustomizer();

    @Test
    @DisplayName("writes roles, permissions and username claims from the principal authorities")
    void writesClaimsContract() {
        var authentication = new UsernamePasswordAuthenticationToken(
                "admin",
                "n/a",
                List.of(
                        new SimpleGrantedAuthority("ROLE_ADMIN"),
                        new SimpleGrantedAuthority("user:write"),
                        new SimpleGrantedAuthority("user:list")));
        JwsHeader.Builder headers = JwsHeader.with(MacAlgorithm.HS256);
        JwtClaimsSet.Builder claims = JwtClaimsSet.builder();
        JwtEncodingContext context = JwtEncodingContext.with(headers, claims)
                .registeredClient(testClient())
                .principal(authentication)
                .authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
                .tokenType(OAuth2TokenType.ACCESS_TOKEN)
                .build();

        customizer.customize(context);

        JwtClaimsSet result = claims.build();
        assertThat(result.getClaimAsString("username")).isEqualTo("admin");
        assertThat(result.getClaimAsStringList("roles")).containsExactly("ADMIN");
        assertThat(result.getClaimAsStringList("permissions")).containsExactly("user:write", "user:list");
    }

    @Test
    @DisplayName("leaves claims empty when the principal has no authorities")
    void skipsEmptyClaims() {
        var authentication = new UsernamePasswordAuthenticationToken("svc", "n/a", List.of());
        JwsHeader.Builder headers = JwsHeader.with(MacAlgorithm.HS256);
        JwtClaimsSet.Builder claims = JwtClaimsSet.builder();
        JwtEncodingContext context = JwtEncodingContext.with(headers, claims)
                .registeredClient(testClient())
                .principal(authentication)
                .authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
                .tokenType(OAuth2TokenType.ACCESS_TOKEN)
                .build();

        customizer.customize(context);

        JwtClaimsSet result = claims.build();
        assertThat(result.getClaimAsString("username")).isEqualTo("svc");
        assertThat(result.getClaims()).doesNotContainKey("roles");
        assertThat(result.getClaims()).doesNotContainKey("permissions");
    }

    private static RegisteredClient testClient() {
        return RegisteredClient.withId("test")
                .clientId("test-client")
                .authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
                .build();
    }
}
