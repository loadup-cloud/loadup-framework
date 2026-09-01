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

import io.github.loadup.gateway.facade.exception.GatewayExceptionFactory;
import io.github.loadup.gateway.facade.model.RouteConfig;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authorization.AuthorizationResult;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.access.expression.WebExpressionAuthorizationManager;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;
import org.springframework.web.servlet.function.ServerRequest;

/**
 * Evaluates route-level {@code authorize} rules after authentication.
 *
 * <p>Rules use the standard Spring Security expression language via
 * {@link WebExpressionAuthorizationManager}. Two notations are supported:
 * <ul>
 *   <li>Full SpEL, e.g. {@code hasRole('ADMIN')} or {@code hasAnyAuthority('a','b')}</li>
 *   <li>Comma-separated authority/permission shorthand, e.g.
 *       {@code user:list,user:delete} → {@code hasAnyAuthority('user:list','user:delete')}</li>
 * </ul>
 */
public class RouteAuthorizationManager {

    private static final Logger log = LoggerFactory.getLogger(RouteAuthorizationManager.class);

    private final Map<String, WebExpressionAuthorizationManager> cache = new ConcurrentHashMap<>();

    /**
     * Enforces the route authorization rule, if any.
     *
     * @param route the compiled route
     * @param request the incoming server request
     */
    public void authorize(RouteConfig route, ServerRequest request) {
        String expression = route.getAuthorize();
        if (StringUtils.isBlank(expression)) {
            return;
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw GatewayExceptionFactory.unauthorized("Authentication required");
        }

        WebExpressionAuthorizationManager manager = cache.computeIfAbsent(expression, this::toManager);
        RequestAuthorizationContext context = new RequestAuthorizationContext(request.servletRequest());
        AuthorizationResult result = manager.authorize(() -> authentication, context);
        if (result == null || !result.isGranted()) {
            log.warn(
                    "Route '{}' authorization denied for principal '{}'", route.getRouteId(), authentication.getName());
            throw GatewayExceptionFactory.forbidden("Access denied");
        }
    }

    private WebExpressionAuthorizationManager toManager(String expression) {
        return new WebExpressionAuthorizationManager(toSpel(expression));
    }

    /**
     * Normalizes the configured value into a SpEL expression: full expressions are used
     * as-is, a plain comma-separated list becomes {@code hasAnyAuthority(...)}.
     */
    public static String toSpel(String expression) {
        String trimmed = expression.trim();
        if (trimmed.contains("(") || trimmed.contains(" ")) {
            return trimmed;
        }
        String authorities = Arrays.stream(trimmed.split(","))
                .map(String::trim)
                .filter(StringUtils::isNotBlank)
                .map(a -> "'" + a + "'")
                .collect(Collectors.joining(", "));
        return "hasAnyAuthority(" + authorities + ")";
    }
}
