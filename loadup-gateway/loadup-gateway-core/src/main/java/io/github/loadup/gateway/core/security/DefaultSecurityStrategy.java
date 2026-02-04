package io.github.loadup.gateway.core.security;

/*-
 * #%L
 * LoadUp Gateway Core
 * %%
 * Copyright (C) 2025 - 2026 LoadUp Cloud
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

import io.github.loadup.commons.util.JwtUtils;
import io.github.loadup.gateway.facade.config.GatewayProperties;
import io.github.loadup.gateway.facade.context.GatewayContext;
import io.github.loadup.gateway.facade.exception.GatewayExceptionFactory;
import io.github.loadup.gateway.facade.model.GatewayRequest;
import io.github.loadup.gateway.facade.spi.SecurityStrategy;
import io.jsonwebtoken.Claims;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

/**
 * Default JWT-based security strategy.
 *
 * <p>Validates JWT token and populates SecurityContext for downstream authorization.</p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DefaultSecurityStrategy implements SecurityStrategy {

    private final GatewayProperties gatewayProperties;

    @Override
    public String getCode() {
        return "default";
    }

    @Override
    public void process(GatewayContext context) {
        log.debug("Processing JWT security strategy for route: {}", context.getRoute().getRouteId());

        GatewayRequest request = context.getRequest();
        GatewayProperties.SecurityConfig securityConfig = gatewayProperties.getSecurity();

        // 1. Extract Authorization header
        String authHeader = request.getHeaders().get(securityConfig.getHeader());
        if (authHeader == null) {
            // Try case-insensitive lookup
            authHeader = request.getHeaders().entrySet().stream()
                .filter(e -> e.getKey().equalsIgnoreCase(securityConfig.getHeader()))
                .map(java.util.Map.Entry::getValue)
                .findFirst()
                .orElse(null);
        }

        if (StringUtils.isBlank(authHeader)) {
            throw GatewayExceptionFactory.unauthorized("Missing authorization header");
        }

        if (!authHeader.startsWith(securityConfig.getPrefix())) {
            throw GatewayExceptionFactory.unauthorized("Invalid authorization header format");
        }

        String token = authHeader.substring(securityConfig.getPrefix().length()).trim();

        try {
            // 2. Parse and validate JWT
            Claims claims = JwtUtils.parseToken(token, securityConfig.getSecret());
            if (JwtUtils.isExpired(claims)) {
                throw GatewayExceptionFactory.unauthorized("Token expired");
            }

            // 3. Extract user info
            String userId = claims.getSubject();
            String username = claims.get("username", String.class);
            Object rolesObj = claims.get("roles");

            List<String> roles = Collections.emptyList();
            if (rolesObj instanceof String) {
                roles = Arrays.asList(((String) rolesObj).split(","));
            } else if (rolesObj instanceof List) {
                roles = (List<String>) rolesObj;
            }

            log.debug("User authenticated: userId={}, username={}, roles={}", userId, username, roles);

            // 4. Pass user info to downstream via headers (for microservices)
            request.getHeaders().put("X-User-Id", userId);
            if (username != null) {
                request.getHeaders().put("X-User-Name", username);
            }
            if (!roles.isEmpty()) {
                request.getHeaders().put("X-User-Roles", String.join(",", roles));
            }

            // 5. Store in context attributes
            request.getAttributes().put("userId", userId);
            request.getAttributes().put("username", username);
            request.getAttributes().put("roles", roles);
            request.getAttributes().put("claims", claims);

        } catch (io.jsonwebtoken.JwtException e) {
            log.warn("JWT validation failed", e);
            throw GatewayExceptionFactory.unauthorized("Invalid token: " + e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error during JWT authentication", e);
            throw GatewayExceptionFactory.systemError("Authentication failed");
        }
    }
}
