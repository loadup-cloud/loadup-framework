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

import io.github.loadup.components.authorization.model.LoadUpUser;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

class LoadUpJwtAuthenticationConverterTest {
    @Test
    void authenticationNameUsesSubjectRatherThanPrincipalDescription() {
        Jwt jwt = Jwt.withTokenValue("test")
                .header("alg", "RS256")
                .subject("u-1")
                .claim("username", "admin")
                .claim("roles", List.of("ADMIN"))
                .claim("permissions", List.of("user:read", "user:delete", "audit:read"))
                .build();

        var authentication = new LoadUpJwtAuthenticationConverter().convert(jwt);

        assertThat(authentication.getName()).isEqualTo("u-1");
        assertThat(authentication.getPrincipal()).isInstanceOf(LoadUpUser.class);
        assertThat(((LoadUpUser) authentication.getPrincipal()).getUsername()).isEqualTo("admin");
        assertThat(authentication.getAuthorities())
                .extracting(GrantedAuthority::getAuthority)
                .containsExactlyInAnyOrder("ROLE_ADMIN", "ADMIN", "user:read", "user:delete", "audit:read");
    }
}
