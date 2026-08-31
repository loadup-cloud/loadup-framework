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
package io.github.loadup.gateway.webmvc.support;

import io.github.loadup.gateway.facade.context.GatewayContext;
import io.github.loadup.gateway.facade.model.GatewayRequest;
import io.github.loadup.gateway.facade.model.RouteConfig;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.web.servlet.function.ServerRequest;

/**
 * Builds a {@link GatewayContext} for the facade security SPI from a {@link ServerRequest}.
 */
public final class GatewayContextFactory {

    private GatewayContextFactory() {}

    public static GatewayContext from(ServerRequest request, RouteConfig route) {
        GatewayRequest gatewayRequest = GatewayRequestFactory.from(request, route);
        Map<String, Object> attributes = new ConcurrentHashMap<>(
                gatewayRequest.getAttributes() == null ? Map.of() : gatewayRequest.getAttributes());
        return new GatewayContext(gatewayRequest, request.servletRequest(), null, null, route, attributes, null);
    }
}
