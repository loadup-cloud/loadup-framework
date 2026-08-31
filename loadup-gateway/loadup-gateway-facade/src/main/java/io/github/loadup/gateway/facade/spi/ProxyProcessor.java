package io.github.loadup.gateway.facade.spi;

/*-
 * #%L
 * LoadUp Gateway Facade
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

import io.github.loadup.gateway.facade.model.GatewayRequest;
import io.github.loadup.gateway.facade.model.GatewayResponse;
import io.github.loadup.gateway.facade.model.RouteConfig;

/**
 * Proxy plugin SPI interface.
 *
 * <p>Each protocol backend (HTTP / BEAN / RPC) registers one implementation; the
 * gateway engine looks it up by {@link #getSupportedProtocol()}.
 */
public interface ProxyProcessor {

    /**
     * Plugin name
     */
    String getName();

    /**
     * Plugin type
     */
    String getType();

    /**
     * Plugin version
     */
    String getVersion();

    /**
     * Plugin priority
     */
    int getPriority();

    /**
     * Initialize the plugin
     */
    void initialize();

    /**
     * Destroy the plugin
     */
    void destroy();

    /**
     * Proxy request to target service
     */
    GatewayResponse proxy(GatewayRequest request, RouteConfig route) throws Exception;

    /**
     * Get supported protocol type
     */
    String getSupportedProtocol();
}
