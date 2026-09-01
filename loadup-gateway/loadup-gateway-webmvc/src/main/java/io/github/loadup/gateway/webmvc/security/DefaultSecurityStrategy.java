package io.github.loadup.gateway.webmvc.security;

/*-
 * #%L
 * Loadup Gateway WebMVC Engine
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

import io.github.loadup.components.authorization.model.LoadUpUser;
import io.github.loadup.gateway.facade.context.GatewayContext;
import io.github.loadup.gateway.facade.exception.GatewayExceptionFactory;
import io.github.loadup.gateway.facade.model.GatewayRequest;
import io.github.loadup.gateway.facade.spi.SecurityStrategy;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Bearer token security strategy (security code {@code default}).
 *
 * <p>Token verification and claims mapping are performed by the standard OAuth2 resource server
 * filter chain ({@code BearerTokenAuthenticationFilter} + Nimbus {@code JwtDecoder}); this
 * strategy only enforces that a {@link LoadUpUser} principal is present in the Spring Security
 * context and exposes the authenticated user to downstream gateway processing.
 */
public class DefaultSecurityStrategy implements SecurityStrategy {

    @Override
    public String getCode() {
        return "default";
    }

    @Override
    public void process(GatewayContext context) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null
                || !authentication.isAuthenticated()
                || !(authentication.getPrincipal() instanceof LoadUpUser user)) {
            throw GatewayExceptionFactory.unauthorized("Missing or invalid access token");
        }

        GatewayRequest request = context.getRequest();
        if (user.getUserId() != null) {
            request.getHeaders().put("X-User-Id", user.getUserId());
        }
        if (user.getUsername() != null) {
            request.getHeaders().put("X-User-Name", user.getUsername());
        }
        if (user.getRoles() != null && !user.getRoles().isEmpty()) {
            request.getHeaders().put("X-User-Roles", String.join(",", user.getRoles()));
        }
        if (user.getPermissions() != null && !user.getPermissions().isEmpty()) {
            request.getHeaders().put("X-User-Permissions", String.join(",", user.getPermissions()));
        }

        request.getAttributes().put("userId", user.getUserId());
        request.getAttributes().put("username", user.getUsername());
        request.getAttributes().put("roles", user.getRoles());
        request.getAttributes().put("permissions", user.getPermissions());
    }
}
