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

/**
 * Request attribute keys used by the gateway engine.
 */
public final class GatewayAttributes {

    /** Compiled {@link io.github.loadup.gateway.facade.model.RouteConfig} for the matched route. */
    public static final String ROUTE_CONFIG = "loadup.gateway.routeConfig";

    /** Backend {@link io.github.loadup.gateway.facade.model.GatewayResponse} produced by the proxy handler. */
    public static final String PROXY_RESPONSE = "loadup.gateway.proxyResponse";

    private GatewayAttributes() {}
}
