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
package io.github.loadup.gateway.webmvc.filter;

import io.github.loadup.gateway.facade.exception.GatewayExceptionFactory;
import io.github.loadup.gateway.facade.model.RouteConfig;
import io.github.loadup.gateway.facade.spi.SecurityStrategy;
import io.github.loadup.gateway.webmvc.security.SecurityStrategyManager;
import io.github.loadup.gateway.webmvc.support.GatewayAttributes;
import io.github.loadup.gateway.webmvc.support.GatewayContextFactory;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.server.mvc.common.MvcUtils;
import org.springframework.web.servlet.function.HandlerFilterFunction;
import org.springframework.web.servlet.function.HandlerFunction;
import org.springframework.web.servlet.function.ServerRequest;
import org.springframework.web.servlet.function.ServerResponse;

/**
 * Applies the route {@code securityCode} strategy (JWT / HMAC / internal / OFF).
 */
public class SecurityHandlerFilterFunction implements HandlerFilterFunction<ServerResponse, ServerResponse> {
    private static final Logger log = LoggerFactory.getLogger(SecurityHandlerFilterFunction.class);

    private final SecurityStrategyManager strategyManager;

    public SecurityHandlerFilterFunction(SecurityStrategyManager strategyManager) {
        this.strategyManager = strategyManager;
    }

    @Override
    public ServerResponse filter(ServerRequest request, HandlerFunction<ServerResponse> next) throws Exception {
        RouteConfig route = MvcUtils.getAttribute(request, GatewayAttributes.ROUTE_CONFIG);
        if (route == null) {
            return next.handle(request);
        }

        String code = route.getSecurityCode();
        if (StringUtils.isBlank(code) || "OFF".equalsIgnoreCase(code)) {
            return next.handle(request);
        }

        SecurityStrategy strategy = strategyManager.getStrategy(code);
        if (strategy == null) {
            log.error("Unknown security code '{}' for route '{}'", code, route.getRouteId());
            throw GatewayExceptionFactory.systemError("Unknown security strategy: " + code);
        }

        strategy.process(GatewayContextFactory.from(request, route));
        return next.handle(request);
    }
}
