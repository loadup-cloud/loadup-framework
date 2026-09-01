package io.github.loadup.components.authserver.jwt;

/*-
 * #%L
 * LoadUp Components AuthServer API
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

import java.util.LinkedHashSet;
import java.util.Set;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.server.authorization.token.JwtEncodingContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenCustomizer;

/**
 * Standard {@link OAuth2TokenCustomizer} that writes the LoadUp claims contract into access
 * tokens:
 * <ul>
 *   <li>{@code username} — principal name;</li>
 *   <li>{@code roles} — authority names with the {@code ROLE_} prefix stripped;</li>
 *   <li>{@code permissions} — non-role authority names.</li>
 * </ul>
 *
 * <p>Claims are self-contained so any standard OAuth2 resource server (including the LoadUp
 * gateway) can derive authorities without an introspection call.
 */
public class LoadUpJwtTokenCustomizer implements OAuth2TokenCustomizer<JwtEncodingContext> {

    @Override
    public void customize(JwtEncodingContext context) {
        Authentication principal = context.getPrincipal();
        if (principal == null) {
            return;
        }

        Set<String> roles = new LinkedHashSet<>();
        Set<String> permissions = new LinkedHashSet<>();
        for (GrantedAuthority authority : principal.getAuthorities()) {
            String value = authority.getAuthority();
            if (value == null || value.isBlank()) {
                continue;
            }
            if (value.startsWith("ROLE_")) {
                roles.add(value.substring("ROLE_".length()));
            } else {
                permissions.add(value);
            }
        }

        if (!roles.isEmpty()) {
            context.getClaims().claim("roles", roles);
        }
        if (!permissions.isEmpty()) {
            context.getClaims().claim("permissions", permissions);
        }

        String username = resolveUsername(principal);
        if (username != null) {
            context.getClaims().claim("username", username);
        }
    }

    private static String resolveUsername(Authentication authentication) {
        Object principal = authentication.getPrincipal();
        if (principal instanceof UserDetails userDetails) {
            return userDetails.getUsername();
        }
        if (principal instanceof String name) {
            return name;
        }
        return authentication.getName();
    }
}
