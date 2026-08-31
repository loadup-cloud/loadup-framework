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
package io.github.loadup.gateway.webmvc.security;

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
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * JWT Bearer token security strategy (security code {@code default}).
 */
public class DefaultSecurityStrategy implements SecurityStrategy {
    private static final Logger log = LoggerFactory.getLogger(DefaultSecurityStrategy.class);

    private final GatewayProperties gatewayProperties;

    public DefaultSecurityStrategy(GatewayProperties gatewayProperties) {
        this.gatewayProperties = gatewayProperties;
    }

    @Override
    public String getCode() {
        return "default";
    }

    @Override
    public void process(GatewayContext context) {
        GatewayRequest request = context.getRequest();
        GatewayProperties.SecurityConfig securityConfig = gatewayProperties.getSecurity();

        String authHeader = getHeader(request, securityConfig.getHeader());
        if (StringUtils.isBlank(authHeader)) {
            throw GatewayExceptionFactory.unauthorized("Missing authorization header");
        }
        if (!authHeader.startsWith(securityConfig.getPrefix())) {
            throw GatewayExceptionFactory.unauthorized("Invalid authorization header format");
        }

        String token = authHeader.substring(securityConfig.getPrefix().length()).trim();
        try {
            Claims claims = JwtUtils.parseToken(token, securityConfig.getSecret());
            if (JwtUtils.isExpired(claims)) {
                throw GatewayExceptionFactory.unauthorized("Token expired");
            }

            String userId = claims.getSubject();
            String username = claims.get("username", String.class);
            Object rolesObj = claims.get("roles");

            List<String> roles = Collections.emptyList();
            if (rolesObj instanceof String s) {
                roles = Arrays.asList(s.split(","));
            } else if (rolesObj instanceof List<?> list) {
                roles = list.stream().map(String::valueOf).toList();
            }

            request.getHeaders().put("X-User-Id", userId);
            if (username != null) {
                request.getHeaders().put("X-User-Name", username);
            }
            if (!roles.isEmpty()) {
                request.getHeaders().put("X-User-Roles", String.join(",", roles));
            }

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

    private String getHeader(GatewayRequest request, String name) {
        String value = request.getHeaders().get(name);
        if (value == null) {
            value = request.getHeaders().entrySet().stream()
                    .filter(e -> e.getKey().equalsIgnoreCase(name))
                    .map(java.util.Map.Entry::getValue)
                    .findFirst()
                    .orElse(null);
        }
        return value;
    }
}
