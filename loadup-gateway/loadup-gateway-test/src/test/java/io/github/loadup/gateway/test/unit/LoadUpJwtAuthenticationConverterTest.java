package io.github.loadup.gateway.test.unit;

/*-
 * #%L
 * LoadUp Gateway Test
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

import io.github.loadup.components.authorization.model.LoadUpUser;
import io.github.loadup.gateway.webmvc.security.LoadUpJwtAuthenticationConverter;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

class LoadUpJwtAuthenticationConverterTest {

    private final LoadUpJwtAuthenticationConverter converter = new LoadUpJwtAuthenticationConverter();

    @Test
    void convertsClaimsToPrincipalAndAuthorities() {
        Jwt jwt = Jwt.withTokenValue("token")
                .header("alg", "HS256")
                .subject("u-1")
                .claim("username", "admin")
                .claim("roles", List.of("ADMIN", "ROLE_AUDITOR"))
                .claim("permissions", List.of("user:read", "user:delete"))
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(300))
                .build();

        AbstractAuthenticationToken authentication = converter.convert(jwt);

        assertThat(authentication).isNotNull();
        assertThat(authentication.isAuthenticated()).isTrue();
        assertThat(authentication.getPrincipal()).isInstanceOf(LoadUpUser.class);
        LoadUpUser user = (LoadUpUser) authentication.getPrincipal();
        assertThat(user.getUserId()).isEqualTo("u-1");
        assertThat(user.getUsername()).isEqualTo("admin");
        assertThat(user.getRoles()).containsExactly("ADMIN", "ROLE_AUDITOR");
        assertThat(user.getPermissions()).containsExactly("user:read", "user:delete");
        assertThat(authentication.getAuthorities())
                .extracting(GrantedAuthority::getAuthority)
                .contains("ROLE_ADMIN", "ADMIN", "ROLE_AUDITOR", "user:read", "user:delete");
    }

    @Test
    void handlesCommaSeparatedStringClaims() {
        Jwt jwt = Jwt.withTokenValue("token")
                .header("alg", "HS256")
                .subject("u-2")
                .claim("roles", "USER,ADMIN")
                .claim("permissions", "order:read")
                .build();

        AbstractAuthenticationToken authentication = converter.convert(jwt);

        LoadUpUser user = (LoadUpUser) authentication.getPrincipal();
        assertThat(user.getRoles()).containsExactly("USER", "ADMIN");
        assertThat(user.getPermissions()).containsExactly("order:read");
        assertThat(authentication.getAuthorities())
                .extracting(GrantedAuthority::getAuthority)
                .contains("ROLE_USER", "ROLE_ADMIN", "order:read");
    }
}
